package br.com.delegacia.sgidp.service.vehicle;

import java.time.LocalDate;

// Uma linha do relatório de veículos, já com os textos prontos (rótulo da situação, "IP 123/2026", data de entrada).
// Assim os geradores de XLSX e PDF só escrevem, sem regra nenhuma. Visível só dentro do pacote service.vehicle, por isso não é um DTO
record LinhaRelatorioVeiculo(
        String lacre,
        String tipoVeiculo,
        String marca,
        String modelo,
        String cor,
        String placa,
        String chassi,
        String motor,
        String situacao,
        String pericia,
        String procedimento,
        LocalDate dataEntrada,
        String caracteristicasVisuais,
        String observacoes
) {
}