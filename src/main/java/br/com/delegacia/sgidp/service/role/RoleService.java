package br.com.delegacia.sgidp.service.role;

import br.com.delegacia.sgidp.dto.role.RoleResponseDto;
import br.com.delegacia.sgidp.repository.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;
    
    @Transactional(readOnly = true) //Avisa ao hibernate que é só leitura, assim, pula verificações de alteração
    public List<RoleResponseDto> listarPapeisCadastro(){
        return roleRepository.findByAcessoNotOrderByIdAsc("ROLE_ADMIN").stream()
                .map(RoleResponseDto::de) //Transforma a lista de entidades em lista de DTO's
                .toList();
    }
}
