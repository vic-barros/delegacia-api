package br.com.delegacia.sgidp.enums;

import lombok.Getter;

@Getter
public enum TipoParte {

    VITIMA("Vítima"),
    INVESTIGADO("Investigado"),
    TESTEMUNHA("Testemunha");

    private final String descricao;

    TipoParte(String descricao) {
        this.descricao = descricao;
    }
}
