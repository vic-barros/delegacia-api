package br.com.delegacia.sgidp.dto.search;

import br.com.delegacia.sgidp.enums.vehicle.SituacaoVeiculo;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.vehicle.Veiculo;

// Uma linha do grupo "Veículos" da busca global
public record VeiculoBuscaResponseDto(
        Long id,
        String lacre,
        String descricao,
        String placa,
        SituacaoVeiculo situacao,
        String procedimento
) {

    public static VeiculoBuscaResponseDto de(Veiculo veiculo) {
        Procedimento procedimento = veiculo.getProcedimento();
        return new VeiculoBuscaResponseDto(
                veiculo.getId(),
                veiculo.getLacre(),
                veiculo.getTipoVeiculo() + " " + veiculo.getMarca() + " " + veiculo.getModelo() + " - " + veiculo.getCor(),
                veiculo.getPlaca(),
                veiculo.getSituacaoVeiculo(),
                procedimento.getTipoProcedimento() + " " + procedimento.getNumeroProcedimento() + "/"
                        + procedimento.getAnoProcedimento()
        );
    }
}
