package br.com.delegacia.sgidp.enums;

public enum TipoParte {

    VITIMA("Vítma"),
    INVESTIGADO("Investigado"),
    TESTEMUNHA("Testemunha");

    private final String descricao;

    TipoParte(String descricao) {
        this.descricao = descricao;
    }
}
