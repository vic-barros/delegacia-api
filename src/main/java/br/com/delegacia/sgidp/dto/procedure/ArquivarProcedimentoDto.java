package br.com.delegacia.sgidp.dto.procedure;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ArquivarProcedimentoDto (
    @NotBlank(message="O motivo do arquivamento deve ser informado")
    String motivo
) {
    
}
