package br.com.delegacia.sgidp.dto.procedure;

import java.time.LocalDate;

import br.com.delegacia.sgidp.dto.user.UsuarioResumoResponseDto;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;


import br.com.delegacia.sgidp.model.procedure.Procedimento;

public record ProcedimentoResponseDto(
        Long id,
        TipoProcedimento tipoProcedimento,
        Long numeroProcedimento,
        Integer anoProcedimento,
        String crime,
        LocalDate dataAbertura,
        LocalDate dataRemessaFinal,
        String protocoloRemessaFinal,
        StatusProcedimento statusProcedimento,
        UsuarioResumoResponseDto detentorAtual
) {
    public static ProcedimentoResponseDto de(Procedimento procedimento) {
        return new ProcedimentoResponseDto(
                procedimento.getId(),
                procedimento.getTipoProcedimento(),
                procedimento.getNumeroProcedimento(),
                procedimento.getAnoProcedimento(),
                procedimento.getCrime(),
                procedimento.getDataAbertura(),
                procedimento.getDataRemessaFinal(),
                procedimento.getProtocoloRemessaFinal(),
                procedimento.getStatusProcedimento(),
                UsuarioResumoResponseDto.de(procedimento.getDetentorAtual())
        );
    }
}
