package br.com.delegacia.sgidp.service.vehicle;

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

// Planilha XLSX da exportação de veículos (Apache POI): todos os campos em colunas separadas,
// para filtrar e cruzar no Excel. Mesma estrutura da planilha de procedimentos
@Service
public class VeiculoXlsxService {

    private static final String[] COLUNAS = {"Lacre", "Tipo", "Marca", "Modelo", "Cor", "Placa", "Chassi", "Motor",
            "Situação", "Perícia", "Procedimento", "Data de entrada", "Características visuais", "Observações"};

    // Largura de cada coluna em caracteres (autoSizeColumn depende das fontes instaladas no servidor)
    private static final int[] LARGURAS = {17, 14, 14, 20, 12, 10, 20, 16, 11, 22, 14, 15, 40, 40};

    // Linhas 0 a 3: título, filtros, geração e totais; linha 4 em branco; cabeçalho da tabela na linha 5
    private static final int LINHA_CABECALHO = 5;

    public byte[] gerar(List<LinhaRelatorioVeiculo> linhas, String titulo, String filtros, String geracao,
                        String totais) {
        try (XSSFWorkbook planilha = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            Sheet aba = planilha.createSheet("Veículos");

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
            linhaTitulo.createCell(0).setCellValue(titulo);
            linhaTitulo.getCell(0).setCellStyle(estiloTitulo);
            aba.createRow(1).createCell(0).setCellValue(filtros);
            aba.createRow(2).createCell(0).setCellValue(geracao);
            aba.createRow(3).createCell(0).setCellValue(totais);

            Row cabecalho = aba.createRow(LINHA_CABECALHO);
            for (int i = 0; i < COLUNAS.length; i++) {
                cabecalho.createCell(i).setCellValue(COLUNAS[i]);
                cabecalho.getCell(i).setCellStyle(estiloCabecalho);
                aba.setColumnWidth(i, LARGURAS[i] * 256); // unidade do POI: 1/256 da largura de um caractere
            }

            int numeroLinha = LINHA_CABECALHO + 1;
            for (LinhaRelatorioVeiculo veiculo : linhas) {
                Row linha = aba.createRow(numeroLinha++);
                escreverTexto(linha, 0, veiculo.lacre());
                escreverTexto(linha, 1, veiculo.tipoVeiculo());
                escreverTexto(linha, 2, veiculo.marca());
                escreverTexto(linha, 3, veiculo.modelo());
                escreverTexto(linha, 4, veiculo.cor());
                escreverTexto(linha, 5, veiculo.placa());
                escreverTexto(linha, 6, veiculo.chassi());
                escreverTexto(linha, 7, veiculo.motor());
                escreverTexto(linha, 8, veiculo.situacao());
                escreverTexto(linha, 9, veiculo.pericia());
                escreverTexto(linha, 10, veiculo.procedimento());
                escreverData(linha, 11, veiculo.dataEntrada(), estiloData);
                escreverTexto(linha, 12, veiculo.caracteristicasVisuais());
                escreverTexto(linha, 13, veiculo.observacoes());
            }

            // Setinhas de filtro no cabeçalho e cabeçalho fixo ao rolar a planilha
            aba.setAutoFilter(new CellRangeAddress(LINHA_CABECALHO, numeroLinha - 1, 0, COLUNAS.length - 1));
            aba.createFreezePane(0, LINHA_CABECALHO + 1);

            planilha.write(saida);
            return saida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível gerar a planilha de veículos", e);
        }
    }

    // Campos opcionais (placa, chassi, observações...) nulos ficam com a célula vazia
    private void escreverTexto(Row linha, int coluna, String texto) {
        if (texto != null) {
            linha.createCell(coluna).setCellValue(texto);
        }
    }

    private void escreverData(Row linha, int coluna, LocalDate data, CellStyle estiloData) {
        if (data != null) {
            linha.createCell(coluna).setCellValue(data);
            linha.getCell(coluna).setCellStyle(estiloData);
        }
    }
}