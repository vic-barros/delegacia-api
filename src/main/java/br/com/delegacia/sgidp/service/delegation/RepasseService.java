package br.com.delegacia.sgidp.service.delegation;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.delegacia.sgidp.dto.delegation.RecusarRepasseRequestDto;
import br.com.delegacia.sgidp.dto.delegation.RepasseRequestDto;
import br.com.delegacia.sgidp.dto.delegation.RepasseResponseDto;
import br.com.delegacia.sgidp.enums.custody.OrigemHistoricoCustodia;
import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.enums.notification.TipoNotificacao;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.enums.user.StatusUsuario;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.custody.HistoricoCustodia;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.custody.HistoricoCustodiaRepository;
import br.com.delegacia.sgidp.repository.delegation.RepasseRepository;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import br.com.delegacia.sgidp.service.notification.NotificacaoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class RepasseService {
    // papéis que podem solicitar/receber repasses
    private static final Set<String> PAPEIS_RESPONSAVEL = Set.of("ROLE_DELEGADO", "ROLE_POLICIAL", "ROLE_ESTAGIARIO");
    private final RepasseRepository repasseRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final NotificacaoService notificacaoService;
    private final HistoricoCustodiaRepository historicoCustodiaRepository;

    @Transactional 
    public RepasseResponseDto solicitarRepasse(RepasseRequestDto dto, String login) {
        Usuario solicitante = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário autenticado não encontrado"
                )); 
        
        Usuario destinatario = usuarioRepository.findById(dto.idDestinatario())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Destinatário não encontrado."
                ));
        
        Procedimento procedimento = procedimentoRepository.findById(dto.idProcedimento())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Procedimento não encontrado."
                    ));
        
        if (procedimento.getDetentorAtual() == null || !solicitante.getId().equals(procedimento.getDetentorAtual().getId())) {
            throw new RegraNegocioException("O procedimento informado não pertence ao solicitante.");
        }

        if (destinatario.getId().equals(solicitante.getId())) {
            throw new RegraNegocioException("Não é possível repassar um procedimento para si próprio.");
        }

        if (destinatario.getStatusUsuario() != StatusUsuario.APROVADO || !PAPEIS_RESPONSAVEL.contains(destinatario.getRole().getAcesso())) {
            throw new RegraNegocioException(
                    "O destinatário deve ser Delegado, Policial ou Estagiário com cadastro aprovado");
        }

        if (procedimento.getStatusProcedimento() == StatusProcedimento.ARQUIVADO) {
            throw new RegraNegocioException("Não é possível solicitar um repasse para um procedimento arquivado.");
        }

        if (repasseRepository.existsByProcedimentoIdAndStatusRepasse(procedimento.getId(), StatusRepasse.PENDENTE)) {
            throw new RegraNegocioException("Já existe um repasse pendente para esse procedimento.");
        }


        Repasse repasse = new Repasse();
        repasse.setProcedimento(procedimento);
        repasse.setSolicitante(solicitante);
        repasse.setDestinatario(destinatario);
        repasse = repasseRepository.saveAndFlush(repasse);

        notificacaoService.notificar(
        destinatario,
        TipoNotificacao.REPASSE_RECEBIDO,
        "Você recebeu uma solicitação de repasse do procedimento " + procedimento.getTipoProcedimento() + " " + procedimento.getNumeroProcedimento() + "/" + procedimento.getAnoProcedimento() + " de " + solicitante.getNome(),
        repasse.getId()
        );

        return RepasseResponseDto.de(repasse);
    }

    @Transactional 
    public RepasseResponseDto aceitar(Long id, String login) {
        Usuario usuario = usuarioRepository.findByLogin(login).orElseThrow(() -> new RegraNegocioException("Usuário autenticado não encontrado."));
        
        Repasse repasse = repasseRepository.findById(id).orElseThrow(()-> new RecursoNaoEncontradoException("Solicitação de repasse não encontrada."));

        if (!usuario.getId().equals(repasse.getDestinatario().getId())) {
            throw new RegraNegocioException("Você não é o destinatário dessa solicitação de repasse.");
        }

        if (!repasse.getStatusRepasse().equals(StatusRepasse.PENDENTE)) {
            throw new RegraNegocioException("O repasse já foi respondido por outra via");
        }

        repasse.aceitar();

        Procedimento procedimento = repasse.getProcedimento();
        procedimento.setDetentorAtual(usuario);

        historicoCustodiaRepository.findByProcedimentoIdAndDataFimIsNull(procedimento.getId()).ifPresent(HistoricoCustodia::encerrar);

        HistoricoCustodia novaCustodia = new HistoricoCustodia(procedimento, usuario, OrigemHistoricoCustodia.REPASSE, repasse);
        historicoCustodiaRepository.save(novaCustodia);

        notificacaoService.notificar(
            repasse.getSolicitante(), 
            TipoNotificacao.REPASSE_ACEITO,
            "O repasse do procedimento " + procedimento.getTipoProcedimento() + " " + procedimento.getNumeroProcedimento() + "/" + procedimento.getAnoProcedimento() + " foi aceito por " + usuario.getNome() + ".",
            repasse.getId()
        
        );

        return RepasseResponseDto.de(repasse);
    }

    @Transactional 
    public RepasseResponseDto recusar(Long id, RecusarRepasseRequestDto dto, String login) {
        Usuario usuario = usuarioRepository.findByLogin(login).orElseThrow(() -> new RegraNegocioException("Usuário autenticado não encontrado."));
        
        Repasse repasse = repasseRepository.findById(id).orElseThrow(()-> new RecursoNaoEncontradoException("Solicitação de repasse não encontrada."));

        if (!usuario.getId().equals(repasse.getDestinatario().getId())) {
            throw new RegraNegocioException("Você não é o destinatário dessa solicitação de repasse.");
        }

        if (!repasse.getStatusRepasse().equals(StatusRepasse.PENDENTE)) {
            throw new RegraNegocioException("O repasse já foi respondido por outra via");
        }

        repasse.recusar(dto.justificativa().strip());

        Procedimento procedimento = repasse.getProcedimento();

        notificacaoService.notificar(
            repasse.getSolicitante(), 
            TipoNotificacao.REPASSE_RECUSADO,
            "O repasse do procedimento " + procedimento.getTipoProcedimento() + " " + procedimento.getNumeroProcedimento() + "/" + procedimento.getAnoProcedimento() + " foi recusado por " + usuario.getNome() + " com a seguinte justificativa: " + dto.justificativa().strip(),
            repasse.getId()
        
        );

        return RepasseResponseDto.de(repasse);
    }
}
