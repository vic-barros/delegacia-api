package br.com.delegacia.sgidp.enums.notification;

import lombok.Getter;

@Getter
public enum TipoNotificacao {

    OITIVA_PROXIMA("Oitiva próxima"),
    REPASSE_RECEBIDO("Repasse recebido"),
    REPASSE_ACEITO("Repasse aceito"),
    REPASSE_RECUSADO("Repasse recusado"),
    CADASTRO_PENDENTE("Cadastro pendente de aprovação");

    private final String descricao;

    TipoNotificacao(String descricao) {
        this.descricao = descricao;
    }
}