package br.com.delegacia.sgidp.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(

        @NotBlank(message = "O login deve ser informado")
        String login,

        @NotBlank(message = "A senha deve ser informada")
        String senha
) {
}
