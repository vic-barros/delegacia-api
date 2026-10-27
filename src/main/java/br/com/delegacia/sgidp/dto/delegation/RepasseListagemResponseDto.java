package br.com.delegacia.sgidp.dto.delegation;

import java.util.List;

// Página da aba pedida + números das abas "Recebidos (2)" e "Enviados (1)" (pendentes do usuário logado)
public record RepasseListagemResponseDto(
        List<RepasseListagemItemResponseDto> itens,
        int pagina,
        int tamanho,
        long totalItens,
        int totalPaginas,
        long recebidosPendentes,
        long enviadosPendentes
) {
}