package br.com.delegacia.sgidp.controller.user;

import br.com.delegacia.sgidp.dto.user.UsuarioAprovacaoRequestDto;
import br.com.delegacia.sgidp.dto.user.UsuarioCadastroRequestDto;
import br.com.delegacia.sgidp.dto.user.UsuarioResponseDto;
import br.com.delegacia.sgidp.dto.user.UsuarioRejeicaoRequestDto;
import br.com.delegacia.sgidp.enums.user.StatusUsuario;
import br.com.delegacia.sgidp.service.user.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // UC03 - Só Admin (protegido por /usuarios/** no SecurityConfig)
    @GetMapping
    public List<UsuarioResponseDto> listar(@RequestParam(required = false)
                                                   StatusUsuario status) {
        return usuarioService.listar(status);
    }

    @GetMapping("/{id}")
    public UsuarioResponseDto buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @Transactional
    @PatchMapping("/{id}/aprovar")
    public UsuarioResponseDto aprovar(@PathVariable Long id,
                                      @RequestBody(required = false) @Valid
                                              UsuarioAprovacaoRequestDto dados) {
        return usuarioService.aprovar(id, dados);
    }

    @Transactional
    @PatchMapping("/{id}/rejeitar")
    public UsuarioResponseDto rejeitar(@PathVariable Long id, @RequestBody @Valid UsuarioRejeicaoRequestDto dados) {
        return usuarioService.rejeitar(id, dados);
    }

}
