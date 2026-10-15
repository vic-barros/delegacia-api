package br.com.delegacia.sgidp.dto.delegation;
import jakarta.validation.constraints.NotNull;

public record RepasseRequestDto (

    @NotNull(message="O destinatário do repasse deve ser informado")
    Long idDestinatario,

    @NotNull(message="Deve haver um procedimento vinculado à solicitação de repasse")
    Long idProcedimento

){}
