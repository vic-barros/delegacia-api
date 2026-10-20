package br.com.delegacia.sgidp.controller.procedure;

import br.com.delegacia.sgidp.dto.procedure.ArquivarProcedimentoDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoRequestDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoResponseDto;
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
    public ProcedimentoResponseDto arquivarProcedimento(@PathVariable Long id, @RequestBody @Valid ArquivarProcedimentoDto dto, @AuthenticationPrincipal Jwt jwt) {
        return procedimentoService.arquivar(id, dto, jwt.getSubject());
    }
}
