package br.com.delegacia.sgidp.enums;

import lombok.Getter;

@Getter
public enum StatusOitiva {

    AGENDADA("Agendada"),
    CONCLUIDA("Concluída"),
    DESMARCADA("Desmarcada"),
    REMARCADA ("Remarcada");

    private final String descricao;

    StatusOitiva(String descricao) {
        this.descricao = descricao;
    }
}
