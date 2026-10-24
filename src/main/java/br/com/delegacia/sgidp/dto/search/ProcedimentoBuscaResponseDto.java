package br.com.delegacia.sgidp.dto.search;

import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.model.procedure.Procedimento;

// Uma linha do grupo "Procedimentos" da busca global: o suficiente para reconhecer e abrir o caso
public record ProcedimentoBuscaResponseDto(
        Long id,
        String identificacao,
        String crime,
        StatusProcedimento status,
        String detentor
) {

    public static ProcedimentoBuscaResponseDto de(Procedimento procedimento) {
        return new ProcedimentoBuscaResponseDto(
                procedimento.getId(),
                procedimento.getTipoProcedimento() + " " +
                        procedimento.getNumeroProcedimento() + "/"
                        + procedimento.getAnoProcedimento(),
                procedimento.getCrime(),
                procedimento.getStatusProcedimento(),
                procedimento.getDetentorAtual().getNome()

        );
    }
}
