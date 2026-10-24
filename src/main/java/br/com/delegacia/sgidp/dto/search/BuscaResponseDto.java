package br.com.delegacia.sgidp.dto.search;

import br.com.delegacia.sgidp.dto.party.PessoaResumoResponseDto;

import java.util.List;

// Resultado da busca global (Ctrl+K), agrupado como na paleta do front. Cada grupo traz no máximo 10 itens
public record BuscaResponseDto(
        List<ProcedimentoBuscaResponseDto> procedimentos,
        List<VeiculoBuscaResponseDto> veiculos,
        List<PessoaResumoResponseDto> pessoas

) {
}
