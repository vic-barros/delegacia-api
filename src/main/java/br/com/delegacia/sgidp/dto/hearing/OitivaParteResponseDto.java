package br.com.delegacia.sgidp.dto.hearing;

import br.com.delegacia.sgidp.enums.party.TipoParte;
import br.com.delegacia.sgidp.model.hearing.OitivaParte;

public record OitivaParteResponseDto(Long id, TipoParte tipoParte, String nomeParte) {

    public static OitivaParteResponseDto de(OitivaParte parte){
        return new OitivaParteResponseDto(parte.getId(), parte.getTipoParte(), parte.getNomeParte());
    }
}
