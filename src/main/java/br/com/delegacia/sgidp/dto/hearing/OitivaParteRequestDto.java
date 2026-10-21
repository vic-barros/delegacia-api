package br.com.delegacia.sgidp.dto.hearing;

import br.com.delegacia.sgidp.enums.party.TipoParte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OitivaParteRequestDto(

        @NotNull(message = "O tipo da parte deve ser informado")
        TipoParte tipoParte,

        @NotBlank(message = "O nome da parte deve ser informado")
        @Size(max = 150, message = "O nome da parte deve ter no máximo 150 caracteres")
        String nomeParte,

        // Opcional: id de uma pessoa já cadastrada (GET /pessoas). Sem ele, a parte fica só com o nome
        Long pessoaId
) {
}
