package br.com.delegacia.sgidp.repository.vehicle;

import br.com.delegacia.sgidp.enums.vehicle.SituacaoVeiculo;
import br.com.delegacia.sgidp.model.vehicle.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    // Próximo número do lacre. SQL nativo justificado (RNF06): o JPQL não lê sequence
    @Query(value = "SELECT nextval('seq_lacre_veiculo')", nativeQuery = true)
    Long proximoNumeroLacre();

    // Duplicidade: procura um veículo ATIVO (situação na lista) com a mesma placa / o mesmo chassi / o mesmo motor
    Optional<Veiculo> findFirstByPlacaAndSituacaoVeiculoIn(String placa, Collection<SituacaoVeiculo> situacoes);

    Optional<Veiculo> findFirstByChassiAndSituacaoVeiculoIn(String chassi, Collection<SituacaoVeiculo> situacoes);

    Optional<Veiculo> findFirstByMotorAndSituacaoVeiculoIn(String motor, Collection<SituacaoVeiculo> situacoes);


    // Busca com filtros opcionais: filtro nulo é ignorado (":x IS NULL OR ...").
    // JOIN FETCH traz o procedimento na mesma consulta (a resposta mostra "IP 123/2026")
    @Query("""
            SELECT v FROM Veiculo v JOIN FETCH v.procedimento p
            WHERE (:lacre IS NULL OR v.lacre = :lacre)
            AND (:placa IS NULL OR v.placa = :placa)
            AND (:chassi IS NULL OR v.chassi = :chassi)
            AND (:motor IS NULL OR v.motor = :motor)
            AND (:situacao IS NULL OR v.situacaoVeiculo = :situacao)
            AND (:procedimentoId IS NULL OR p.id = :procedimentoId)
            ORDER BY v.id DESC
            """)
    List<Veiculo> buscar(String lacre, String placa, String chassi, String motor,
                         SituacaoVeiculo situacao, Long procedimentoId);

}
