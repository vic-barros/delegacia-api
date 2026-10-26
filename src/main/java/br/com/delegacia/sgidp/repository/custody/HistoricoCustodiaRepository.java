package br.com.delegacia.sgidp.repository.custody;

import br.com.delegacia.sgidp.repository.procedure.DataPorProcedimento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.delegacia.sgidp.model.custody.HistoricoCustodia;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface HistoricoCustodiaRepository
        extends JpaRepository<HistoricoCustodia, Long> {

    // Minha mesa: início da custódia atual de cada procedimento = "na posse desde" / data_fim = nula
    @Query("""
            SELECT h.procedimento.id AS procedimentoId, h.dataInicio AS data
            FROM HistoricoCustodia h
            WHERE h.procedimento.id IN :procedimentoIds
              AND h.dataFim IS NULL
            """)
    List<DataPorProcedimento> buscarInicioCustodiaAtual(Collection<Long> procedimentoIds);

    // Detalhe do procedimento: todas as posses, da mais antiga para a atual, com o usuário na mesma consulta
    @EntityGraph(attributePaths = "usuario")
    List<HistoricoCustodia> findByProcedimentoIdOrderByDataInicioAsc(Long procedimentoId);

    Optional<HistoricoCustodia> findByProcedimentoIdAndDataFimIsNull(Long procedimentoId);
}