package br.com.delegacia.sgidp.dto.auth;

public record TokenResponseDto(String token, String tipo) {

    public TokenResponseDto(String token) {
        this(token, "Bearer");
    }
}
