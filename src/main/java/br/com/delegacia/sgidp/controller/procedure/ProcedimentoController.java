package br.com.delegacia.sgidp.controller.procedure;

import br.com.delegacia.sgidp.dto.procedure.ArquivarProcedimentoRequestDto;
import br.com.delegacia.sgidp.dto.procedure.HistoricoRemessaFinalResponseDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoRequestDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoResponseDto;
import br.com.delegacia.sgidp.dto.procedure.RegistrarRemessaFinalRequestDto;
import br.com.delegacia.sgidp.service.procedure.ProcedimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

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

    @PatchMapping("/{id}/registrar-remessa")
    @ResponseStatus(HttpStatus.OK)
    public ProcedimentoResponseDto registrarRemessaFinal(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean sobrescrever, @RequestBody @Valid RegistrarRemessaFinalRequestDto dadosDto, @AuthenticationPrincipal Jwt jwt ) {
        return procedimentoService.registrarRemessaFinal(id, sobrescrever, dadosDto, jwt.getSubject());
    }

    @GetMapping("/{id}/historico-remessa")
    @ResponseStatus(HttpStatus.OK)
    public List<HistoricoRemessaFinalResponseDto> listarHistoricoRemessa(
            @PathVariable Long id) {
        return procedimentoService.listarHistoricoRemessa(id);
    }
}
