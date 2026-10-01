package br.com.delegacia.sgidp.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCadastroRequestDto(

        @NotBlank(message = "O nome deve ser informado")
        @Size(max = 150, message = "O nome deve ter no máxima 255 caracteres")
        String nome,

        @NotBlank(message = "A matrícula deve ser informada")
        @Size(max = 50, message = "A matrícula deve ter no máximo 50 caracteres")
        String matricula,

        @NotBlank(message = "O login deve ser informado")
        @Size(min = 3, max = 50, message = "O login deve ter entre 3 e 50 caracteres")
        String login,

        @NotBlank(message = "A senha deve ser informada")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String senha,

        @NotNull(message = "O papel deve ser informado")
        Long roleId
) {
}
