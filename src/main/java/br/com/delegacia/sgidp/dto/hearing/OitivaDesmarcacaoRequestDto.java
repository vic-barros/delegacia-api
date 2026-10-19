package br.com.delegacia.sgidp.dto.hearing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OitivaDesmarcacaoRequestDto(

        @NotBlank(message = "O motivo deve ser informado")
        @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
        String motivo
) {
}
