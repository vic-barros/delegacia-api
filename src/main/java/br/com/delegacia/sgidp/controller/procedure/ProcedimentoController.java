package br.com.delegacia.sgidp.controller.procedure;

import br.com.delegacia.sgidp.dto.procedure.ArquivarProcedimentoRequestDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoRequestDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoResponseDto;
import br.com.delegacia.sgidp.dto.procedure.ReabrirProcedimentoRequestDto;
import br.com.delegacia.sgidp.service.procedure.ProcedimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/procedimentos")
@RequiredArgsConstructor
public class ProcedimentoController {
    private final ProcedimentoService procedimentoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcedimentoResponseDto criarProcedimento(@RequestBody @Valid ProcedimentoRequestDto dadosDto,
                                                    @AuthenticationPrincipal Jwt jwt) {
        return procedimentoService.cadastrar(dadosDto, jwt.getSubject());
    }

    @PatchMapping("/{id}/arquivar")
    @ResponseStatus(HttpStatus.OK)
    public ProcedimentoResponseDto arquivarProcedimento(@PathVariable Long id, @RequestBody @Valid ArquivarProcedimentoRequestDto dto, @AuthenticationPrincipal Jwt jwt) {
        return procedimentoService.arquivar(id, dto, jwt.getSubject());
    }
    
    @PatchMapping("/{id}/reabrir")
    @ResponseStatus(HttpStatus.OK)
    public ProcedimentoResponseDto reabrirProcedimento(@PathVariable Long id, @RequestBody @Valid ReabrirProcedimentoRequestDto dadosDto, @AuthenticationPrincipal Jwt jwt) {
        return procedimentoService.reabrir(id, dadosDto, jwt.getSubject());
    }
}
