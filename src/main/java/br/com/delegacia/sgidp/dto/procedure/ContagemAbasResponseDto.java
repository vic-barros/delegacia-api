package br.com.delegacia.sgidp.dto.procedure;

import br.com.delegacia.sgidp.repository.procedure.ContagemAbasProcedimento;

// Números das abas da listagem ("Em andamento (12)"), já com os filtros aplicados
public record ContagemAbasResponseDto(
        long emAndamento,
        long arquivados,
        long remetidos,
        long todos
) {

    public static ContagemAbasResponseDto de(ContagemAbasProcedimento contagem) {
        return new ContagemAbasResponseDto(
                contagem.getEmAndamento(),
                contagem.getArquivados(),
                contagem.getRemetidos(),
                contagem.getTodos()
        );
    }
}
