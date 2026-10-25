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
import br.com.delegacia.sgidp.repository.delegation.RepasseRepository;
import br.com.delegacia.sgidp.repository.procedure.ContagemAbasProcedimento;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListagemProcedimentoService {

    private static final int TAMANHO_MAXIMO = 100;

    private final ProcedimentoRepository procedimentoRepository;
    private final RepasseRepository repasseRepository;

    // T06: aba (padrão EM_ANDAMENTO), tipos, ano, detentor e texto (só dígitos = número; senão parte do crime)
    @Transactional(readOnly = true)
    public ProcedimentoListagemResponseDto listar(AbaProcedimento aba, List<TipoProcedimento> tipos, Integer ano,
                                                  Long detentorId, String texto, int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new RegraNegocioException("A página deve ser 0 ou maior e o tamanho entre 1 e " + TAMANHO_MAXIMO);
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

        // Mais recentes primeiro; o id desempata procedimentos abertos no mesmo dia
        Pageable paginacao = PageRequest.of(pagina, tamanho,
                Sort.by("dataAbertura").descending().and(Sort.by("id").descending()));

        Page<Procedimento> resultado = procedimentoRepository.listar(status, somenteRemetidos, filtroTipos, ano,
                detentorId, numero, crime, paginacao);

        Set<Long> comRepassePendente = buscarComRepassePendente(resultado.getContent());

        List<ProcedimentoListagemItemResponseDto> itens = new ArrayList<>();
        for (Procedimento procedimento : resultado.getContent()) {
            itens.add(ProcedimentoListagemItemResponseDto.de(procedimento,
                    comRepassePendente.contains(procedimento.getId())));
        }

        ContagemAbasProcedimento contagem = procedimentoRepository.contarPorAba(StatusProcedimento.EM_ANDAMENTO,
                StatusProcedimento.ARQUIVADO, filtroTipos, ano, detentorId, numero, crime);

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
}