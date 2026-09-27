package br.com.delegacia.sgidp.enums;

import lombok.Getter;

@Getter
public enum SituacaoVeiculo {

    NA_DEPOL("Veículo encontra-se localizado na Delegacia"),
    EM_PATIO("Veículo foi enviado para o Pátio"),
    DEVOLVIDO("Veículo foi devolvido ao proprietário"),
    DESCARTADO("Veículo foi identificado como sucata");

    private final String descricao;

    SituacaoVeiculo(String descricao) {
        this.descricao = descricao;
    }

}
