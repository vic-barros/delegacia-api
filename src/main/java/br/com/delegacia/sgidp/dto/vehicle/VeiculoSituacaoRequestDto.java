package br.com.delegacia.sgidp.dto.vehicle;

import br.com.delegacia.sgidp.enums.vehicle.SituacaoVeiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// RF27 - Toda mudança de situação exige motivo e vira uma linha no histórico
public record VeiculoSituacaoRequestDto(

        @NotNull(message = "A nova situação deve ser informada")
        SituacaoVeiculo situacaoNova,

        @NotBlank(message = "O motivo deve ser informado")
        @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
        String motivo,

        // true = o usuário confirmou que quer mudar a situação de veículo
        Boolean confirmarReversao
) {
}
