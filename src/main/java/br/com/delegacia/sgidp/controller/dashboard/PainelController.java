package br.com.delegacia.sgidp.controller.dashboard;

import br.com.delegacia.sgidp.dto.dashboard.MinhaMesaResponseDto;
import br.com.delegacia.sgidp.dto.dashboard.PainelResponseDto;
import br.com.delegacia.sgidp.service.dashboard.MinhaMesaService;
import br.com.delegacia.sgidp.service.dashboard.PainelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Delegado, Policial e Estagiário (regra no SecurityConfig); cada um vê os próprios dados
@RestController
@RequestMapping("/painel")
@RequiredArgsConstructor
public class PainelController {

    private final PainelService painelService;
    private final MinhaMesaService minhaMesaService;

    @GetMapping
    public PainelResponseDto consultar(@AuthenticationPrincipal Jwt jwt){
        return painelService.consultar(jwt.getSubject());
    }

    @GetMapping("/minha-mesa")
    public MinhaMesaResponseDto minhaMesa(@AuthenticationPrincipal Jwt jwt){
        return minhaMesaService.consultar(jwt.getSubject());
    }
}
