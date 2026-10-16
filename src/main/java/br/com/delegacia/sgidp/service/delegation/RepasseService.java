package br.com.delegacia.sgidp.service.delegation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.delegacia.sgidp.dto.delegation.RepasseRequestDto;
import br.com.delegacia.sgidp.dto.delegation.RepasseResponseDto;
import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.enums.notification.TipoNotificacao;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.delegation.RepasseRepository;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import br.com.delegacia.sgidp.service.notification.NotificacaoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class RepasseService {
    private final RepasseRepository repasseRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final NotificacaoService notificacaoService;

    @Transactional 
    public RepasseResponseDto solicitarRepasse(RepasseRequestDto dto, String login) {
        Usuario solicitante = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário autenticado não encontrado"
                )); 
        
        Usuario destinatario = usuarioRepository.findById(dto.idDestinatario())
                    .orElseThrow(() -> new RegraNegocioException(
                        "Destinatário não encontrado."
                ));
        
        Procedimento procedimento = procedimentoRepository.findById(dto.idProcedimento())
                    .orElseThrow(() -> new RegraNegocioException(
                                "Procedimento não encontrado."
                    ));
        
        if (procedimento.getDetentorAtual() == null || !solicitante.getId().equals(procedimento.getDetentorAtual().getId())) {
            throw new RegraNegocioException("O procedimento informado não pertence ao solicitante.");
        }

        if (destinatario.getId().equals(solicitante.getId())) {
            throw new RegraNegocioException("Não é possível repassar um procimento para si próprio.");
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
        "Você recebeu uma solicitação de repasse do procedimento " + procedimento.getNumeroProcedimento(),
        repasse.getId()
        );

        return RepasseResponseDto.de(repasse);
    }
}
