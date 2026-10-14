package br.com.delegacia.sgidp.repository.vehicle;

import br.com.delegacia.sgidp.model.vehicle.HistoricoSituacaoVeiculo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoSituacaoVeiculoRepository extends JpaRepository<HistoricoSituacaoVeiculo, Long> {

    // Linha do tempo do veículo, da mais antiga para a mais nova.
    // @EntityGraph traz o responsável junto (evita uma consulta extra por linha)
    @EntityGraph(attributePaths = "responsavel")
    List<HistoricoSituacaoVeiculo> findByVeiculoIdOrderByDataTransicaoAsc(Long veiculoId);
}
