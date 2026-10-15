package br.com.delegacia.sgidp.dto.hearing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Edita responsável e partes (a lista enviada substitui a anterior). A data muda por /remarcar
public record OitivaEdicaoRequestDto(

        @NotNull(message = "O responsável deve ser informado")
        Long responsavelId,

        @NotEmpty(message = "A oitiva deve ter ao menos uma parte")
        List<@Valid OitivaParteRequestDto> partes,

        Boolean confirmarConflito
) {
}
