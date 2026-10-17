package br.com.delegacia.sgidp.controller.delegation;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
