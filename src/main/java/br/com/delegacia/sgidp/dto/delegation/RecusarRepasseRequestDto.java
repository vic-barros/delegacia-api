package br.com.delegacia.sgidp.dto.delegation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecusarRepasseRequestDto (
    @NotBlank(message="A justificativa da recusa não pode estar vazia")
    @Size(min=3, max=500, message="A justificativa deve conter de 3 a 500 caracteres")
    String justificativa
) {
    
}
