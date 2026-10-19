package br.com.delegacia.sgidp.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// UC14 - Senha provisória definida pelo Admin e repassada pessoalmente ao servidor
public record UsuarioRedefinicaoSenhaRequestDto(

        @NotBlank(message = "A nova senha deve ser informada")
        @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres")
        String novaSenha
) {
}
