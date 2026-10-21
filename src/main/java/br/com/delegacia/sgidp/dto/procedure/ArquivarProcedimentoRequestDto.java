package br.com.delegacia.sgidp.dto.procedure;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ArquivarProcedimentoRequestDto (
    @NotBlank(message="O motivo do arquivamento deve ser informado")
    @Min(value=3, message="O motivo deve ter ao menos três caracteres")
    @Max(value=500, message="O motivo deve ter no máximo 500 caracteres")
    String motivo
) {
    
}
