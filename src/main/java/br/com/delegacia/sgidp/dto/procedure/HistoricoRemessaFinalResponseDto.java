package br.com.delegacia.sgidp.dto.procedure;

import br.com.delegacia.sgidp.dto.user.UsuarioResumoResponseDto;
import br.com.delegacia.sgidp.model.procedure.HistoricoRemessaFinal;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HistoricoRemessaFinalResponseDto(
        Long id,
        LocalDate dataRemessaAnterior,
        String protocoloAnterior,
        LocalDate dataRemessaNova,
        String protocoloNovo,
        LocalDateTime dataRegistro,
        UsuarioResumoResponseDto responsavel
) {
    public static HistoricoRemessaFinalResponseDto de(HistoricoRemessaFinal h) {
        return new HistoricoRemessaFinalResponseDto(
                h.getId(),
                h.getDataRemessaAnterior(),
                h.getProtocoloAnterior(),
                h.getDataRemessaNova(),
                h.getProtocoloNovo(),
                h.getDataRegistro(),
                UsuarioResumoResponseDto.de(h.getResponsavel())
        );
    }
}