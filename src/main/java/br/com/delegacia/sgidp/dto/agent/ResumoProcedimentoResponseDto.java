package br.com.delegacia.sgidp.dto.agent;

public record ResumoProcedimentoResponseDto (
    String resposta
) {
    public static ResumoProcedimentoResponseDto de(String resposta) {
        return new ResumoProcedimentoResponseDto(
            resposta
        );
    }
}
