package br.com.delegacia.sgidp.dto.user;

import br.com.delegacia.sgidp.model.user.Usuario;

// Resumo para os seletores do front (destinatário do repasse, responsável da oitiva, filtro de detentor):
// sem matrícula, login e status, que só o Admin precisa ver
public record UsuarioResumoResponseDto(
        Long id,
        String nome,
        String role
) {

    public static UsuarioResumoResponseDto de(Usuario usuario) {
        return new UsuarioResumoResponseDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getRole().getAcesso()
        );
    }
}
