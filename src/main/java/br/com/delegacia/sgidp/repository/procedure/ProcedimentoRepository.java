package br.com.delegacia.sgidp.repository.procedure;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;

public interface ProcedimentoRepository
        extends JpaRepository<Procedimento, Long> {

    boolean existsByTipoProcedimentoAndNumeroProcedimentoAndAnoProcedimento(
        TipoProcedimento tipoProcedimento,
        Long numeroProcedimento,
        Integer anoProcedimento
    );
}