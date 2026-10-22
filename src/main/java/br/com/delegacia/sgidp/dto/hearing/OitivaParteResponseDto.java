package br.com.delegacia.sgidp.dto.hearing;

import br.com.delegacia.sgidp.enums.party.TipoParte;
import br.com.delegacia.sgidp.model.hearing.OitivaParte;

public record OitivaParteResponseDto(Long id, TipoParte tipoParte, String nomeParte, Long pessoaId) {

    public static OitivaParteResponseDto de(OitivaParte parte){
        // getId() de um relacionamento LAZY não dispara consulta: o id já está na própria linha da parte
        Long pessoaId = parte.getPessoa() != null ? parte.getPessoa().getId() : null;
        return new OitivaParteResponseDto(parte.getId(), parte.getTipoParte(), parte.getNomeParte(), pessoaId);
    }
}