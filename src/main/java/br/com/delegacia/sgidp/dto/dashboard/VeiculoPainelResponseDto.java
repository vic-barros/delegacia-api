package br.com.delegacia.sgidp.dto.dashboard;

// Card "Veículos nesta delegacia": os que estão fisicamente na DEPOL (situação NA_DEPOL).
// semPericia e periciaEmAndamento são partes do total: os que ainda pedem providência de perícia
public record VeiculoPainelResponseDto(
        long total,
        long semPericia,
        long periciaEmAndamento
) {
}
