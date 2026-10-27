package br.com.delegacia.sgidp.dto.delegation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RepasseRequestDto(

        @NotNull(message = "O destinatário do repasse deve ser informado")
        Long idDestinatario,

        @NotNull(message = "Deve haver um procedimento vinculado à solicitação de repasse")
        Long idProcedimento,

        @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres")
        String observacao

) {
}
