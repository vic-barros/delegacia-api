package br.com.delegacia.sgidp.dto.dashboard;

import br.com.delegacia.sgidp.enums.dashboard.ProximoPasso;

import java.time.LocalDateTime;

// Uma linha da Minha mesa. Tudo é calculado a partir do que já está gravado
public record ProcedimentoMesaResponseDto(
        Long id,
        String procedimento,
        String crime,
        LocalDateTime naPosseDesde,
        long diasNaPosse,
        Long proximaOitivaId,
        LocalDateTime proximaOitiva,
        boolean oitivaAtrasada,
        Long repassePendenteId,
        String repasseDestinatario,
        LocalDateTime ultimaMovimentacao,
        long diasSemMovimentacao,
        boolean parado,
        ProximoPasso proximoPasso,
        String descricaoProximoPasso
) {
}
