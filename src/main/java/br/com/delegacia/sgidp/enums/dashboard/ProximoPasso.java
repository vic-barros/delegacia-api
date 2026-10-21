package br.com.delegacia.sgidp.enums.dashboard;

// Indicativo da Minha mesa, calculado na consulta (não é salvo no banco).
// A ordem dos valores é a prioridade: o primeiro que se aplica ao procedimento é o próximo passo
public enum ProximoPasso {
    AGUARDANDO_ACEITE,
    OITIVA_ATRASADA,
    OITIVA_AGENDADA,
    PARADO,
    NENHUM
}
