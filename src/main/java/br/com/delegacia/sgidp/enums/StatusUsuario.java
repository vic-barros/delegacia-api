package br.com.delegacia.sgidp.enums;

import lombok.Getter;

@Getter
public enum StatusUsuario {

    PENDENTE("Pendente"),
    APROVADO("Aprovado"),
    REJEITADO("Rejeitado"),
    DESATIVADO("Desativado");

    private final String descricao;

    StatusUsuario(String descricao) {
        this.descricao = descricao;
    }
}
