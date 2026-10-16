package br.com.delegacia.sgidp.service.hearing;

import br.com.delegacia.sgidp.dto.hearing.*;
import br.com.delegacia.sgidp.enums.hearing.StatusOitiva;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.enums.user.StatusUsuario;
import br.com.delegacia.sgidp.exception.ConfirmacaoNecessariaException;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.hearing.Oitiva;
import br.com.delegacia.sgidp.model.hearing.OitivaParte;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.hearing.OitivaRepository;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OitivaService {

    // Papéis que podem ser responsáveis por uma oitiva (o Estagiário conduz oitivas sob supervisão)
    private static final Set<String> PAPEIS_RESPONSAVEL = Set.of("ROLE_DELEGADO", "ROLE_POLICIAL", "ROLE_ESTAGIARIO");

    // Janela do aviso de conflito: outra oitiva do mesmo responsável até 59 min antes ou depois
    private static final long JANELA_CONFLITO_MINUTOS = 59;

    private final OitivaNotificacaoService oitivaNotificacaoService;

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final OitivaRepository oitivaRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final UsuarioRepository usuarioRepository;

    // Agendar: nasce AGENDADA; procedimento arquivado não aceita novas diligências
    @Transactional
    public OitivaResponseDto agendar(OitivaAgendamentoRequestDto dados, String loginLogado) {
        Procedimento procedimento = procedimentoRepository.findById(dados.procedimentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado"));
        if (procedimento.getStatusProcedimento() == StatusProcedimento.ARQUIVADO) {
            throw new RegraNegocioException("Procedimento arquivado. É necessário desarquivar para novas diligências.");
        }

        Usuario responsavel = buscarResponsavel(dados.responsavelId());
        verificarConflito(responsavel, dados.dataHora(), null, dados.confirmarConflito());

        Oitiva oitiva = new Oitiva();
        oitiva.setProcedimento(procedimento);
        oitiva.setResponsavel(responsavel);
        oitiva.setDataHora(dados.dataHora());
        Usuario usuarioLogado = buscarUsuarioLogado(loginLogado);
        oitiva.setCadastradoPor(usuarioLogado);
        // statusOitiva já começa AGENDADA na entidade
        for (OitivaParteRequestDto parte : dados.partes()) {
            oitiva.adicionarParte(new OitivaParte(parte.tipoParte(), parte.nomeParte().trim()));
        }
        oitivaRepository.save(oitiva);

        oitivaNotificacaoService.avisarAtribuicao(oitiva, usuarioLogado);

        return OitivaResponseDto.de(oitiva);
    }

    // Listagem com filtros opcionais. minhas = responsável é quem está logado
    @Transactional(readOnly = true)
    public List<OitivaResponseDto> listar(LocalDate dataInicio, LocalDate dataFim, Long responsavelId,
                                          StatusOitiva status, Long procedimentoId, boolean minhas,
                                          boolean atrasadas, String loginLogado) {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new RegraNegocioException("A data final não pode ser anterior à data inicial");
        }

        // Datas viram intervalo de data/hora: [dataInicio 00:00, dia seguinte a dataFim 00:00)
        LocalDateTime inicio = null;
        if (dataInicio != null) {
            inicio = dataInicio.atStartOfDay();
        }
        LocalDateTime fim = null;
        if (dataFim != null) {
            fim = dataFim.plusDays(1).atStartOfDay();
        }
        String responsavelLogin = null;
        if (minhas) {
            responsavelLogin = loginLogado;
        }
        LocalDateTime agora = null;
        if (atrasadas) {
            agora = LocalDateTime.now();
        }

        return oitivaRepository.buscar(inicio, fim, responsavelId, responsavelLogin, status, procedimentoId,
                        agora, StatusOitiva.AGENDADA)
                .stream()
                .map(OitivaResponseDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public OitivaResponseDto buscarPorId(Long id) {
        return OitivaResponseDto.de(buscarOitiva(id));
    }

    // Editar responsável e partes (a lista enviada substitui a anterior)
    // Editar responsável e partes (a lista enviada substitui a anterior)
    @Transactional
    public OitivaResponseDto editar(Long id, OitivaEdicaoRequestDto dados, String loginLogado) {
        Oitiva oitiva = buscarOitiva(id);
        exigirAgendada(oitiva);

        Usuario responsavel = buscarResponsavel(dados.responsavelId());
        Usuario usuarioLogado = buscarUsuarioLogado(loginLogado);
        // Só verifica conflito se o responsável mudou (a data não muda aqui)
        boolean trocouResponsavel = !responsavel.getId().equals(oitiva.getResponsavel().getId());
        if (trocouResponsavel) {
            verificarConflito(responsavel, oitiva.getDataHora(), oitiva.getId(), dados.confirmarConflito());
        }
        oitiva.setResponsavel(responsavel);

        if (trocouResponsavel) {
            oitivaNotificacaoService.apagarAvisos(oitiva);
            oitivaNotificacaoService.avisarAtribuicao(oitiva, usuarioLogado);
        }

        for (OitivaParte parte : new ArrayList<>(oitiva.getPartes())) {
            oitiva.removerParte(parte);
        }
        for (OitivaParteRequestDto parte : dados.partes()) {
            oitiva.adicionarParte(new OitivaParte(parte.tipoParte(), parte.nomeParte().trim()));
        }

        oitiva.setAlteradoPor(usuarioLogado);
        return OitivaResponseDto.de(oitiva);
    }

    // Remarcar: só a data/hora muda, o status continua AGENDADA
    @Transactional
    public OitivaResponseDto remarcar(Long id, OitivaRemarcacaoRequestDto dados, String loginLogado) {
        Oitiva oitiva = buscarOitiva(id);
        exigirAgendada(oitiva);

        if (dados.dataHora().equals(oitiva.getDataHora())) {
            throw new RegraNegocioException("A nova data e hora são iguais às atuais");
        }
        verificarConflito(oitiva.getResponsavel(), dados.dataHora(), oitiva.getId(), dados.confirmarConflito());

        oitiva.setDataHora(dados.dataHora());
        oitiva.setAlteradoPor(buscarUsuarioLogado(loginLogado));
        // Nova data: os avisos antigos não valem mais; a tarefa agendada gera de novo perto da nova data
        oitivaNotificacaoService.apagarAvisos(oitiva);
        return OitivaResponseDto.de(oitiva);
    }

    // Desmarcar: exige motivo
    @Transactional
    public OitivaResponseDto desmarcar(Long id, OitivaDesmarcacaoRequestDto dados, String loginLogado) {
        Oitiva oitiva = buscarOitiva(id);
        exigirAgendada(oitiva);

        oitiva.setStatusOitiva(StatusOitiva.DESMARCADA);
        oitiva.setMotivoCancelamento(dados.motivo().trim());
        oitiva.setAlteradoPor(buscarUsuarioLogado(loginLogado));
        oitivaNotificacaoService.resolverAvisos(oitiva);
        return OitivaResponseDto.de(oitiva);
    }

    // Concluir: permitido a qualquer momento. Se for antes da data marcada (oitiva antecipada),
    // a data passa a ser o momento da conclusão, para o registro refletir quando ela aconteceu
    @Transactional
    public OitivaResponseDto concluir(Long id, String loginLogado) {
        Oitiva oitiva = buscarOitiva(id);
        exigirAgendada(oitiva);

        LocalDateTime agora = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        if (oitiva.getDataHora().isAfter(agora)) {
            oitiva.setDataHora(agora);
        }
        oitiva.setStatusOitiva(StatusOitiva.CONCLUIDA);
        oitiva.setAlteradoPor(buscarUsuarioLogado(loginLogado));
        oitivaNotificacaoService.resolverAvisos(oitiva);
        return OitivaResponseDto.de(oitiva);
    }

    // ---------- métodos auxiliares ----------

    private Oitiva buscarOitiva(Long id) {
        return oitivaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Oitiva não encontrada"));
    }

    private Usuario buscarUsuarioLogado(String login) {
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));
    }

    // Responsável: Delegado, Policial ou Estagiário com cadastro aprovado
    private Usuario buscarResponsavel(Long id) {
        Usuario responsavel = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Responsável não encontrado"));
        if (responsavel.getStatusUsuario() != StatusUsuario.APROVADO
                || !PAPEIS_RESPONSAVEL.contains(responsavel.getRole().getAcesso())) {
            throw new RegraNegocioException(
                    "O responsável deve ser Delegado, Policial ou Estagiário com cadastro aprovado");
        }
        return responsavel;
    }

    // Oitiva CONCLUIDA ou DESMARCADA não pode mais ser alterada
    private void exigirAgendada(Oitiva oitiva) {
        if (oitiva.getStatusOitiva() != StatusOitiva.AGENDADA) {
            throw new RegraNegocioException("Oitiva " + oitiva.getStatusOitiva().getDescricao().toLowerCase()
                    + " não pode ser alterada");
        }
    }

    // Outra oitiva AGENDADA do mesmo responsável na janela: avisa (409) até o usuário confirmar
    private void verificarConflito(Usuario responsavel, LocalDateTime dataHora, Long ignorarId, Boolean confirmado) {
        if (Boolean.TRUE.equals(confirmado)) {
            return;
        }
        List<Oitiva> conflitos = oitivaRepository.buscarConflitos(responsavel.getId(), StatusOitiva.AGENDADA,
                dataHora.minusMinutes(JANELA_CONFLITO_MINUTOS), dataHora.plusMinutes(JANELA_CONFLITO_MINUTOS),
                ignorarId);
        if (!conflitos.isEmpty()) {
            Oitiva existente = conflitos.get(0);
            Procedimento procedimento = existente.getProcedimento();
            throw new ConfirmacaoNecessariaException(
                    responsavel.getNome() + " já tem uma oitiva em " + existente.getDataHora().format(FORMATO_DATA_HORA)
                            + " (" + procedimento.getTipoProcedimento() + " " + procedimento.getNumeroProcedimento()
                            + "/" + procedimento.getAnoProcedimento() + "). "
                            + "Tem certeza que quer marcar para o mesmo horário?",
                    "confirmarConflito");
        }
    }
}