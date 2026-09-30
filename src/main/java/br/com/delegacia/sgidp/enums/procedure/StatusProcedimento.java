package br.com.delegacia.sgidp.enums.procedure;

import lombok.Getter;

@Getter
public enum StatusProcedimento {

    EM_ANDAMENTO("Em Andamento"),
    ARQUIVADO("Arquivado");

    private final String descricao;

    StatusProcedimento(String descricao) {
        this.descricao = descricao;
    }

}
