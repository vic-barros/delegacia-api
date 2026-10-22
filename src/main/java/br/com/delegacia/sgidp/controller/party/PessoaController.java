package br.com.delegacia.sgidp.controller.party;

import br.com.delegacia.sgidp.dto.party.ParticipacaoResponseDto;
import br.com.delegacia.sgidp.dto.party.PessoaRequestDto;
import br.com.delegacia.sgidp.dto.party.PessoaResponseDto;
import br.com.delegacia.sgidp.dto.party.PessoaResumoResponseDto;
import br.com.delegacia.sgidp.service.party.PessoaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pessoas")
@RequiredArgsConstructor
public class PessoaController {

    private final PessoaService pessoaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PessoaResponseDto cadastrar(@RequestBody @Valid PessoaRequestDto dados,
                                       @AuthenticationPrincipal Jwt jwt) {
        return pessoaService.cadastrar(dados, jwt.getSubject());
    }

    @GetMapping
    public List<PessoaResumoResponseDto> buscar(@RequestParam(required = false)
                                                String nome,
                                                @RequestParam(required = false) String cpf) {
        return pessoaService.buscar(nome, cpf);
    }

    @GetMapping("/{id}")
    public PessoaResponseDto buscarPorId(@PathVariable Long id) {
        return pessoaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public PessoaResponseDto editar(@PathVariable Long id, @RequestBody @Valid PessoaRequestDto dados) {
        return pessoaService.editar(id, dados);
    }

    @GetMapping("/{id}/participacoes")
    public List<ParticipacaoResponseDto> listarParticipacoes(@PathVariable Long id) {
        return pessoaService.listarParticipacoes(id);
    }


}
