package br.com.delegacia.sgidp.dto.hearing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

// UC - Agendar oitiva. cadastradoPor vem do token, nunca do formulário
public record OitivaAgendamentoRequestDto(

        @NotNull(message = "O procedimento deve ser informado")
        Long procedimentoId,

        @NotNull(message = "O responsável deve ser informado")
        Long responsavelId,

        @NotNull(message = "A data e hora devem ser informadas")
        @Future(message = "A data e hora devem estar no futuro")
        LocalDateTime dataHora,

        // @Valid valida cada parte da lista (tipo e nome)
        @NotEmpty(message = "A oitiva deve ter ao menos uma parte")
        List<@Valid OitivaParteRequestDto> partes,

        // true = o usuário viu o aviso de outra oitiva no mesmo horário e quer agendar mesmo assim
        Boolean confirmarConflito
) {
}
