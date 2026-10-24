package br.com.delegacia.sgidp.controller.procedure;

import br.com.delegacia.sgidp.dto.custody.HistoricoCustodiaResponseDto;
import br.com.delegacia.sgidp.dto.procedure.*;
import br.com.delegacia.sgidp.service.procedure.ProcedimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}")
    public ProcedimentoDetalheResponseDto buscarPorId(@PathVariable Long id) {
        return procedimentoService.buscarPorId(id);
    }

    @GetMapping("/{id}/historico-custodia")
    public List<HistoricoCustodiaResponseDto> listarHistoricoCustodia(@PathVariable Long id) {
        return procedimentoService.listarHistoricoCustodia(id);
    }

    @GetMapping("/{id}/historico-status")
    public List<HistoricoStatusResponseDto> listarHistoricoStatus(@PathVariable Long id) {
        return procedimentoService.listarHistoricoStatus(id);
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
