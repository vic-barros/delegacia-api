package br.com.delegacia.sgidp.dto.agent;

import java.util.List;

import br.com.delegacia.sgidp.dto.procedure.ProcedimentoResponseDto;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import br.com.delegacia.sgidp.model.hearing.Oitiva;
import br.com.delegacia.sgidp.model.procedure.HistoricoStatusProcedimento;
import br.com.delegacia.sgidp.model.vehicle.HistoricoSituacaoVeiculo;
import br.com.delegacia.sgidp.model.vehicle.Veiculo;

public record ResumoProcedimentoDto (
    ProcedimentoResponseDto procedimento,
    List<HistoricoStatusProcedimento> historicoStatusProcedimento,
    List<Repasse> historicoRepasses,
    List<Oitiva> historicoOitivas,
    List<Veiculo> listaVeiculos,
    List<HistoricoSituacaoVeiculo> historicoSituacaoVeiculo
) {
    public static ResumoProcedimentoDto de(ProcedimentoResponseDto procedimento,
                                            List<HistoricoStatusProcedimento> historicoStatusProcedimento,
                                            List<Repasse> historicoRepasses,
                                            List<Oitiva> historOitivas,
                                            List<Veiculo> listaVeiculos,
                                            List<HistoricoSituacaoVeiculo> historicoSituacaoVeiculo) {
        return new ResumoProcedimentoDto(procedimento, historicoStatusProcedimento, historicoRepasses, historOitivas, listaVeiculos, historicoSituacaoVeiculo);
    }
}
