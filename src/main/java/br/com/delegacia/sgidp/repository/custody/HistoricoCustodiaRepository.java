package br.com.delegacia.sgidp.repository.custody;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.delegacia.sgidp.model.custody.HistoricoCustodia;;

public interface HistoricoCustodiaRepository
        extends JpaRepository<HistoricoCustodia, Long> {
}