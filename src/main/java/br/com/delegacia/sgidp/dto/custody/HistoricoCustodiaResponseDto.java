package br.com.delegacia.sgidp.dto.custody;

import br.com.delegacia.sgidp.dto.user.UsuarioResumoResponseDto;
import br.com.delegacia.sgidp.enums.custody.OrigemHistoricoCustodia;
import br.com.delegacia.sgidp.model.custody.HistoricoCustodia;

import java.time.LocalDateTime;

// Uma posse do procedimento: quem teve, de quando até quando (dataFim null = posse atual) e como recebeu
public record HistoricoCustodiaResponseDto(
        Long id,
        UsuarioResumoResponseDto usuario,
        OrigemHistoricoCustodia origem,
        Long repasseOrigemId,
        LocalDateTime dataInicio,
        LocalDateTime dataFim
) {

    public static HistoricoCustodiaResponseDto de(HistoricoCustodia historico) {
        Long repasseOrigemId = null;
        if (historico.getRepasseOrigem() != null) {
            repasseOrigemId = historico.getRepasseOrigem().getId();
        }
        return new HistoricoCustodiaResponseDto(
                historico.getId(),
                UsuarioResumoResponseDto.de(historico.getUsuario()),
                historico.getOrigem(),
                repasseOrigemId,
                historico.getDataInicio(),
                historico.getDataFim()
        );
    }
}
