package br.com.delegacia.sgidp.controller;

import br.com.delegacia.sgidp.dto.UsuarioCadastroRequestDto;
import br.com.delegacia.sgidp.dto.UsuarioResponseDto;
import br.com.delegacia.sgidp.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDto solicitarCadastro(@RequestBody @Valid UsuarioCadastroRequestDto dadosDto) {
        return usuarioService.solicitarCadastro(dadosDto);
    }
}
