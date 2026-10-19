package br.com.delegacia.sgidp.dto.vehicle;

import br.com.delegacia.sgidp.enums.examination.StatusPericia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VeiculoEdicaoRequestDto(
        @NotBlank(message = "O tipo do veículo deve ser informado")
        @Size(max = 50, message = "O tipo do veículo deve ter no máximo 50 caracteres")
        String tipoVeiculo,

        @NotBlank(message = "A marca deve ser informada")
        @Size(max = 50, message = "A marca deve ter no máximo 50 caracteres")
        String marca,

        @NotBlank(message = "O modelo deve ser informado")
        @Size(max = 80, message = "O modelo deve ter no máximo 80 caracteres")
        String modelo,

        @NotBlank(message = "A cor deve ser informada")
        @Size(max = 30, message = "A cor deve ter no máximo 30 caracteres")
        String cor,

        @Size(max = 10, message = "A placa deve ter no máximo 10 caracteres")
        String placa,

        @Size(max = 20, message = "O chassi deve ter no máximo 20 caracteres")
        String chassi,

        @Size(max = 20, message = "O motor deve ter no máximo 20 caracteres")
        String motor,

        @Size(max = 2000, message = "As características visuais devem ter no máximo 2000 caracteres")
        String caracteristicasVisuais,

        @Size(max = 2000, message = "As observações devem ter no máximo 2000 caracteres")
        String observacoes,

        @NotNull(message = "O status da perícia deve ser informado")
        StatusPericia statusPericia,

        Boolean confirmarDuplicidade
) {
}
