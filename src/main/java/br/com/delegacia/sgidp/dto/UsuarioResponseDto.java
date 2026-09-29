package br.com.delegacia.sgidp.dto;

import br.com.delegacia.sgidp.enums.StatusUsuario;
import br.com.delegacia.sgidp.model.Usuario;

public record UsuarioResponseDto(
        Long id,
        String nome,
        String matricula,
        String login,
        StatusUsuario status,
        String role
) {

    public static UsuarioResponseDto de(Usuario usuario) {
        return new UsuarioResponseDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getMatricula(),
                usuario.getLogin(),
                usuario.getStatusUsuario(),
                usuario.getRole().getAcesso()
        );
    }
}
