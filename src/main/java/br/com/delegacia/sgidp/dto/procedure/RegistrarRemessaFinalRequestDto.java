package br.com.delegacia.sgidp.dto.procedure;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record RegistrarRemessaFinalRequestDto (
    @NotNull(message = "A data da remessa não pode estar vazia")
    @PastOrPresent(message = "A data da remessa não pode estar no futuro")
    LocalDate data,

    @NotBlank(message="O número do protocolo não pode estar vazio")
    @Size(min=3, max=255, message="O número do protocolo deve ter entre 3 e 255 caracteres")
    String numeroProtocolo,

    Boolean confirmarSobrescrita
) {
    
}
