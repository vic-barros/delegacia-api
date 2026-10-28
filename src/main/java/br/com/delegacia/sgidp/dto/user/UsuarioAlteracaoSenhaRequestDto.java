package br.com.delegacia.sgidp.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioAlteracaoSenhaRequestDto(

        @NotBlank(message = "A senha atual deve ser informada")
        String senhaAtual,

        @NotBlank(message = "A nova senha deve ser informada")
        @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres")
        String novaSenha
) {
}
