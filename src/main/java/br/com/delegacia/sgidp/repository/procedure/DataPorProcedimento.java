package br.com.delegacia.sgidp.repository.procedure;

import java.time.LocalDateTime;

// Projeção por interface usada nas consultas "uma data por procedimento" (Minha mesa).
// Não é DTO: DTO é a resposta que vai para o front; isto é só o formato do resultado da consulta,
// usado dentro do service. O Spring preenche os getters pelos apelidos (AS procedimentoId, AS data)
public interface DataPorProcedimento {

    Long getProcedimentoId();

    LocalDateTime getData();
}
