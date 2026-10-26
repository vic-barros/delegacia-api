package br.com.delegacia.sgidp.controller.delegation;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.delegacia.sgidp.dto.delegation.RecusarRepasseRequestDto;
import br.com.delegacia.sgidp.dto.delegation.RepasseRequestDto;
import br.com.delegacia.sgidp.dto.delegation.RepasseResponseDto;
import br.com.delegacia.sgidp.service.delegation.RepasseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/repasses")
@RequiredArgsConstructor 
public class RepasseController {
    private final RepasseService repasseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepasseResponseDto repassarCustodia(@RequestBody @Valid RepasseRequestDto dadosDto, @AuthenticationPrincipal Jwt jwt) {
        return repasseService.solicitarRepasse(dadosDto, jwt.getSubject());
    }

    @PatchMapping("/{id}/aceitar")
    @ResponseStatus(HttpStatus.OK)
    public RepasseResponseDto aceitarRepasse(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return repasseService.aceitar(id, jwt.getSubject());
    }

    @PatchMapping("/{id}/recusar")
    @ResponseStatus(HttpStatus.OK)
    public RepasseResponseDto recusarRepasse(@PathVariable Long id, @RequestBody @Valid RecusarRepasseRequestDto dadosDto, @AuthenticationPrincipal Jwt jwt) {
        return repasseService.recusar(id, dadosDto, jwt.getSubject());
    }
}
