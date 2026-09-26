package br.com.delegacia.sgidp.enums;

public enum StatusUsuario {

    PENDENTE("Ativo"),
    APROVADO("Aprovado"),
    REJEITADO("Rejeitado"),
    DESATIVADO("Desativado");

    private final String descricao;

    StatusUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
