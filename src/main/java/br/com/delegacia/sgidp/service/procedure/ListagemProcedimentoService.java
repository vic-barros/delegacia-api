package br.com.delegacia.sgidp.service.procedure;

import br.com.delegacia.sgidp.dto.procedure.ContagemAbasResponseDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoListagemItemResponseDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoListagemResponseDto;
import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.enums.procedure.AbaProcedimento;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.repository.custody.HistoricoCustodiaRepository;
import br.com.delegacia.sgidp.repository.delegation.RepasseRepository;
import br.com.delegacia.sgidp.repository.procedure.ContagemAbasProcedimento;
import br.com.delegacia.sgidp.repository.procedure.DataPorProcedimento;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListagemProcedimentoService {

    private static final int TAMANHO_MAXIMO = 100;

    private final ProcedimentoRepository procedimentoRepository;
    private final RepasseRepository repasseRepository;
    private final HistoricoCustodiaRepository historicoCustodiaRepository;

    // T06: aba (padrão EM_ANDAMENTO), tipos, ano, detentor, texto (só dígitos = número; senão parte do crime)
    // e período de abertura (dataInicio/dataFim, os mesmos da exportação)
    @Transactional(readOnly = true)
    public ProcedimentoListagemResponseDto listar(AbaProcedimento aba, List<TipoProcedimento> tipos, Integer ano,
                                                  Long detentorId, String texto, LocalDate dataInicio,
                                                  LocalDate dataFim, String ordem, int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new RegraNegocioException("A página deve ser 0 ou maior e o tamanho entre 1 e " + TAMANHO_MAXIMO);
        }
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new RegraNegocioException("A data inicial não pode ser posterior à data final.");
        }

        if (aba == null) {
            aba = AbaProcedimento.EM_ANDAMENTO;
        }

        // Sem filtro de tipo = todos os tipos (o IN da consulta não aceita lista vazia)
        List<TipoProcedimento> filtroTipos = tipos;
        if (filtroTipos == null || filtroTipos.isEmpty()) {
            filtroTipos = List.of(TipoProcedimento.values());
        }

        Long numero = null;
        String crime = null;
        if (texto != null && !texto.isBlank()) {
            String termo = texto.trim();
            if (termo.matches("\\d{1,9}")) {
                numero = Long.valueOf(termo);
            } else {
                crime = termo;
            }
        }

        StatusProcedimento status = null;
        if (aba == AbaProcedimento.EM_ANDAMENTO) {
            status = StatusProcedimento.EM_ANDAMENTO;
        } else if (aba == AbaProcedimento.ARQUIVADOS) {
            status = StatusProcedimento.ARQUIVADO;
        }
        boolean somenteRemetidos = aba == AbaProcedimento.REMETIDOS;

        Pageable paginacao = PageRequest.of(pagina, tamanho, montarOrdem(ordem));

        Page<Procedimento> resultado = procedimentoRepository.listar(status, somenteRemetidos, filtroTipos, ano,
                detentorId, numero, crime, dataInicio, dataFim, paginacao);

        Set<Long> comRepassePendente = buscarComRepassePendente(resultado.getContent());
        Map<Long, LocalDateTime> naPosseDesde = buscarInicioDaPosse(resultado.getContent());

        List<ProcedimentoListagemItemResponseDto> itens = new ArrayList<>();
        for (Procedimento procedimento : resultado.getContent()) {
            itens.add(ProcedimentoListagemItemResponseDto.de(procedimento,
                    comRepassePendente.contains(procedimento.getId()), naPosseDesde.get(procedimento.getId())));
        }

        ContagemAbasProcedimento contagem = procedimentoRepository.contarPorAba(StatusProcedimento.EM_ANDAMENTO,
                StatusProcedimento.ARQUIVADO, filtroTipos, ano, detentorId, numero, crime, dataInicio, dataFim);

        return new ProcedimentoListagemResponseDto(itens, resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages(), ContagemAbasResponseDto.de(contagem));
    }

    // Ids da página que têm repasse PENDENTE, numa consulta só (e não uma por linha)
    private Set<Long> buscarComRepassePendente(List<Procedimento> procedimentos) {
        Set<Long> ids = new HashSet<>();
        if (procedimentos.isEmpty()) {
            return ids;
        }

        List<Long> idsDaPagina = new ArrayList<>();
        for (Procedimento procedimento : procedimentos) {
            idsDaPagina.add(procedimento.getId());
        }

        for (Repasse repasse : repasseRepository.findByProcedimentoIdInAndStatusRepasse(idsDaPagina,
                StatusRepasse.PENDENTE)) {
            ids.add(repasse.getProcedimento().getId());
        }
        return ids;
    }

    // "Na posse há N dias": início da custódia aberta (dataFim nula) de cada procedimento da página, numa consulta só.
    // Mesma consulta da Minha mesa
    private Map<Long, LocalDateTime> buscarInicioDaPosse(List<Procedimento> procedimentos) {
        Map<Long, LocalDateTime> inicioPorProcedimento = new HashMap<>();
        if (procedimentos.isEmpty()) {
            return inicioPorProcedimento;
        }

        List<Long> idsDaPagina = new ArrayList<>();
        for (Procedimento procedimento : procedimentos) {
            idsDaPagina.add(procedimento.getId());
        }

        for (DataPorProcedimento custodia : historicoCustodiaRepository.buscarInicioCustodiaAtual(idsDaPagina)) {
            inicioPorProcedimento.put(custodia.getProcedimentoId(), custodia.getData());
        }
        return inicioPorProcedimento;
    }

    // Ordem pelas colunas da tabela. Só valores conhecidos: o texto do front nunca vira nome de campo direto.
    // Padrão: abertura mais recente primeiro. "procedimento" = ano, tipo e número. O id desempata
    private Sort montarOrdem(String ordem) {
        Sort porProcedimento = Sort.by("anoProcedimento", "tipoProcedimento", "numeroProcedimento");
        Sort escolhida = switch (ordem == null || ordem.isBlank() ? "-abertura" : ordem.trim()) {
            case "abertura" -> Sort.by("dataAbertura").ascending();
            case "-abertura" -> Sort.by("dataAbertura").descending();
            case "procedimento" -> porProcedimento.ascending();
            case "-procedimento" -> porProcedimento.descending();
            default -> throw new RegraNegocioException(
                    "Ordem inválida. Use abertura, -abertura, procedimento ou -procedimento");
        };
        return escolhida.and(Sort.by("id").descending());
    }
}