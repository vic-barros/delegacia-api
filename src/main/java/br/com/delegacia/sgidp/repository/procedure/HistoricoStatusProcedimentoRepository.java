package br.com.delegacia.sgidp.repository.procedure;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.delegacia.sgidp.model.procedure.HistoricoStatusProcedimento;

public interface HistoricoStatusProcedimentoRepository extends JpaRepository<HistoricoStatusProcedimento, Long> {
    
}