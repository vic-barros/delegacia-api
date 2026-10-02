package br.com.delegacia.sgidp.dto.user;

import br.com.delegacia.sgidp.enums.user.StatusUsuario;
import br.com.delegacia.sgidp.model.user.Usuario;

public record UsuarioResponseDto(
        Long id,
        String nome,
        String matricula,
        String login,
        StatusUsuario status,
        String role,
        String motivoRejeicao
) {

    public static UsuarioResponseDto de(Usuario usuario) {
        return new UsuarioResponseDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getMatricula(),
                usuario.getLogin(),
                usuario.getStatusUsuario(),
                usuario.getRole().getAcesso(),
                usuario.getMotivoRejeicao()
        );
    }
}
