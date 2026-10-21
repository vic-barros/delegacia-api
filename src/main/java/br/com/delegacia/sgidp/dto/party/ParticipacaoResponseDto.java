package br.com.delegacia.sgidp.dto.party;

import br.com.delegacia.sgidp.enums.hearing.StatusOitiva;
import br.com.delegacia.sgidp.enums.party.TipoParte;
import br.com.delegacia.sgidp.model.hearing.Oitiva;
import br.com.delegacia.sgidp.model.hearing.OitivaParte;
import br.com.delegacia.sgidp.model.procedure.Procedimento;

import java.time.LocalDateTime;

// Uma "ligação" da pessoa: o procedimento e a oitiva em que aparece, com o papel para os grafos
public record ParticipacaoResponseDto(
        Long procedimentoId,
        String procedimento,
        String crime,
        TipoParte tipoParte,
        Long oitivaId,
        LocalDateTime dataHoraOitiva,
        StatusOitiva statusOitiva
) {

    public static ParticipacaoResponseDto de(OitivaParte parte) {
        Oitiva oitiva = parte.getOitiva();
        Procedimento procedimento = oitiva.getProcedimento();
        return new ParticipacaoResponseDto(
                procedimento.getId(),
                procedimento.getTipoProcedimento() + " " + procedimento.getNumeroProcedimento() + "/"
                        + procedimento.getAnoProcedimento(),
                procedimento.getCrime(),
                parte.getTipoParte(),
                oitiva.getId(),
                oitiva.getDataHora(),
                oitiva.getStatusOitiva()
        );
    }

}
