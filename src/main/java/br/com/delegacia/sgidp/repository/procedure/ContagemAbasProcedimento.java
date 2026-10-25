package br.com.delegacia.sgidp.repository.procedure;

// Projeção da contagem das abas: cada getter recebe a coluna de mesmo nome
public interface ContagemAbasProcedimento {

    Long getEmAndamento();

    Long getArquivados();

    Long getRemetidos();

    Long getTodos();
}
