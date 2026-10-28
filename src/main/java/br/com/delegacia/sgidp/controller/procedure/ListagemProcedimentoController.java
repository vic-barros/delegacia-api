package br.com.delegacia.sgidp.controller.procedure;

import br.com.delegacia.sgidp.dto.procedure.ProcedimentoListagemResponseDto;
import br.com.delegacia.sgidp.enums.procedure.AbaProcedimento;
import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;
import br.com.delegacia.sgidp.service.procedure.ListagemProcedimentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/procedimentos")
@RequiredArgsConstructor
public class ListagemProcedimentoController {

    private final ListagemProcedimentoService listagemProcedimentoService;

    // GET /procedimentos?aba=EM_ANDAMENTO&tipo=IP&tipo=APF&ano=2026&detentorId=3&texto=furto
    //     &dataInicio=2026-01-01&dataFim=2026-06-30&ordem=-abertura&pagina=0&tamanho=20
    // ordem: abertura | -abertura (padrão) | procedimento | -procedimento ("-" = decrescente)
    @GetMapping
    public ProcedimentoListagemResponseDto listar(@RequestParam(required = false) AbaProcedimento aba,
                                                  @RequestParam(required = false) List<TipoProcedimento> tipo,
                                                  @RequestParam(required = false) Integer ano,
                                                  @RequestParam(required = false) Long detentorId,
                                                  @RequestParam(required = false) String texto,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                                                  @RequestParam(required = false) String ordem,
                                                  @RequestParam(defaultValue = "0") int pagina,
                                                  @RequestParam(defaultValue = "20") int tamanho) {
        return listagemProcedimentoService.listar(aba, tipo, ano, detentorId, texto, dataInicio, dataFim,
                ordem, pagina, tamanho);
    }
}