package br.com.delegacia.sgidp.controller.auth;

import br.com.delegacia.sgidp.dto.auth.LoginRequestDto;
import br.com.delegacia.sgidp.dto.auth.TokenResponseDto;
import br.com.delegacia.sgidp.dto.auth.UsuarioLogadoResponseDto;
import br.com.delegacia.sgidp.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponseDto login(@RequestBody @Valid LoginRequestDto dados) {
        return authService.login(dados);
    }

    // Rota protegida: devolve quem está logado (o login vem do token; o resto, do banco)
    @GetMapping("/usuario-logado")
    public UsuarioLogadoResponseDto usuarioLogado(@AuthenticationPrincipal Jwt jwt) {
        return authService.buscarUsuarioLogado(jwt.getSubject());
    }
}
