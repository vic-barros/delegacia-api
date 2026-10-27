package br.com.delegacia.sgidp.dto.delegation;

import java.time.LocalDateTime;

import br.com.delegacia.sgidp.dto.procedure.ProcedimentoResponseDto;
import br.com.delegacia.sgidp.dto.user.UsuarioResponseDto;
import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.model.delegation.Repasse;


public record RepasseResponseDto(

        Long id,
        StatusRepasse statusRepasse,
        String observacao,
        String justificativaRecusa,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataResposta,
        ProcedimentoResponseDto procedimento,
        UsuarioResponseDto solicitante,
        UsuarioResponseDto destinatario


) {
    public static RepasseResponseDto de(Repasse repasse) {
        return new RepasseResponseDto(
                repasse.getId(),
                repasse.getStatusRepasse(),
                repasse.getObservacao(),
                repasse.getJustificativaRecusa(),
                repasse.getDataSolicitacao(),
                repasse.getDataResposta(),
                ProcedimentoResponseDto.de(repasse.getProcedimento()),
                UsuarioResponseDto.de(repasse.getSolicitante()),
                UsuarioResponseDto.de(repasse.getDestinatario())
        );
    }
}
