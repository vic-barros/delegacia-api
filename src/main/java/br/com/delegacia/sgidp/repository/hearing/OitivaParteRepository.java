package br.com.delegacia.sgidp.repository.hearing;

import br.com.delegacia.sgidp.model.hearing.OitivaParte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OitivaParteRepository extends JpaRepository<OitivaParte, Long> {

    // Participações de uma pessoa: em quais oitivas (e procedimentos) ela aparece e com qual papel.
    // JOIN FETCH traz oitiva e procedimento na mesma consulta
    @Query("""
            SELECT p FROM OitivaParte p
            JOIN FETCH p.oitiva o
            JOIN FETCH o.procedimento
            WHERE p.pessoa.id = :pessoaId
            ORDER BY o.dataHora DESC
            """)
    List<OitivaParte> buscarParticipacoes(Long pessoaId);
}
