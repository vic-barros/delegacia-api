package br.com.delegacia.sgidp.dto.hearing;

import br.com.delegacia.sgidp.enums.hearing.StatusOitiva;
import br.com.delegacia.sgidp.model.hearing.Oitiva;
import br.com.delegacia.sgidp.model.procedure.Procedimento;

import java.time.LocalDateTime;
import java.util.List;

public record OitivaResponseDto(
        Long id,
        LocalDateTime dataHora,
        StatusOitiva status,
        boolean atrasada,
        Long procedimentoId,
        String procedimento,
        Long responsavelId,
        String responsavel,
        List<OitivaParteResponseDto> partes,
        String motivoCancelamento,
        String cadastradoPor,
        String alteradoPor,
        LocalDateTime dataCadastro
) {
    public static OitivaResponseDto de(Oitiva oitiva) {
        Procedimento procedimento = oitiva.getProcedimento();
        // "Atrasada" não é salva no banco: AGENDADA cuja data já passou (ninguém deu baixa)
        boolean atrasada = oitiva.getStatusOitiva() == StatusOitiva.AGENDADA
                && oitiva.getDataHora().isBefore(LocalDateTime.now());

        // Proteção para alteradoPor porque ele pode ser nulo, se chamar o .getNome em algo nulo dará nullpointer
        String alteradoPor = null;
        if(oitiva.getAlteradoPor() != null){
            alteradoPor = oitiva.getAlteradoPor().getNome();
        }


        return new OitivaResponseDto(
                oitiva.getId(),
                oitiva.getDataHora(),
                oitiva.getStatusOitiva(),
                atrasada,
                procedimento.getId(),
                procedimento.getTipoProcedimento() + " " +
                        procedimento.getNumeroProcedimento() + "/" + procedimento.getAnoProcedimento(),
                oitiva.getResponsavel().getId(),
                oitiva.getResponsavel().getNome(),

                oitiva.getPartes().stream().map(OitivaParteResponseDto::de).toList(),
                oitiva.getMotivoCancelamento(),
                oitiva.getCadastradoPor().getNome(),
                alteradoPor,
                oitiva.getDataCadastro()
        );
    }
}
