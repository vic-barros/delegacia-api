package br.com.delegacia.sgidp.dto.dashboard;

import br.com.delegacia.sgidp.dto.hearing.OitivaResponseDto;

import java.util.List;

public record PainelResponseDto(
        List<OitivaResponseDto> oitivasHoje,
        List<RepassePainelResponseDto> repassesRecebidos,
        List<RepassePainelResponseDto> repassesEnviados,
        VeiculoPainelResponseDto veiculos,
        List<ProcedimentoMesaResponseDto> procedimentosParados
) {
}
