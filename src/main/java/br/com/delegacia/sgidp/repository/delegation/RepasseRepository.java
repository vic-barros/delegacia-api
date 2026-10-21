package br.com.delegacia.sgidp.repository.delegation;

import br.com.delegacia.sgidp.repository.procedure.DataPorProcedimento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface RepasseRepository extends JpaRepository<Repasse, Long> {

    boolean existsByProcedimentoIdAndStatusRepasse(Long procedimentoId, StatusRepasse statusRepasse);

    // Minha mesa: repasses de um status (PENDENTE) dos procedimentos, com o destinatário na mesma consulta
    @EntityGraph(attributePaths = "destinatario")
    List<Repasse> findByProcedimentoIdInAndStatusRepasse(Collection<Long> procedimentoIds, StatusRepasse status);

    // Minha mesa: última movimentação com repasse (resposta, se houver; senão a solicitação)
    @Query("""
            SELECT r.procedimento.id AS procedimentoId, MAX(COALESCE(r.dataResposta, r.dataSolicitacao)) AS data
            FROM Repasse r
            WHERE r.procedimento.id IN :procedimentoIds
            GROUP BY r.procedimento.id
            """)
    List<DataPorProcedimento> buscarUltimaMovimentacao(Collection<Long> procedimentoIds);

    // Painel (RF37): repasses pendentes recebidos e enviados pelo usuário logado
    @EntityGraph(attributePaths = {"procedimento", "solicitante", "destinatario"})
    List<Repasse> findByDestinatarioLoginAndStatusRepasseOrderByDataSolicitacaoAsc(String login, StatusRepasse status);

    @EntityGraph(attributePaths = {"procedimento", "solicitante", "destinatario"})
    List<Repasse> findBySolicitanteLoginAndStatusRepasseOrderByDataSolicitacaoAsc(String login, StatusRepasse status);
}
