package br.com.delegacia.sgidp.enums;

public enum OrigemHistoricoCustodia {

    CADASTRO_INICIAL("Cadastro Inicial"),
    REPASSE("Repasse");

    private final String descricao;

    OrigemHistoricoCustodia(String descricao) {
        this.descricao = descricao;
    }

}
