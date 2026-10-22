package br.com.delegacia.sgidp.dto.procedure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArquivarProcedimentoRequestDto (
    @NotBlank(message="O motivo do arquivamento deve ser informado")
    @Size(min = 3, max = 500, message = "O motivo deve ter entre 3 e 500 caracteres")
    String motivo
) {

}
