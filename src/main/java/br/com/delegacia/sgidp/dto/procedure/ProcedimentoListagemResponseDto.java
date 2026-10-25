package br.com.delegacia.sgidp.dto.procedure;

import java.util.List;

// Resposta da listagem: a página pedida + os dados para a paginação + as contagens das abas
public record ProcedimentoListagemResponseDto(
        List<ProcedimentoListagemItemResponseDto> itens,
        int pagina,
        int tamanho,
        long totalItens,
        int totalPaginas,
        ContagemAbasResponseDto contagens
) {
}