package br.com.delegacia.sgidp.dto.dashboard;

import br.com.delegacia.sgidp.model.delegation.Repasse;
import br.com.delegacia.sgidp.model.procedure.Procedimento;

import java.time.LocalDateTime;

public record RepassePainelResponseDto(
        Long id,
        Long procedimentoId,
        String procedimento,
        String crime,
        String solicitante,
        String destinatario,
        LocalDateTime dataSolicitacao
) {
    public static RepassePainelResponseDto de(Repasse repasse) {
        Procedimento procedimento = repasse.getProcedimento();
        return new RepassePainelResponseDto(
                repasse.getId(),
                procedimento.getId(),
                procedimento.getTipoProcedimento() + " " + procedimento.getNumeroProcedimento() + "/"
                        + procedimento.getAnoProcedimento(),
                procedimento.getCrime(),
                repasse.getSolicitante().getNome(),
                repasse.getDestinatario().getNome(),
                repasse.getDataSolicitacao()
        );
    }
}
