package br.com.delegacia.sgidp.repository.hearing;

import br.com.delegacia.sgidp.enums.hearing.StatusOitiva;
import br.com.delegacia.sgidp.model.hearing.Oitiva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OitivaRepository extends JpaRepository<Oitiva, Long> {

    // Conflito de horário: oitivas AGENDADAS do mesmo responsável dentro da janela (inicio..fim).
    // ignorarId = a própria oitiva (na remarcação/edição); nulo no agendamento
    @Query("""
            SELECT o FROM Oitiva o
            WHERE o.responsavel.id = :responsavelId
              AND o.statusOitiva = :status
              AND o.dataHora BETWEEN :inicio AND :fim
              AND (:ignorarId IS NULL OR o.id <> :ignorarId)
            ORDER BY o.dataHora
            """)
    List<Oitiva> buscarConflitos(Long responsavelId, StatusOitiva status, LocalDateTime inicio,
                                 LocalDateTime fim, Long ignorarId);

    // Listagem com filtros opcionais (nulo = ignora o filtro).
    // CAST nas datas: sem ele o PostgreSQL não descobre o tipo de um parâmetro de data nulo e dá erro
    // "atrasadas": só é aplicado quando agora != null (status AGENDADA e data já passou)
    // @EntityGraph traz procedimento, usuários e partes na mesma consulta
    @EntityGraph(attributePaths = {"procedimento", "responsavel", "cadastradoPor", "alteradoPor", "partes"})
    @Query("""
            SELECT o FROM Oitiva o
            WHERE (CAST(:inicio AS LocalDateTime) IS NULL OR o.dataHora >= :inicio)
              AND (CAST(:fim AS LocalDateTime) IS NULL OR o.dataHora < :fim)
              AND (:responsavelId IS NULL OR o.responsavel.id = :responsavelId)
              AND (:responsavelLogin IS NULL OR o.responsavel.login = :responsavelLogin)
              AND (:status IS NULL OR o.statusOitiva = :status)
              AND (:procedimentoId IS NULL OR o.procedimento.id = :procedimentoId)
              AND (CAST(:agora AS LocalDateTime) IS NULL OR (o.statusOitiva = :agendada AND o.dataHora < :agora))
            ORDER BY o.dataHora
            """)
    List<Oitiva> buscar(LocalDateTime inicio, LocalDateTime fim, Long responsavelId, String responsavelLogin,
                        StatusOitiva status, Long procedimentoId, LocalDateTime agora, StatusOitiva agendada);

    // Tarefas de notificação: oitivas AGENDADAS num intervalo (próximas) ou antes de um momento (sem baixa).
    @EntityGraph(attributePaths = {"responsavel", "procedimento"})
    List<Oitiva> findByStatusOitivaAndDataHoraBetween(StatusOitiva status, LocalDateTime inicio, LocalDateTime fim);

    @EntityGraph(attributePaths = {"responsavel", "procedimento"})
    List<Oitiva> findByStatusOitivaAndDataHoraBefore(StatusOitiva status, LocalDateTime limite);

    // Para o arquivamento do procedimento: oitivas ainda AGENDADAS bloqueiam o arquivamento
    List<Oitiva> findByProcedimentoIdAndStatusOitivaOrderByDataHoraAsc(Long procedimentoId, StatusOitiva status);

    List<Oitiva> findByProcedimentoIdOrderByDataCadastroAsc(Long procedimentoId);
}
