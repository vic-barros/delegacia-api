package br.com.delegacia.sgidp.dto.delegation;

import br.com.delegacia.sgidp.dto.user.UsuarioResumoResponseDto;
import br.com.delegacia.sgidp.model.delegation.Repasse;

import java.time.LocalDateTime;

// Repasse aguardando aceite, mostrado no detalhe do procedimento ("Repasse para Ana Lima aguardando").
// O solicitante é sempre o detentor atual, que já vem no procedimento
public record RepassePendenteResponseDto(
        Long id,
        UsuarioResumoResponseDto destinatario,
        LocalDateTime dataSolicitacao
) {

    public static RepassePendenteResponseDto de(Repasse repasse) {
        return new RepassePendenteResponseDto(
                repasse.getId(),
                UsuarioResumoResponseDto.de(repasse.getDestinatario()),
                repasse.getDataSolicitacao()
        );
    }
}
