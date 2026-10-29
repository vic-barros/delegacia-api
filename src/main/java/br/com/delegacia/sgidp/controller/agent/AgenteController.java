package br.com.delegacia.sgidp.controller.agent;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.delegacia.sgidp.dto.agent.ResumoProcedimentoDto;
import br.com.delegacia.sgidp.dto.agent.ResumoProcedimentoResponseDto;
import br.com.delegacia.sgidp.service.agent.AgenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/agente")
@RequiredArgsConstructor 
public class AgenteController {
    private final AgenteService agenteService;

    @GetMapping("/resumir-procedimento/{id}")
    public ResumoProcedimentoResponseDto resumirProcedimento(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return agenteService.gerarResumoProcedimento(id, jwt.getSubject());
    }

}
