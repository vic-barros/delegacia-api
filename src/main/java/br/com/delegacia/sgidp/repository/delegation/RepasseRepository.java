package br.com.delegacia.sgidp.repository.delegation;

import br.com.delegacia.sgidp.repository.procedure.DataPorProcedimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    // Detalhe do procedimento: o repasse pendente (só pode haver um por procedimento)
    @EntityGraph(attributePaths = "destinatario")
    Optional<Repasse> findFirstByProcedimentoIdAndStatusRepasse(Long procedimentoId, StatusRepasse statusRepasse);

    // Tela de repasses (T09): repasses do usuário como destinatário e/ou solicitante, nos status da aba.
    // Texto: só dígitos = número do procedimento; senão parte do crime ou do nome de um dos servidores
    @EntityGraph(attributePaths = {"procedimento", "solicitante.role", "destinatario.role"})
    @Query("""
            SELECT r FROM Repasse r
            WHERE r.statusRepasse IN :status
              AND ((:comoDestinatario = true AND r.destinatario.login = :login)
                OR (:comoSolicitante = true AND r.solicitante.login = :login))
              AND (:numero IS NULL OR r.procedimento.numeroProcedimento = :numero)
              AND (:termo IS NULL
                OR LOWER(r.procedimento.crime) LIKE LOWER(CONCAT('%', CAST(:termo AS String), '%'))
                OR LOWER(r.solicitante.nome) LIKE LOWER(CONCAT('%', CAST(:termo AS String), '%'))
                OR LOWER(r.destinatario.nome) LIKE LOWER(CONCAT('%', CAST(:termo AS String), '%')))
            """)
    Page<Repasse> listar(String login, boolean comoDestinatario, boolean comoSolicitante,
                         Collection<StatusRepasse> status, Long numero, String termo, Pageable pageable);

    // Números das abas "Recebidos (2)" e "Enviados (1)": só os pendentes
    long countByDestinatarioLoginAndStatusRepasse(String login, StatusRepasse status);

    long countBySolicitanteLoginAndStatusRepasse(String login, StatusRepasse status);
}



