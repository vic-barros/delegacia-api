package br.com.delegacia.sgidp.controller.procedure;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.delegacia.sgidp.dto.procedure.ProcedimentoRequestDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoResponseDto;
import br.com.delegacia.sgidp.service.procedure.ProcedimentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/procedimento")
@RequiredArgsConstructor 
public class ProcedimentoController {
    private final ProcedimentoService procedimentoService;

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public ProcedimentoResponseDto criarProcedimento(@RequestBody @Valid ProcedimentoRequestDto dadosDto) {
        return procedimentoService.cadastrar(dadosDto);
    }
}
