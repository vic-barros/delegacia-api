package br.com.delegacia.sgidp.repository.procedure;

import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ProcedimentoRepository
        extends JpaRepository<Procedimento, Long> {

    boolean existsByTipoProcedimentoAndNumeroProcedimentoAndAnoProcedimento(
            TipoProcedimento tipoProcedimento,
            Long numeroProcedimento,
            Integer anoProcedimento
    );

    // Exportação (RF20): filtros opcionais (nulo = ignora o filtro); período pela data de abertura.
    // CAST nas datas: sem ele o PostgreSQL não descobre o tipo de um parâmetro de data nulo e dá erro
    // @EntityGraph traz o detentor na mesma consulta (o relatório mostra o nome dele em toda linha)
    @EntityGraph(attributePaths = "detentorAtual")
    @Query("""
            SELECT p FROM Procedimento p
            WHERE (CAST(:dataInicio AS LocalDate) IS NULL OR p.dataAbertura >= :dataInicio)
              AND (CAST(:dataFim AS LocalDate) IS NULL OR p.dataAbertura <= :dataFim)
              AND (:tipo IS NULL OR p.tipoProcedimento = :tipo)
              AND (:status IS NULL OR p.statusProcedimento = :status)
            ORDER BY p.anoProcedimento, p.tipoProcedimento, p.numeroProcedimento
            """)
    List<Procedimento> buscarParaExportacao(LocalDate dataInicio, LocalDate dataFim,
                                            TipoProcedimento tipo,
                                            StatusProcedimento status);

    // Minha mesa (RF38): procedimentos em andamento sob a posse do usuário logado
    List<Procedimento> findByDetentorAtualLoginAndStatusProcedimentoOrderByDataAberturaAsc(
            String login, StatusProcedimento status
    );

    // Busca global: "IP 45/2026", "45/2026" ou "45" (tipo e ano opcionais)
    @EntityGraph(attributePaths = "detentorAtual")
    @Query("""
            SELECT p FROM Procedimento p
            WHERE p.numeroProcedimento = :numero
              AND (:tipo IS NULL OR p.tipoProcedimento = :tipo)
              AND (:ano IS NULL OR p.anoProcedimento = :ano)
            ORDER BY p.anoProcedimento DESC, p.tipoProcedimento
            """)
    List<Procedimento> buscarPorNumero(Long numero, TipoProcedimento
            tipo, Integer ano, Limit limite);

    // Busca global: texto livre procura parte do crime, sem diferenciar maiúsculas
    @EntityGraph(attributePaths = "detentorAtual")
    List<Procedimento> findByCrimeContainingIgnoreCaseOrderByDataAberturaDesc(String crime, Limit limite);
}