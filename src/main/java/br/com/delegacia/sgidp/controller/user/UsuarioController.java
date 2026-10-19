package br.com.delegacia.sgidp.controller.user;

import br.com.delegacia.sgidp.dto.user.*;
import br.com.delegacia.sgidp.enums.user.StatusUsuario;
import br.com.delegacia.sgidp.service.user.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @PatchMapping("/{id}/aprovar")
    public UsuarioResponseDto aprovar(@PathVariable Long id,
                                      @RequestBody(required = false) @Valid
                                      UsuarioAprovacaoRequestDto dados) {
        return usuarioService.aprovar(id, dados);
    }

    @PatchMapping("/{id}/rejeitar")
    public UsuarioResponseDto rejeitar(@PathVariable Long id, @RequestBody @Valid UsuarioRejeicaoRequestDto dados) {
        return usuarioService.rejeitar(id, dados);
    }

    @PutMapping("/{id}")
    public UsuarioResponseDto editar(@PathVariable Long id,
                                     @RequestBody @Valid UsuarioEdicaoRequestDto
                                             dados, @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.editar(id, dados, jwt.getSubject());
    }

    @PatchMapping("/{id}/desativar")
    public UsuarioResponseDto desativar(@PathVariable Long id,
                                        @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.desativar(id, jwt.getSubject());
    }

    @PatchMapping("/{id}/reativar")
    public UsuarioResponseDto reativar(@PathVariable Long id) {
        return usuarioService.reativar(id);
    }

    // UC14 - 204 No Content: a senha nunca volta na resposta
    @PatchMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinirSenha(@PathVariable Long id,
                               @RequestBody @Valid
                               UsuarioRedefinicaoSenhaRequestDto dados) {
        usuarioService.redefinirSenha(id, dados);
    }


}
