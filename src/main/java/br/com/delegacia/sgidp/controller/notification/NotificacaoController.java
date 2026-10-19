package br.com.delegacia.sgidp.controller.notification;

import br.com.delegacia.sgidp.dto.notification.ContagemNotificacoesResponseDto;
import br.com.delegacia.sgidp.dto.notification.NotificacaoResponseDto;
import br.com.delegacia.sgidp.service.notification.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;


    // Ex: GET /notificacoes?lida=false
    @GetMapping
    public List<NotificacaoResponseDto> listar(@RequestParam(required = false)
                                               Boolean lida, @AuthenticationPrincipal Jwt jwt) {
        return notificacaoService.listar((jwt.getSubject()), lida);
    }

    @GetMapping("/nao-lidas/contagem")
    public ContagemNotificacoesResponseDto contarNaoLidas(@AuthenticationPrincipal
                                                          Jwt jwt) {
        return notificacaoService.contarNaoLidas(jwt.getSubject());
    }

    @PatchMapping("/{id}/lida")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarComoLida(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        notificacaoService.marcarComoLida(id, jwt.getSubject());
    }

    @PatchMapping("/lidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarTodasComoLidas(@AuthenticationPrincipal Jwt jwt) {
        notificacaoService.marcarTodasComoLidas(jwt.getSubject());
    }
}
