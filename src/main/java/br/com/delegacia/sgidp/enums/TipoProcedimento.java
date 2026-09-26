package br.com.delegacia.sgidp.enums;

import lombok.Getter;

@Getter
public enum TipoProcedimento {

    BO("Boletim de Ocorrência"),
    IP("Inquérito Policial"),
    TCO("Termo Circunstanciado de Ocorrência"),
    APF("Auto de Prisão em Flagrante"),
    AIAI("Auto de Investigação de Ato Infracional"),
    AAFAI("Auto de Apreensão em Flagrante de Ato Infracional"),
    ROP("Registro de Ocorrência Policial - PM");

    private final String descricao;

    TipoProcedimento(String descricao) {
        this.descricao = descricao;
    }

}
