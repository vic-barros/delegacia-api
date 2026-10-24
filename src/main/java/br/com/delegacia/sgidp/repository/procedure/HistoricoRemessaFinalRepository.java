package br.com.delegacia.sgidp.repository.procedure;

import br.com.delegacia.sgidp.model.procedure.HistoricoRemessaFinal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoRemessaFinalRepository
        extends JpaRepository<HistoricoRemessaFinal, Long> {

    List<HistoricoRemessaFinal> findByProcedimentoIdOrderByDataRegistroAsc(Long procedimentoId);
}