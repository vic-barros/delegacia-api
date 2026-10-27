package br.com.delegacia.sgidp.dto.auth;

import br.com.delegacia.sgidp.model.user.Usuario;

public record UsuarioLogadoResponseDto(Long id, String login, String nome,
                                       String matricula, String role) {

    public static UsuarioLogadoResponseDto de(Usuario usuario) {
        return new UsuarioLogadoResponseDto(
                usuario.getId(),
                usuario.getLogin(),
                usuario.getNome(),
                usuario.getMatricula(),
                usuario.getRole().getAcesso()
        );
    }

}
