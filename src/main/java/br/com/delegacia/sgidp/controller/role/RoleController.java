package br.com.delegacia.sgidp.controller.role;


import br.com.delegacia.sgidp.dto.role.RoleResponseDto;
import br.com.delegacia.sgidp.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // Rota pública: papéis que podem ser escolhidos no formulário de cadastro (sem Admin)
    @GetMapping("/cadastro")
    public List<RoleResponseDto> listarPapeisCadastro() {
        return roleService.listarPapeisCadastro();
    }

}
