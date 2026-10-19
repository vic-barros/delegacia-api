package br.com.delegacia.sgidp.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// UC13 - Dados editáveis pelo Admin. O login não entra: é a identidade do usuário no token
public record UsuarioEdicaoRequestDto(

        @NotBlank(message = "O nome deve ser informado")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "A matrícula deve ser informada")
        @Size(max = 50, message = "A matrícula deve ter no máximo 50 caracteres")
        String matricula,

        @NotNull(message = "O papel deve ser informado")
        Long roleId

) {
}
