package br.com.delegacia.sgidp.dto.role;

import br.com.delegacia.sgidp.model.role.Role;

public record RoleResponseDto(Long id, String acesso, String descricao) {

    // Converte a entidade em DTO, expondo apenas o que o front precisa
    // "de" é um método de fábrica estático que transforma a entidade em DTO. Com ele, a conversão fica num lugar só. No service você escreve RoleResponseDto.de(role), em vez de repetir o new RoleResponseDto(...)
    public static RoleResponseDto de(Role role){
        return new RoleResponseDto(role.getId(), role.getAcesso(), role.getDescricao());
    }
}
