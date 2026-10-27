package br.com.delegacia.sgidp.controller.delegation;

import br.com.delegacia.sgidp.dto.delegation.RepasseListagemResponseDto;
import br.com.delegacia.sgidp.enums.delegation.AbaRepasse;
import br.com.delegacia.sgidp.service.delegation.ListagemRepasseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/repasses")
@RequiredArgsConstructor
public class ListagemRepasseController {

    private final ListagemRepasseService listagemRepasseService;

    // GET /repasses?aba=RECEBIDOS&texto=roubo&pagina=0&tamanho=20
    @GetMapping
    public RepasseListagemResponseDto listar(@RequestParam(required = false) AbaRepasse aba,
                                             @RequestParam(required = false) String texto,
                                             @RequestParam(defaultValue = "0") int pagina,
                                             @RequestParam(defaultValue = "20") int tamanho,
                                             @AuthenticationPrincipal Jwt jwt) {
        return listagemRepasseService.listar(aba, texto, pagina, tamanho, jwt.getSubject());
    }
}