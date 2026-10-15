package br.com.delegacia.sgidp.dto.hearing;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// Remarcar só altera a data/hora; o status continua AGENDADA
public record OitivaRemarcacaoRequestDto(

        @NotNull(message = "A nova data e hora devem ser informadas")
        @Future(message = "A data e hora devem estar no futuro")
        LocalDateTime dataHora,

        Boolean confirmarConflito

) {
}
