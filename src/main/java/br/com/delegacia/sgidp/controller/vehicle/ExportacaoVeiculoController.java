package br.com.delegacia.sgidp.controller.vehicle;

import br.com.delegacia.sgidp.dto.export.ArquivoExportadoResponseDto;
import br.com.delegacia.sgidp.enums.examination.StatusPericia;
import br.com.delegacia.sgidp.enums.export.FormatoExportacao;
import br.com.delegacia.sgidp.enums.vehicle.SituacaoVeiculo;
import br.com.delegacia.sgidp.service.vehicle.ExportacaoVeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

// Exportação de veículos / relatório de carga. Separado do VeiculoController, como na exportação de procedimentos
@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
public class ExportacaoVeiculoController {

    private final ExportacaoVeiculoService exportacaoVeiculoService;

    // "situacao" pode repetir: ?situacao=NA_DEPOL&situacao=EM_PATIO (carga atual); sem nenhuma = todas
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(
            @RequestParam FormatoExportacao formato,
            @RequestParam(required = false) List<SituacaoVeiculo> situacao,
            @RequestParam(required = false) StatusPericia statusPericia,
            @RequestParam(required = false) Long procedimentoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @AuthenticationPrincipal Jwt jwt) {
        ArquivoExportadoResponseDto arquivo = exportacaoVeiculoService.exportar(formato, situacao, statusPericia,
                procedimentoId, dataInicio, dataFim, jwt.getSubject());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + arquivo.nomeArquivo() + "\"")
                .contentType(MediaType.parseMediaType(arquivo.tipoConteudo()))
                .body(arquivo.conteudo());
    }
}