package br.com.delegacia.sgidp.dto.procedure;

import br.com.delegacia.sgidp.dto.user.UsuarioResumoResponseDto;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;
import br.com.delegacia.sgidp.model.procedure.Procedimento;

import java.time.LocalDate;

// Uma linha da tabela de procedimentos (T06)
public record ProcedimentoListagemItemResponseDto(
        Long id,
        TipoProcedimento tipoProcedimento,
        Long numeroProcedimento,
        Integer anoProcedimento,
        String crime,
        LocalDate dataAbertura,
        StatusProcedimento statusProcedimento,
        boolean remetido,
        UsuarioResumoResponseDto detentorAtual,
        boolean repassePendente
) {

    public static ProcedimentoListagemItemResponseDto de(Procedimento procedimento, boolean repassePendente) {
        return new ProcedimentoListagemItemResponseDto(
                procedimento.getId(),
                procedimento.getTipoProcedimento(),
                procedimento.getNumeroProcedimento(),
                procedimento.getAnoProcedimento(),
                procedimento.getCrime(),
                procedimento.getDataAbertura(),
                procedimento.getStatusProcedimento(),
                procedimento.getDataRemessaFinal() != null,
                UsuarioResumoResponseDto.de(procedimento.getDetentorAtual()),
                repassePendente
        );
    }
}