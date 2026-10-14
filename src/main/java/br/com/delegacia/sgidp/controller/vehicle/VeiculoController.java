package br.com.delegacia.sgidp.controller.vehicle;

import br.com.delegacia.sgidp.dto.vehicle.*;
import br.com.delegacia.sgidp.enums.vehicle.SituacaoVeiculo;
import br.com.delegacia.sgidp.service.vehicle.VeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// Leitura: qualquer logado. Escrita: Delegado/Policial (regras no SecurityConfig)
@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService veiculoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeiculoResponseDto cadastrar(@RequestBody @Valid VeiculoCadastroRequestDto dados,
                                        @AuthenticationPrincipal Jwt jwt) {
        return veiculoService.cadastrar(dados, jwt.getSubject());
    }

    @GetMapping
    public List<VeiculoResponseDto> listar(@RequestParam(required = false) String lacre,
                                           @RequestParam(required = false) String placa,
                                           @RequestParam(required = false) String chassi,
                                           @RequestParam(required = false) String motor,
                                           @RequestParam(required = false) SituacaoVeiculo situacao,
                                           @RequestParam(required = false) Long procedimentoId) {
        return veiculoService.listar(lacre, placa, chassi, motor, situacao, procedimentoId);
    }

    @GetMapping("/{id}")
    public VeiculoResponseDto buscarPorId(@PathVariable Long id) {
        return veiculoService.buscarPorId(id);
    }

    @GetMapping("/{id}/historico")
    public List<HistoricoSituacaoVeiculoResponseDto> listarHistorico(@PathVariable Long id) {
        return veiculoService.listarHistorico(id);
    }

    @PutMapping("/{id}")
    public VeiculoResponseDto editar(@PathVariable Long id, @RequestBody @Valid VeiculoEdicaoRequestDto dados) {
        return veiculoService.editar(id, dados);
    }

    @PatchMapping("/{id}/situacao")
    public VeiculoResponseDto alterarSituacao(@PathVariable Long id,
                                              @RequestBody @Valid VeiculoSituacaoRequestDto dados,
                                              @AuthenticationPrincipal Jwt jwt) {
        return veiculoService.alterarSituacao(id, dados, jwt.getSubject());
    }


}
