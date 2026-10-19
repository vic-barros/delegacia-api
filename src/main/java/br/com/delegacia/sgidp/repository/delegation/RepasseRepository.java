package br.com.delegacia.sgidp.repository.delegation;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.model.delegation.Repasse;

public interface RepasseRepository extends JpaRepository<Repasse, Long> {

    boolean existsByProcedimentoIdAndStatusRepasse(Long procedimentoId, StatusRepasse statusRepasse);
    List<Repasse> findByProcedimentoIdOrderByDataSolicitacaoAsc(Long procedimentoId);
}
