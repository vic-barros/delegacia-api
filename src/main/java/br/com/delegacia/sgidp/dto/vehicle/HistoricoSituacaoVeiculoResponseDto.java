package br.com.delegacia.sgidp.dto.vehicle;

import br.com.delegacia.sgidp.enums.vehicle.SituacaoVeiculo;
import br.com.delegacia.sgidp.model.vehicle.HistoricoSituacaoVeiculo;

import java.time.LocalDateTime;

public record HistoricoSituacaoVeiculoResponseDto(
        Long id,
        SituacaoVeiculo situacaoAnterior,
        SituacaoVeiculo situacaoNova,
        String motivo,
        LocalDateTime dataTransicao,
        String responsavel
) {

    public static HistoricoSituacaoVeiculoResponseDto de(HistoricoSituacaoVeiculo historico){
        return new HistoricoSituacaoVeiculoResponseDto(
                historico.getId(),
                historico.getSituacaoAnterior(),
                historico.getSituacaoNova(),
                historico.getMotivo(),
                historico.getDataTransicao(),
                historico.getResponsavel().getNome()

        );
    }
}
