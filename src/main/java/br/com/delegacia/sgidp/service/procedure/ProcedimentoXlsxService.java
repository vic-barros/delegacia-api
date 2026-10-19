package br.com.delegacia.sgidp.service.procedure;

import br.com.delegacia.sgidp.model.procedure.Procedimento;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

// Planilha XLSX da exportação de procedimentos (Apache POI).
// Datas e números vão como valores de verdade (não texto), para o Excel conseguir ordenar e filtrar
@Service
public class ProcedimentoXlsxService {

    private static final String[] COLUNAS = {"Tipo", "Número", "Ano", "Crime", "Data de abertura", "Status",
            "Detentor atual", "Remessa final", "Protocolo da remessa"};

    // Largura de cada coluna em caracteres (autoSizeColumn depende das fontes instaladas no servidor)
    private static final int[] LARGURAS = {8, 10, 7, 40, 16, 14, 28, 15, 22};

    // Linhas 0 a 2: título, filtros e geração; linha 3 em branco; cabeçalho da tabela na linha 4
    private static final int LINHA_CABECALHO = 4;

    public byte[] gerar(List<Procedimento> procedimentos, String filtros, String geracao) {
        try (XSSFWorkbook planilha = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            Sheet aba = planilha.createSheet("Procedimentos");

            // Estilos: cada CellStyle é criado uma vez e reaproveitado (o Excel tem limite de estilos por arquivo)
            Font fonteTitulo = planilha.createFont();
            fonteTitulo.setBold(true);
            fonteTitulo.setFontHeightInPoints((short) 14);
            CellStyle estiloTitulo = planilha.createCellStyle();
            estiloTitulo.setFont(fonteTitulo);

            Font fonteNegrito = planilha.createFont();
            fonteNegrito.setBold(true);
            CellStyle estiloCabecalho = planilha.createCellStyle();
            estiloCabecalho.setFont(fonteNegrito);
            estiloCabecalho.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            estiloCabecalho.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle estiloData = planilha.createCellStyle();
            estiloData.setDataFormat(planilha.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));

            Row linhaTitulo = aba.createRow(0);
            linhaTitulo.createCell(0).setCellValue("Relatório de procedimentos");
            linhaTitulo.getCell(0).setCellStyle(estiloTitulo);
            aba.createRow(1).createCell(0).setCellValue(filtros);
            aba.createRow(2).createCell(0).setCellValue(geracao);

            Row cabecalho = aba.createRow(LINHA_CABECALHO);
            for (int i = 0; i < COLUNAS.length; i++) {
                cabecalho.createCell(i).setCellValue(COLUNAS[i]);
                cabecalho.getCell(i).setCellStyle(estiloCabecalho);
                aba.setColumnWidth(i, LARGURAS[i] * 256); // unidade do POI: 1/256 da largura de um caractere
            }

            int numeroLinha = LINHA_CABECALHO + 1;
            for (Procedimento procedimento : procedimentos) {
                Row linha = aba.createRow(numeroLinha++);
                linha.createCell(0).setCellValue(procedimento.getTipoProcedimento().name());
                linha.createCell(1).setCellValue(procedimento.getNumeroProcedimento());
                linha.createCell(2).setCellValue(procedimento.getAnoProcedimento());
                linha.createCell(3).setCellValue(procedimento.getCrime());
                escreverData(linha, 4, procedimento.getDataAbertura(), estiloData);
                linha.createCell(5).setCellValue(procedimento.getStatusProcedimento().getDescricao());
                linha.createCell(6).setCellValue(procedimento.getDetentorAtual().getNome());
                escreverData(linha, 7, procedimento.getDataRemessaFinal(), estiloData);
                if (procedimento.getProtocoloRemessaFinal() != null) {
                    linha.createCell(8).setCellValue(procedimento.getProtocoloRemessaFinal());
                }
            }

            // Setinhas de filtro no cabeçalho e cabeçalho fixo ao rolar a planilha
            aba.setAutoFilter(new CellRangeAddress(LINHA_CABECALHO, numeroLinha - 1, 0, COLUNAS.length - 1));
            aba.createFreezePane(0, LINHA_CABECALHO + 1);

            planilha.write(saida);
            return saida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível gerar a planilha de procedimentos", e);
        }
    }

    // Data nula (ex.: remessa ainda não feita) fica com a célula vazia
    private void escreverData(Row linha, int coluna, LocalDate data, CellStyle estiloData) {
        if (data != null) {
            linha.createCell(coluna).setCellValue(data);
            linha.getCell(coluna).setCellStyle(estiloData);
        }
    }
}