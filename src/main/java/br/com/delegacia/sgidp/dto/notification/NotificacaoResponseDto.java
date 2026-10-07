package br.com.delegacia.sgidp.dto.notification;

import br.com.delegacia.sgidp.enums.notification.TipoNotificacao;
import br.com.delegacia.sgidp.model.notification.Notificacao;

import java.time.LocalDateTime;

public record NotificacaoResponseDto(
        Long id,
        TipoNotificacao tipo,
        String mensagem,
        Boolean lida,
        LocalDateTime dataCriacao,
        Long referenciaId

        // tipo + referenciaId dizem ao front para onde navegar ao clicar (ver TipoNotificacao)
) {

    public static NotificacaoResponseDto de(Notificacao notificacao){
        return new NotificacaoResponseDto(
                notificacao.getId(),
                notificacao.getTipoNotificacao(),
                notificacao.getMensagem(),
                notificacao.getLida(),
                notificacao.getDataCriacao(),
                notificacao.getReferenciaId()
        );
    }
}
