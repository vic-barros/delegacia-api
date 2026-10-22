package br.com.delegacia.sgidp.dto.procedure;

// Detalhe do procedimento (T08): os dados de sempre + o repasse pendente, se houver (null quando não há)
public record ProcedimentoDetalheResponseDto(
        ProcedimentoResponseDto procedimento,
        RepassePendenteResponseDto repassePendente
) {
}
