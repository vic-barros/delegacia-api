package br.com.delegacia.sgidp.dto.procedure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;

public record ProcedimentoRequestDto(

    @NotNull(message="O tipo deve ser informado")
    TipoProcedimento tipoProcedimento,

    @NotNull(message="O número do procedimento deve ser informado")
    @Positive(message="o número do procedimento não pode ser zero ou negativo")
    Long numeroProcedimento,

    @NotNull(message="O ano do procedimento deve ser informado")
    @Min(value=1900, message="O ano do procedimento deve ser válido")
    Integer anoProcedimento,

    @NotBlank(message="O crime deve ser informado")
    @Size(min=3, max=150, message="O crime deve ter entre 3 e 150 caracteres")
    String crime,

    @NotNull(message="A data de abertura do procedimento deve ser informada")
    LocalDate dataAbertura
    
) {

}
