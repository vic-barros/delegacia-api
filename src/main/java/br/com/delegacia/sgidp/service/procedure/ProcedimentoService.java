package br.com.delegacia.sgidp.service.procedure;

import br.com.delegacia.sgidp.dto.custody.HistoricoCustodiaResponseDto;
import br.com.delegacia.sgidp.dto.procedure.ArquivarProcedimentoRequestDto;
import br.com.delegacia.sgidp.dto.procedure.HistoricoRemessaFinalResponseDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoRequestDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoResponseDto;
import br.com.delegacia.sgidp.dto.procedure.RegistrarRemessaFinalRequestDto;
import br.com.delegacia.sgidp.dto.procedure.HistoricoStatusResponseDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoDetalheResponseDto;
import br.com.delegacia.sgidp.dto.delegation.RepassePendenteResponseDto;
import br.com.delegacia.sgidp.dto.procedure.ReabrirProcedimentoRequestDto;
import br.com.delegacia.sgidp.enums.custody.OrigemHistoricoCustodia;
import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.enums.hearing.StatusOitiva;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.exception.ConfirmacaoNecessariaException;
import br.com.delegacia.sgidp.exception.RecursoDuplicadoException;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.custody.HistoricoCustodia;
import br.com.delegacia.sgidp.model.hearing.Oitiva;
import br.com.delegacia.sgidp.model.procedure.HistoricoRemessaFinal;
import br.com.delegacia.sgidp.model.procedure.HistoricoStatusProcedimento;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.custody.HistoricoCustodiaRepository;
import br.com.delegacia.sgidp.repository.delegation.RepasseRepository;
import br.com.delegacia.sgidp.repository.hearing.OitivaRepository;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import br.com.delegacia.sgidp.repository.procedure.HistoricoRemessaFinalRepository;
import br.com.delegacia.sgidp.repository.procedure.HistoricoStatusProcedimentoRepository;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcedimentoService {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final ProcedimentoRepository procedimentoRepository;
    private final HistoricoCustodiaRepository historicoCustodiaRepository;
    private final HistoricoStatusProcedimentoRepository historicoStatusProcedimentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RepasseRepository repasseRepository;
    private final OitivaRepository oitivaRepository;
    private final HistoricoRemessaFinalRepository historicoRemessaFinalRepository;


    @Transactional
    public ProcedimentoResponseDto cadastrar(ProcedimentoRequestDto dto, String login) {


        if (procedimentoRepository.existsByTipoProcedimentoAndNumeroProcedimentoAndAnoProcedimento(
                dto.tipoProcedimento(),
                dto.numeroProcedimento(),
                dto.anoProcedimento())) {

            throw new RecursoDuplicadoException(
                    "Já existe um procedimento com esse tipo, número e ano"
            );
        }

        LocalDate hoje = LocalDate.now();


        if (dto.anoProcedimento() > hoje.getYear()) {
            throw new RegraNegocioException(
                    "O ano do procedimento não pode ser futuro"
            );
        }


        if (dto.dataAbertura().isAfter(hoje)) {
            throw new RegraNegocioException(
                    "A data de abertura não pode ser futura"
            );
        }

        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário autenticado não encontrado"
                ));


        Procedimento procedimento = new Procedimento();

        procedimento.setTipoProcedimento(dto.tipoProcedimento());
        procedimento.setNumeroProcedimento(dto.numeroProcedimento());
        procedimento.setAnoProcedimento(dto.anoProcedimento());
        procedimento.setCrime(dto.crime());
        procedimento.setDataAbertura(dto.dataAbertura());


        procedimento.setStatusProcedimento(StatusProcedimento.EM_ANDAMENTO);

        procedimento.setDetentorAtual(usuario);

        procedimento = procedimentoRepository.save(procedimento);

        HistoricoCustodia historico = new HistoricoCustodia(
                procedimento,
                usuario,
                OrigemHistoricoCustodia.CADASTRO_INICIAL,
                null
        );

        historicoCustodiaRepository.save(historico);

        historicoStatusProcedimentoRepository.save(new HistoricoStatusProcedimento(
                procedimento, null, StatusProcedimento.EM_ANDAMENTO, "Cadastro do procedimento", usuario
        ));

        return ProcedimentoResponseDto.de(procedimento);
    }

    @Transactional
    public ProcedimentoResponseDto reabrir(Long id, ReabrirProcedimentoRequestDto dadosDto, String login) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário autenticado não encontrado"
                ));

        Procedimento procedimento = procedimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado"));

        boolean delegado = "ROLE_DELEGADO".equals(usuario.getRole().getAcesso());
        boolean detentor = procedimento.getDetentorAtual() != null
                && usuario.getId().equals(procedimento.getDetentorAtual().getId());
        if (!delegado && !detentor) {
            throw new RegraNegocioException("Apenas o detentor atual do procedimento ou um Delegado podem reabrir esse procedimento");
        }

        if (!procedimento.getStatusProcedimento().equals(StatusProcedimento.ARQUIVADO)) {
            throw new RegraNegocioException("Não é possível reabrir um procedimento que não está arquivado");
        }

        StatusProcedimento statusAnterior = procedimento.getStatusProcedimento();
        procedimento.setStatusProcedimento(StatusProcedimento.EM_ANDAMENTO);

        HistoricoStatusProcedimento novoHistorico = new HistoricoStatusProcedimento(procedimento, statusAnterior, StatusProcedimento.EM_ANDAMENTO, dadosDto.motivo(), usuario);
        historicoStatusProcedimentoRepository.save(novoHistorico);

        return ProcedimentoResponseDto.de(procedimento);
    }

    @Transactional
    public ProcedimentoResponseDto arquivar(Long id, ArquivarProcedimentoRequestDto dto, String login) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário autenticado não encontrado"
                ));

        Procedimento procedimento = procedimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Procedimento não encontrado"
                ));

        // Delegado arquiva qualquer procedimento; Policial, só o que está na posse dele
        // (o Estagiário já é barrado no SecurityConfig). Compara o texto do papel (getAcesso), não o objeto Role.
        boolean delegado = "ROLE_DELEGADO".equals(usuario.getRole().getAcesso());
        boolean detentor = procedimento.getDetentorAtual() != null
                && usuario.getId().equals(procedimento.getDetentorAtual().getId());
        if (!delegado && !detentor) {
            throw new RegraNegocioException("Apenas o detentor atual do procedimento ou um Delegado podem arquivar esse procedimento");
        }

        if (procedimento.getStatusProcedimento() == StatusProcedimento.ARQUIVADO) {
            throw new RegraNegocioException("O procedimento já está arquivado");
        }

        if (repasseRepository.existsByProcedimentoIdAndStatusRepasse(procedimento.getId(), StatusRepasse.PENDENTE)) {
            throw new RegraNegocioException("Não é possível arquivar o procedimento enquanto houver repasses pendentes");
        }

        // A mensagem lista as datas para o usuário concluir as já realizadas e desmarcar as demais
        List<Oitiva> agendadas = oitivaRepository.findByProcedimentoIdAndStatusOitivaOrderByDataHoraAsc(id, StatusOitiva.AGENDADA);
        if (!agendadas.isEmpty()) {
            List<String> datas = new ArrayList<>();
            for (Oitiva oitiva : agendadas) {
                datas.add(oitiva.getDataHora().format(DATA_HORA));
            }
            throw new RegraNegocioException("Não é possível arquivar o procedimento enquanto houver oitivas agendadas ("
                    + String.join(", ", datas) + "). Conclua as já realizadas e desmarque as demais");
        }

        StatusProcedimento statusAnterior = procedimento.getStatusProcedimento();
        procedimento.setStatusProcedimento(StatusProcedimento.ARQUIVADO);

        historicoStatusProcedimentoRepository.save(new HistoricoStatusProcedimento(procedimento, statusAnterior, StatusProcedimento.ARQUIVADO, dto.motivo().trim(), usuario));

        return ProcedimentoResponseDto.de(procedimento);
    }

    @Transactional
    public ProcedimentoResponseDto registrarRemessaFinal(Long id, boolean sobrescrever, RegistrarRemessaFinalRequestDto dadosDto, String login) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário autenticado não encontrado"
                ));

        Procedimento procedimento = procedimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Procedimento não encontrado"
                ));

        if (dadosDto.data().isBefore(procedimento.getDataAbertura())) {
            throw new RegraNegocioException("Não é possível definir uma data de remessa anterior à data de abertura do procedimento.");
        }

        String protocolo = dadosDto.numeroProtocolo().strip();

        if (dadosDto.data().equals(procedimento.getDataRemessaFinal()) && protocolo.equals(procedimento.getProtocoloRemessaFinal())) {
            throw new RecursoDuplicadoException("Já existe um registro de remessa com a mesma data e número de protocolo.");
        }

        if (procedimento.getStatusProcedimento().equals(StatusProcedimento.ARQUIVADO)) {
            throw new RegraNegocioException("Não é possível registrar uma remessa para um procedimento arquivado.");
        }

        boolean delegado = "ROLE_DELEGADO".equals(usuario.getRole().getAcesso());
        boolean detentor = procedimento.getDetentorAtual() != null
                && usuario.getId().equals(procedimento.getDetentorAtual().getId());
        if (!delegado && !detentor) {
            throw new RegraNegocioException("Apenas o detentor atual do procedimento ou um Delegado podem registrar a remessa final desse procedimento");
        }

        boolean existeRemessaRegistrada = procedimento.getDataRemessaFinal() != null;
        if (existeRemessaRegistrada && !sobrescrever) {
            throw new ConfirmacaoNecessariaException("Já existe uma remessa registrada. Confirme que deseja sobrescrever antes de prosseguir.", "sobrescrever");
        }

        LocalDate dataAnterior = procedimento.getDataRemessaFinal();
        String protocoloAnterior = procedimento.getProtocoloRemessaFinal();

        procedimento.setDataRemessaFinal(dadosDto.data());
        procedimento.setProtocoloRemessaFinal(protocolo);

        historicoRemessaFinalRepository.save(new HistoricoRemessaFinal(
                procedimento,
                dataAnterior,
                protocoloAnterior,
                dadosDto.data(),
                protocolo,
                usuario
        ));

        return ProcedimentoResponseDto.de(procedimento);
    }

    @Transactional(readOnly = true)
    public List<HistoricoRemessaFinalResponseDto> listarHistoricoRemessa(Long id) {
        if (!procedimentoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Procedimento não encontrado");
        }
        return historicoRemessaFinalRepository
                .findByProcedimentoIdOrderByDataRegistroAsc(id)
                .stream()
                .map(HistoricoRemessaFinalResponseDto::de)
                .toList();
    }

    // T08: dados do procedimento + repasse pendente (null se não houver)
    @Transactional(readOnly = true)
    public ProcedimentoDetalheResponseDto buscarPorId(Long id) {
        Procedimento procedimento = procedimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado"));

        RepassePendenteResponseDto repassePendente = repasseRepository
                .findFirstByProcedimentoIdAndStatusRepasse(id, StatusRepasse.PENDENTE)
                .map(RepassePendenteResponseDto::de)
                .orElse(null);

        return new ProcedimentoDetalheResponseDto(ProcedimentoResponseDto.de(procedimento), repassePendente);
    }

    // T08: quem teve a posse e quando (cadastro inicial e cada repasse aceito)
    @Transactional(readOnly = true)
    public List<HistoricoCustodiaResponseDto> listarHistoricoCustodia(Long id) {
        verificarExistencia(id);
        return historicoCustodiaRepository.findByProcedimentoIdOrderByDataInicioAsc(id)
                .stream()
                .map(HistoricoCustodiaResponseDto::de)
                .toList();
    }

    // T08: mudanças de status com motivo e responsável
    @Transactional(readOnly = true)
    public List<HistoricoStatusResponseDto> listarHistoricoStatus(Long id) {
        verificarExistencia(id);
        return historicoStatusProcedimentoRepository.findByProcedimentoIdOrderByDataTransicaoAsc(id)
                .stream()
                .map(HistoricoStatusResponseDto::de)
                .toList();
    }

    // Sem esta checagem, um id inexistente devolveria lista vazia (200) em vez de 404
    private void verificarExistencia(Long id) {
        if (!procedimentoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Procedimento não encontrado");
        }
    }
}
