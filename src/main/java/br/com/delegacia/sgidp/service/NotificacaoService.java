package br.com.delegacia.sgidp.service;

import br.com.delegacia.sgidp.enums.TipoNotificacao;
import br.com.delegacia.sgidp.model.Notificacao;
import br.com.delegacia.sgidp.model.Usuario;
import br.com.delegacia.sgidp.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    // Chamado de dentro de outros services, sempre DEPOIS de salvar o registro de origem
    public void notificar(Usuario destinatario, TipoNotificacao tipo,
                          String mensagem, Long referenciaId){
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuarioDestinatario(destinatario);
        notificacao.setTipoNotificacao(tipo);
        notificacao.setMensagem(mensagem);
        notificacao.setReferenciaId(referenciaId);
        notificacaoRepository.save(notificacao);
    }
}
