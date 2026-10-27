package br.com.delegacia.sgidp.controller.procedure;

import br.com.delegacia.sgidp.dto.export.ArquivoExportadoResponseDto;
import br.com.delegacia.sgidp.enums.export.FormatoExportacao;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;
import br.com.delegacia.sgidp.service.procedure.ExportacaoProcedimentoService;
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

// Separado do ProcedimentoController para não conflitar com as rotas de procedimento em desenvolvimento
@RestController
@RequestMapping("/procedimentos")
@RequiredArgsConstructor
public class ExportacaoProcedimentoController {

    private final ExportacaoProcedimentoService exportacaoProcedimentoService;

    // "attachment" faz o navegador baixar o arquivo (a etiqueta usa "inline" para abrir na tela).
    // "tipo" pode repetir: ?tipo=IP&tipo=APF; sem nenhum = todos
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(
            @RequestParam FormatoExportacao formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) List<TipoProcedimento> tipo,
            @RequestParam(required = false) StatusProcedimento status,
            @AuthenticationPrincipal Jwt jwt) {
        ArquivoExportadoResponseDto arquivo = exportacaoProcedimentoService.exportar(formato, dataInicio, dataFim,
                tipo, status, jwt.getSubject());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + arquivo.nomeArquivo() + "\"")
                .contentType(MediaType.parseMediaType(arquivo.tipoConteudo()))
                .body(arquivo.conteudo());
    }
}