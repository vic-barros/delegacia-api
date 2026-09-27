package br.com.delegacia.sgidp.enums;

import lombok.Getter;

@Getter
public enum StatusPericia {

    SEM_PERICIA("Sem Perícia"),
    PERICIA_EM_ANDAMENTO("Perícia em Andamento"),
    PERICIA_CONCLUIDA("Perícia Concluída"),
    NAO_PRECISA_PERICIA("Não Precisa de Perícia");

    private final String descricao;

    StatusPericia(String descricao) {
        this.descricao = descricao;
    }
}
