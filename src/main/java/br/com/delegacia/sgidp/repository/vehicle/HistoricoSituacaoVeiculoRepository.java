package br.com.delegacia.sgidp.repository.vehicle;

import br.com.delegacia.sgidp.model.vehicle.HistoricoSituacaoVeiculo;
import br.com.delegacia.sgidp.repository.procedure.DataPorProcedimento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface HistoricoSituacaoVeiculoRepository extends JpaRepository<HistoricoSituacaoVeiculo, Long> {

    // Linha do tempo do veículo, da mais antiga para a mais nova.
    // @EntityGraph traz o responsável junto (evita uma consulta extra por linha)
    @EntityGraph(attributePaths = "responsavel")
    List<HistoricoSituacaoVeiculo> findByVeiculoIdOrderByDataTransicaoAsc(Long veiculoId);

    // Data de entrada de vários veículos de uma vez (primeira linha do histórico de cada um), para a exportação.
    // Projeção por interface: o Spring preenche getVeiculoId()/getDataEntrada() pelos apelidos (AS) da consulta
    @Query("""
            SELECT h.veiculo.id AS veiculoId, MIN(h.dataTransicao) AS dataEntrada
            FROM HistoricoSituacaoVeiculo h
            WHERE h.veiculo.id IN :veiculoIds
            GROUP BY h.veiculo.id
            """)
    List<DataEntradaVeiculo> buscarDatasEntrada(Collection<Long> veiculoIds);

    interface DataEntradaVeiculo {
        Long getVeiculoId();

        LocalDateTime getDataEntrada();
    }

    // Minha mesa: última mudança de situação de algum veículo de cada procedimento
    @Query("""
            SELECT h.veiculo.procedimento.id AS procedimentoId, MAX(h.dataTransicao) AS data
            FROM HistoricoSituacaoVeiculo h
            WHERE h.veiculo.procedimento.id IN :procedimentoIds
            GROUP BY h.veiculo.procedimento.id
            """)
    List<DataPorProcedimento> buscarUltimaMovimentacao(Collection<Long> procedimentoIds);
}
