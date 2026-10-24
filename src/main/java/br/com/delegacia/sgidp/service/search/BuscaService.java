package br.com.delegacia.sgidp.service.search;

import br.com.delegacia.sgidp.dto.party.PessoaResumoResponseDto;
import br.com.delegacia.sgidp.dto.search.BuscaResponseDto;
import br.com.delegacia.sgidp.dto.search.ProcedimentoBuscaResponseDto;
import br.com.delegacia.sgidp.dto.search.VeiculoBuscaResponseDto;
import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.repository.party.PessoaRepository;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import br.com.delegacia.sgidp.repository.vehicle.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BuscaService {

    private static final Limit MAXIMO_POR_GRUPO = Limit.of(10);

    // "IP 45/2026", "ip45/2026", "45/2026", "IP 45" ou "45": grupo 1 = tipo, 2 = número, 3 = ano (1 e 3 opcionais)
    private static final Pattern NUMERO_PROCEDIMENTO = Pattern.compile("^([A-Za-z]{2,5})?\\s*(\\d{1,9})\\s*(?:/\\s*(\\d{4}))?$");

    // CPF com ou sem pontuação: 11 dígitos, opcionalmente com . e -
    private static final Pattern CPF = Pattern.compile("^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$");

    private final ProcedimentoRepository procedimentoRepository;
    private final VeiculoRepository veiculoRepository;
    private final PessoaRepository pessoaRepository;

    // Busca global (Ctrl+K): procura o mesmo termo em procedimentos, veículos e pessoas
    @Transactional(readOnly = true)
    public BuscaResponseDto buscar(String q) {
        String termo = (q == null) ? "" : q.trim();
        if (termo.length() < 2) {
            throw new RegraNegocioException("Digite ao menos 2 caracteres para buscar");
        }

        return new BuscaResponseDto(
                buscarProcedimentos(termo),
                buscarVeiculos(termo),
                buscarPessoas(termo)
        );
    }

    private List<ProcedimentoBuscaResponseDto> buscarProcedimentos(String termo) {
        List<Procedimento> procedimentos;
        Matcher numero = NUMERO_PROCEDIMENTO.matcher(termo);

        if (numero.matches() && tipoValido(numero.group(1))) {
            TipoProcedimento tipo = null;
            if (numero.group(1) != null) {
                tipo = TipoProcedimento.valueOf(numero.group(1).toUpperCase());
            }
            Integer ano = null;
            if (numero.group(3) != null) {
                ano = Integer.valueOf(numero.group(3));
            }
            procedimentos = procedimentoRepository.buscarPorNumero(
                    Long.valueOf(numero.group(2)), tipo, ano, MAXIMO_POR_GRUPO);
        } else {
            procedimentos = procedimentoRepository.findByCrimeContainingIgnoreCaseOrderByDataAberturaDesc(
                    termo, MAXIMO_POR_GRUPO);
        }

        return procedimentos.stream()
                .map(ProcedimentoBuscaResponseDto::de)
                .toList();
    }

    // Sem tipo ("45/2026") vale; com tipo, só se for um dos tipos existentes ("TCO 12" sim, "abc 12" não)
    private boolean tipoValido(String tipo) {
        if (tipo == null) {
            return true;
        }
        for (TipoProcedimento valor : TipoProcedimento.values()) {
            if (valor.name().equalsIgnoreCase(tipo)) {
                return true;
            }
        }
        return false;
    }

    // Lacre, placa e chassi ficam gravados em maiúsculas (normalizados no cadastro)
    private List<VeiculoBuscaResponseDto> buscarVeiculos(String termo) {
        String maiusculo = termo.toUpperCase().replace(" ", "");
        String semHifen = maiusculo.replace("-", "");

        return veiculoRepository.buscarPorIdentificador(maiusculo, semHifen, MAXIMO_POR_GRUPO).stream()
                .map(VeiculoBuscaResponseDto::de)
                .toList();
    }

    // CPF digitado: busca exata pelo CPF (só dígitos); senão, parte do nome ou da alcunha
    private List<PessoaResumoResponseDto> buscarPessoas(String termo) {
        if (CPF.matcher(termo).matches()) {
            return pessoaRepository.buscar(null, termo.replaceAll("\\D", "")).stream()
                    .map(PessoaResumoResponseDto::de)
                    .toList();
        }
        return pessoaRepository.buscar(termo, null).stream()
                .limit(10)
                .map(PessoaResumoResponseDto::de)
                .toList();
    }
}