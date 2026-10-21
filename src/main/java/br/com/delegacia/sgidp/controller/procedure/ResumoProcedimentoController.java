package br.com.delegacia.sgidp.controller.procedure;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.com.delegacia.sgidp.service.procedure.ResumoProcedimentoService;

@RestController
@RequestMapping("/dev")
public class ResumoProcedimentoController {

    private final ResumoProcedimentoService resumoProcedimentoService;

    public ResumoProcedimentoController(ResumoProcedimentoService resumoProcedimentoService) {
        this.resumoProcedimentoService = resumoProcedimentoService;
    }

    // rota usada pra testar comunicação com o gemini
    @GetMapping("/gemini")
    public String test(@RequestParam String message) {
        return resumoProcedimentoService.ask(message);
    }

    // rota usada pra verificar o resumo
    @GetMapping("/resumostring/{procedimentoId}")
    public String resumo(@PathVariable Long procedimentoId) {
        return resumoProcedimentoService.criarContexto(procedimentoId);
    }

    // rota usada pra testar o modelo de resumo
    @GetMapping("/resumo/{procedimentoId}")
    public String testeContexto(@PathVariable Long procedimentoId) {
        return resumoProcedimentoService.gerarResumoProcedimento(procedimentoId);
    }
}