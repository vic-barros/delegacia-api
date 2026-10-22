package br.com.delegacia.sgidp.repository.procedure;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.delegacia.sgidp.model.procedure.HistoricoStatusProcedimento;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface HistoricoStatusProcedimentoRepository extends JpaRepository<HistoricoStatusProcedimento, Long> {

    // Minha mesa: última mudança de status (cadastro, arquivamento, reabertura) de cada procedimento
    @Query("""
            SELECT h.procedimento.id AS procedimentoId, MAX(h.dataTransicao) AS data
            FROM HistoricoStatusProcedimento h
            WHERE h.procedimento.id IN :procedimentoIds
            GROUP BY h.procedimento.id
            """)
    List<DataPorProcedimento> buscarUltimaTransicao(Collection<Long> procedimentoIds);

    // Detalhe do procedimento: mudanças de status em ordem, com o responsável na mesma consulta
    @EntityGraph(attributePaths = "responsavel")
    List<HistoricoStatusProcedimento> findByProcedimentoIdOrderByDataTransicaoAsc(Long procedimentoId);

}