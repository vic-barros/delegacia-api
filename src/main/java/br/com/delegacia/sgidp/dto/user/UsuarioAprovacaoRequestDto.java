package br.com.delegacia.sgidp.dto.user;

// Corpo opcional da aprovação: se roleId vier, o Admin troca o papel pedido no cadastro
public record UsuarioAprovacaoRequestDto(Long roleId) {
}
