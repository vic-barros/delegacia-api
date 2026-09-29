package br.com.delegacia.sgidp.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErroResponseDto(int status, String mensagem, List<String> detalhes, LocalDateTime dataHora) {

    public ErroResponseDto(int status, String mensagem) {
        this(status, mensagem, List.of(), LocalDateTime.now());
    }

    public ErroResponseDto(int status, String mensagem, List<String> detalhes) {
        this(status, mensagem, detalhes, LocalDateTime.now());
    }
}
