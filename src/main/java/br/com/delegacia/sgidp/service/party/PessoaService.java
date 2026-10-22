package br.com.delegacia.sgidp.service.party;

import br.com.delegacia.sgidp.dto.party.ParticipacaoResponseDto;
import br.com.delegacia.sgidp.dto.party.PessoaRequestDto;
import br.com.delegacia.sgidp.dto.party.PessoaResponseDto;
import br.com.delegacia.sgidp.dto.party.PessoaResumoResponseDto;
import br.com.delegacia.sgidp.exception.RecursoDuplicadoException;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.party.Pessoa;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.hearing.OitivaParteRepository;
import br.com.delegacia.sgidp.repository.party.PessoaRepository;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final OitivaParteRepository oitivaParteRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PessoaResponseDto cadastrar(PessoaRequestDto dados, String loginLogado) {
        Usuario usuario = usuarioRepository.findByLogin(loginLogado)
                .orElseThrow(() -> new RegraNegocioException("Usuário autenticado não encontrado"));

        String cpf = normalizarCpf(dados.cpf());
        if (cpf != null && pessoaRepository.existsByCpf(cpf)) {
            throw new RecursoDuplicadoException("Já existe uma pessoa cadastrada com este CPF");
        }

        Pessoa pessoa = new Pessoa();
        preencher(pessoa, dados, cpf);
        pessoa.setCadastradoPor(usuario);
        return PessoaResponseDto.de(pessoaRepository.save(pessoa));
    }

    @Transactional(readOnly = true)
    public List<PessoaResumoResponseDto> buscar(String nome, String cpf) {
        String filtroNome;

        if (nome == null || nome.isBlank()) {
            filtroNome = null;
        } else {
            filtroNome = nome.trim();
        }


        return pessoaRepository.buscar(filtroNome, normalizarCpf(cpf)).stream()
                .map(PessoaResumoResponseDto::de)
                .toList();
    }

    @Transactional
    public PessoaResponseDto editar(Long id, PessoaRequestDto dados) {
        Pessoa pessoa = buscarPessoa(id);
        String cpf = normalizarCpf(dados.cpf());
        if (cpf != null && pessoaRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new RecursoDuplicadoException("Já existe uma pessoa cadastrada com este CPF");
        }
        preencher(pessoa, dados, cpf);
        return PessoaResponseDto.de(pessoa);
    }

    @Transactional(readOnly = true)
    public PessoaResponseDto buscarPorId(Long id) {
        return PessoaResponseDto.de(buscarPessoa(id));
    }


    // Ligações da pessoa: procedimentos e oitivas em que aparece, com o papel
    @Transactional(readOnly = true)
    public List<ParticipacaoResponseDto> listarParticipacoes(Long id) {
        buscarPessoa(id);
        return oitivaParteRepository.buscarParticipacoes(id).stream()
                .map(ParticipacaoResponseDto::de)
                .toList();
    }


    // Também usado pela oitiva, ao vincular uma parte a uma pessoa cadastrada
    public Pessoa buscarPessoa(Long id) {
        return pessoaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada"));
    }

    private void preencher(Pessoa pessoa, PessoaRequestDto dados, String cpf) {
        pessoa.setNome(dados.nome().trim());
        pessoa.setDataNascimento(dados.dataNascimento());
        pessoa.setCpf(cpf);
        pessoa.setAlcunha(dados.alcunha());
        pessoa.setTelefone(dados.telefone());
        pessoa.setEndereco(dados.endereco());
    }

    // Guarda só os dígitos: "123.456.789-09" e "12345678909" viram o mesmo CPF; vazio vira nulo
    private String normalizarCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return null;
        }
        return cpf.replaceAll("\\D", "");
    }
}
