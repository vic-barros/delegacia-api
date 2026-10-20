package br.com.delegacia.sgidp.repository.procedure;

import java.time.LocalDateTime;

// Projeção por interface usada nas consultas "uma data por procedimento" (Minha mesa)
// Utiliza só getter e não usa dto, porque este é para o front
public interface DataPorProcedimento {

    Long getProcedimentoId();

    LocalDateTime getData();
}
