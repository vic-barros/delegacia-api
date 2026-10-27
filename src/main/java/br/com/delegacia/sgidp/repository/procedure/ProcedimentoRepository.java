package br.com.delegacia.sgidp.repository.procedure;

import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
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
              AND (p.tipoProcedimento IN :tipos)
              AND (:status IS NULL OR p.statusProcedimento = :status)
            ORDER BY p.anoProcedimento, p.tipoProcedimento, p.numeroProcedimento
            """)
    List<Procedimento> buscarParaExportacao(LocalDate dataInicio, LocalDate dataFim,
                                            Collection<TipoProcedimento> tipos,
                                            StatusProcedimento status);

    // Listagem (T06): a aba vira status (nulo = todos) e/ou "só remetidos"; tipos em lista (o service manda
    // todos quando o filtro vem vazio); demais filtros opcionais. A ordem e a página vêm no Pageable
    @EntityGraph(attributePaths = "detentorAtual")
    @Query("""
            SELECT p FROM Procedimento p
            WHERE (:status IS NULL OR p.statusProcedimento = :status)
              AND (:somenteRemetidos = false OR p.dataRemessaFinal IS NOT NULL)
              AND p.tipoProcedimento IN :tipos
              AND (:ano IS NULL OR p.anoProcedimento = :ano)
              AND (:detentorId IS NULL OR p.detentorAtual.id = :detentorId)
              AND (:numero IS NULL OR p.numeroProcedimento = :numero)
              AND (:crime IS NULL OR LOWER(p.crime) LIKE LOWER(CONCAT('%', CAST(:crime AS String), '%')))
            """)
    Page<Procedimento> listar(StatusProcedimento status, boolean somenteRemetidos,
                              Collection<TipoProcedimento> tipos, Integer ano, Long detentorId,
                              Long numero, String crime, Pageable pageable);

    // Contagem das abas com os mesmos filtros (menos a aba), numa consulta só: cada SUM conta as linhas da sua aba
    @Query("""
            SELECT COALESCE(SUM(CASE WHEN p.statusProcedimento = :emAndamento THEN 1 ELSE 0 END), 0) AS emAndamento,
                   COALESCE(SUM(CASE WHEN p.statusProcedimento = :arquivado THEN 1 ELSE 0 END), 0) AS arquivados,
                   COALESCE(SUM(CASE WHEN p.dataRemessaFinal IS NOT NULL THEN 1 ELSE 0 END), 0) AS remetidos,
                   COUNT(p) AS todos
            FROM Procedimento p
            WHERE p.tipoProcedimento IN :tipos
              AND (:ano IS NULL OR p.anoProcedimento = :ano)
              AND (:detentorId IS NULL OR p.detentorAtual.id = :detentorId)
              AND (:numero IS NULL OR p.numeroProcedimento = :numero)
              AND (:crime IS NULL OR LOWER(p.crime) LIKE LOWER(CONCAT('%', CAST(:crime AS String), '%')))
            """)
    ContagemAbasProcedimento contarPorAba(StatusProcedimento emAndamento, StatusProcedimento arquivado,
                                          Collection<TipoProcedimento> tipos, Integer ano, Long detentorId,
                                          Long numero, String crime);

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