package br.com.delegacia.sgidp.repository.vehicle;

import br.com.delegacia.sgidp.enums.examination.StatusPericia;
import br.com.delegacia.sgidp.enums.vehicle.SituacaoVeiculo;
import br.com.delegacia.sgidp.model.vehicle.Veiculo;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
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

    // Exportação / relatório de carga: situações na lista (o service manda todas quando o filtro vem vazio),
    // demais filtros opcionais. A data de entrada é a primeira linha do histórico (o veículo não guarda esse campo),
    // por isso o período usa uma subconsulta. CAST nas datas pelo mesmo motivo da busca de oitivas (parâmetro nulo)
    @Query("""
            SELECT v FROM Veiculo v JOIN FETCH v.procedimento p
            WHERE v.situacaoVeiculo IN :situacoes
            AND (:statusPericia IS NULL OR v.statusPericia = :statusPericia)
            AND (:procedimentoId IS NULL OR p.id = :procedimentoId)
            AND (CAST(:entradaInicio AS LocalDateTime) IS NULL OR
                 (SELECT MIN(h.dataTransicao) FROM HistoricoSituacaoVeiculo h WHERE h.veiculo = v) >= :entradaInicio)
            AND (CAST(:entradaFim AS LocalDateTime) IS NULL OR
                 (SELECT MIN(h.dataTransicao) FROM HistoricoSituacaoVeiculo h WHERE h.veiculo = v) < :entradaFim)
            ORDER BY v.lacre
            """)
    List<Veiculo> buscarParaExportacao(Collection<SituacaoVeiculo> situacoes, StatusPericia statusPericia,
                                       Long procedimentoId, LocalDateTime entradaInicio, LocalDateTime entradaFim);

    // Painel (RF37): contadores dos veículos que estão na DEPOL
    long countBySituacaoVeiculo(SituacaoVeiculo situacao);

    long countBySituacaoVeiculoAndStatusPericia(SituacaoVeiculo situacao, StatusPericia statusPericia);

    // Busca global: parte do lacre, da placa ou do chassi (o service manda o termo em maiúsculas;
    // "termoSemHifen" para a placa digitada como ABC-1D23)
    @Query("""
            SELECT v FROM Veiculo v JOIN FETCH v.procedimento
            WHERE v.lacre LIKE CONCAT('%', :termo, '%')
               OR v.placa LIKE CONCAT('%', :termoSemHifen, '%')
               OR v.chassi LIKE CONCAT('%', :termoSemHifen, '%')
            ORDER BY v.id DESC
            """)
    List<Veiculo> buscarPorIdentificador(String termo, String termoSemHifen, Limit limite);

}
