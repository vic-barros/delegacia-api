package br.com.delegacia.sgidp.dto.vehicle;

import br.com.delegacia.sgidp.enums.examination.StatusPericia;
import br.com.delegacia.sgidp.enums.vehicle.SituacaoVeiculo;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.vehicle.Veiculo;

public record VeiculoResponseDto(
        Long id,
        String lacre,
        String tipoVeiculo,
        String marca,
        String modelo,
        String cor,
        String placa,
        String chassi,
        String motor,
        String caracteristicasVisuais,
        String observacoes,
        StatusPericia statusPericia,
        SituacaoVeiculo situacao,
        Long procedimentoId,
        String procedimento
) {

    public static VeiculoResponseDto de(Veiculo veiculo) {
        Procedimento procedimento = veiculo.getProcedimento();
        return new VeiculoResponseDto(
                veiculo.getId(),
                veiculo.getLacre(),
                veiculo.getTipoVeiculo(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getCor(),
                veiculo.getPlaca(),
                veiculo.getChassi(),
                veiculo.getMotor(),
                veiculo.getCaracteristicasVisuais(),
                veiculo.getObservacoes(),
                veiculo.getStatusPericia(),
                veiculo.getSituacaoVeiculo(),
                procedimento.getId(),
                // Ex.: "IP 123/2026" (identifica sem expor nomes de partes)
                procedimento.getTipoProcedimento() + " " + procedimento.getNumeroProcedimento()
                        + "/" + procedimento.getAnoProcedimento()
        );
    }
}
