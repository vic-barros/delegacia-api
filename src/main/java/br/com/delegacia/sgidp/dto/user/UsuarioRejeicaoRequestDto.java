package br.com.delegacia.sgidp.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRejeicaoRequestDto(

        @NotBlank(message = "O motivo da rejeição deve ser informado")
        @Size(max = 500, message = "O motivoda rejeição do cadastro deve ter no máximo 500 caracteres")
        String motivo
) {
}
