package br.com.delegacia.sgidp.dto.delegation;

import br.com.delegacia.sgidp.dto.user.UsuarioResumoResponseDto;
import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import br.com.delegacia.sgidp.model.procedure.Procedimento;

import java.time.LocalDateTime;

// Linha da tela de repasses: o procedimento resumido, quem pediu, para quem e o recado do solicitante
public record RepasseListagemItemResponseDto(
        Long id,
        StatusRepasse statusRepasse,
        Long procedimentoId,
        TipoProcedimento tipoProcedimento,
        Long numeroProcedimento,
        Integer anoProcedimento,
        String crime,
        UsuarioResumoResponseDto solicitante,
        UsuarioResumoResponseDto destinatario,
        String observacao,
        String justificativaRecusa,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataResposta
) {

    public static RepasseListagemItemResponseDto de(Repasse repasse) {
        Procedimento procedimento = repasse.getProcedimento();
        return new RepasseListagemItemResponseDto(
                repasse.getId(),
                repasse.getStatusRepasse(),
                procedimento.getId(),
                procedimento.getTipoProcedimento(),
                procedimento.getNumeroProcedimento(),
                procedimento.getAnoProcedimento(),
                procedimento.getCrime(),
                UsuarioResumoResponseDto.de(repasse.getSolicitante()),
                UsuarioResumoResponseDto.de(repasse.getDestinatario()),
                repasse.getObservacao(),
                repasse.getJustificativaRecusa(),
                repasse.getDataSolicitacao(),
                repasse.getDataResposta()
        );
    }
}