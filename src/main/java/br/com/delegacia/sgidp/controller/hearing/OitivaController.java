package br.com.delegacia.sgidp.controller.hearing;

import br.com.delegacia.sgidp.dto.hearing.*;
import br.com.delegacia.sgidp.enums.hearing.StatusOitiva;
import br.com.delegacia.sgidp.service.hearing.OitivaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// Delegado, Policial e Estagiário (regra no SecurityConfig); Admin não acessa
@RestController
@RequestMapping("/oitivas")
@RequiredArgsConstructor
public class OitivaController {

    private final OitivaService oitivaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OitivaResponseDto agendar(@RequestBody @Valid OitivaAgendamentoRequestDto dados,
                                     @AuthenticationPrincipal Jwt jwt) {
        return oitivaService.agendar(dados, jwt.getSubject());
    }

    // Datas no formato 2026-10-20
    @GetMapping
    public List<OitivaResponseDto> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Long responsavelId,
            @RequestParam(required = false) StatusOitiva status,
            @RequestParam(required = false) Long procedimentoId,
            @RequestParam(defaultValue = "false") boolean minhas,
            @RequestParam(defaultValue = "false") boolean atrasadas,
            @AuthenticationPrincipal Jwt jwt) {
        return oitivaService.listar(dataInicio, dataFim, responsavelId, status, procedimentoId, minhas, atrasadas,
                jwt.getSubject());
    }

    @GetMapping("/{id}")
    public OitivaResponseDto buscarPorId(@PathVariable Long id) {
        return oitivaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public OitivaResponseDto editar(@PathVariable Long id, @RequestBody @Valid OitivaEdicaoRequestDto dados,
                                    @AuthenticationPrincipal Jwt jwt) {
        return oitivaService.editar(id, dados, jwt.getSubject());
    }

    @PatchMapping("/{id}/remarcar")
    public OitivaResponseDto remarcar(@PathVariable Long id, @RequestBody @Valid OitivaRemarcacaoRequestDto dados,
                                      @AuthenticationPrincipal Jwt jwt) {
        return oitivaService.remarcar(id, dados, jwt.getSubject());
    }

    @PatchMapping("/{id}/desmarcar")
    public OitivaResponseDto desmarcar(@PathVariable Long id, @RequestBody @Valid OitivaDesmarcacaoRequestDto dados,
                                       @AuthenticationPrincipal Jwt jwt) {
        return oitivaService.desmarcar(id, dados, jwt.getSubject());
    }

    @PatchMapping("/{id}/concluir")
    public OitivaResponseDto concluir(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return oitivaService.concluir(id, jwt.getSubject());
    }
}