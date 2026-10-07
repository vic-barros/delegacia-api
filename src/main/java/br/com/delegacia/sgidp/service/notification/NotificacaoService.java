package br.com.delegacia.sgidp.service.notification;

import br.com.delegacia.sgidp.dto.notification.ContagemNotificacoesResponseDto;
import br.com.delegacia.sgidp.dto.notification.NotificacaoResponseDto;
import br.com.delegacia.sgidp.enums.notification.TipoNotificacao;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.model.notification.Notificacao;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.notification.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    // Chamado de dentro de outros services, sempre DEPOIS de salvar o registro de origem
    public void notificar(Usuario destinatario, TipoNotificacao tipo,
                          String mensagem, Long referenciaId) {
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuarioDestinatario(destinatario);
        notificacao.setTipoNotificacao(tipo);
        notificacao.setMensagem(mensagem);
        notificacao.setReferenciaId(referenciaId);
        notificacaoRepository.save(notificacao);
    }

    // Lista as notificações de quem está logado; lida == null traz todas
    @Transactional(readOnly = true)
    public List<NotificacaoResponseDto> listar(String login, Boolean lida) {
        List<Notificacao> notificacoes;

        if (lida == null) {
            notificacoes = notificacaoRepository.findByUsuarioDestinatarioLoginOrderByDataCriacaoDesc(login);
        } else {
            notificacoes = notificacaoRepository.findByUsuarioDestinatarioLoginAndLidaOrderByDataCriacaoDesc(login, lida);
        }

        return notificacoes.stream()
                .map(NotificacaoResponseDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContagemNotificacoesResponseDto contarNaoLidas(String login) {
        return new ContagemNotificacoesResponseDto(
                notificacaoRepository.countByUsuarioDestinatarioLoginAndLidaFalse(login));
    }

    @Transactional
    public void marcarComoLida(Long id, String login) {
        Notificacao notificacao = notificacaoRepository.findByIdAndUsuarioDestinatarioLogin(id, login)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada!"));
        notificacao.setLida(true);
    }

    @Transactional
    public void marcarTodasComoLidas(String login) {
        notificacaoRepository.findByUsuarioDestinatarioLoginAndLidaOrderByDataCriacaoDesc(login, false)
                .forEach(notificacao -> notificacao.setLida(true));
    }

    // Quando o assunto é resolvido (ex.: cadastro aprovado/rejeitado), o aviso sai para todos os destinatários
    @Transactional
    public void marcarComoLidas(TipoNotificacao tipo, Long referenciaId) {
        notificacaoRepository.findByTipoNotificacaoAndReferenciaIdAndLidaFalse(tipo, referenciaId)
                .forEach(notificacao -> notificacao.setLida(true));
    }


}
