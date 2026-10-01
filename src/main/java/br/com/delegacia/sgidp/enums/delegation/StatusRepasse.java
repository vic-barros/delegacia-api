package br.com.delegacia.sgidp.enums.delegation;

import lombok.Getter;

@Getter
public enum StatusRepasse {

    PENDENTE("Pendente"),
    ACEITO("Aceito"),
    RECUSADO("Recusado");

    private final String descricao;

    StatusRepasse(String descricao) {
        this.descricao = descricao;
    }
}
