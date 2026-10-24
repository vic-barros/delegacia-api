package br.com.delegacia.sgidp.controller.search;

import br.com.delegacia.sgidp.dto.search.BuscaResponseDto;
import br.com.delegacia.sgidp.service.search.BuscaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/busca")
@RequiredArgsConstructor
public class BuscaController {

    private final BuscaService buscaService;

    // GET /busca?q=IP 45/2026 (ou placa, lacre, CPF, nome, crime)
    @GetMapping
    public BuscaResponseDto buscar(@RequestParam String q) {
        return buscaService.buscar(q);
    }
}