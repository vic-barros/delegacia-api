package br.com.delegacia.sgidp.dto.procedure;

import br.com.delegacia.sgidp.dto.user.UsuarioResumoResponseDto;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.model.procedure.HistoricoStatusProcedimento;

import java.time.LocalDateTime;

// Uma mudança de status (cadastro, arquivamento, reabertura) com o motivo e quem fez
public record HistoricoStatusResponseDto(
        Long id,
        StatusProcedimento statusAnterior,
        StatusProcedimento statusNovo,
        String motivo,
        UsuarioResumoResponseDto responsavel,
        LocalDateTime dataTransicao
) {

    public static HistoricoStatusResponseDto de(HistoricoStatusProcedimento historico) {
        return new HistoricoStatusResponseDto(
                historico.getId(),
                historico.getStatusAnterior(),
                historico.getStatusNovo(),
                historico.getMotivo(),
                UsuarioResumoResponseDto.de(historico.getResponsavel()),
                historico.getDataTransicao()
        );
    }

}
