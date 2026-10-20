package br.com.delegacia.sgidp.dto.dashboard;

import java.util.List;

// Contagens = números dos chips da tela (Todos, Com oitiva marcada, Repasse em andamento, Parados)
public record MinhaMesaResponseDto(

        long total,
        long comOitivaMarcada,
        long repasseEmAndamento,
        long parados,
        List<ProcedimentoMesaResponseDto> procedimentos
) {
}
