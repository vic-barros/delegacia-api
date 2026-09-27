package br.com.delegacia.sgidp.enums;

public enum OrigemHistoricoCustodia {

    CADASTRO_INICIAL("Data do Cadastro Inicial do Procedimento"),
    REPASSE("Data do Repasse do Procedimento");

    private final String descricao;

    OrigemHistoricoCustodia(String descricao) {
        this.descricao = descricao;
    }

}
