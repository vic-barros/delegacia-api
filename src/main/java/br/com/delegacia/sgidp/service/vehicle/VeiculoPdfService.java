package br.com.delegacia.sgidp.service.vehicle;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.HeaderFooter;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Relatório PDF de veículos / carga (OpenPDF), em A4 paisagem. Mesma estrutura do PDF de procedimentos,
// com a coluna "Conferido" em branco e o campo de assinatura para a conferência física no pátio
@Service
public class VeiculoPdfService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] COLUNAS = {"Lacre", "Veículo", "Placa / Chassi", "Situação", "Perícia",
            "Procedimento", "Entrada", "Conferido"};

    // Larguras relativas das colunas (proporção entre elas, não medida)
    private static final float[] LARGURAS = {13, 26, 15, 9, 13, 10, 8, 7};

    private static final Font FONTE_TITULO = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font FONTE_TEXTO = new Font(Font.HELVETICA, 9);
    private static final Font FONTE_NEGRITO = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font FONTE_RODAPE = new Font(Font.HELVETICA, 8);

    private static final Color COR_CABECALHO = new Color(217, 217, 217);
    private static final Color COR_LINHA_ALTERNADA = new Color(242, 242, 242);

    public byte[] gerar(List<LinhaRelatorioVeiculo> linhas, String titulo, String filtros, String geracao,
                        String totais) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        // A4 deitado; margens em pontos (36 pt = 1,27 cm)
        Document documento = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(documento, saida);

        // Rodapé "Página N": o "true" pede para o OpenPDF acrescentar o número da página
        HeaderFooter rodape = new HeaderFooter(new Phrase("Página ", FONTE_RODAPE), true);
        rodape.setAlignment(Element.ALIGN_CENTER);
        rodape.setBorder(Rectangle.NO_BORDER);
        documento.setFooter(rodape);

        documento.open();
        documento.add(new Paragraph(titulo, FONTE_TITULO));
        documento.add(new Paragraph(filtros, FONTE_TEXTO));
        documento.add(new Paragraph(geracao, FONTE_TEXTO));
        Paragraph paragrafoTotais = new Paragraph(totais, FONTE_NEGRITO);
        paragrafoTotais.setSpacingAfter(10);
        documento.add(paragrafoTotais);

        PdfPTable tabela = new PdfPTable(LARGURAS);
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1); // a primeira linha é o cabeçalho: repete no topo de cada página

        for (String coluna : COLUNAS) {
            adicionarCelula(tabela, coluna, FONTE_NEGRITO, COR_CABECALHO);
        }

        boolean alternada = false;
        for (LinhaRelatorioVeiculo veiculo : linhas) {
            Color fundo = alternada ? COR_LINHA_ALTERNADA : null;
            adicionarCelula(tabela, veiculo.lacre(), FONTE_TEXTO, fundo);
            adicionarCelula(tabela, veiculo.tipoVeiculo() + " " + veiculo.marca() + " " + veiculo.modelo() + " - "
                    + veiculo.cor(), FONTE_TEXTO, fundo);
            adicionarCelula(tabela, identificacao(veiculo), FONTE_TEXTO, fundo);
            adicionarCelula(tabela, veiculo.situacao(), FONTE_TEXTO, fundo);
            adicionarCelula(tabela, veiculo.pericia(), FONTE_TEXTO, fundo);
            adicionarCelula(tabela, veiculo.procedimento(), FONTE_TEXTO, fundo);
            adicionarCelula(tabela, veiculo.dataEntrada() != null ? veiculo.dataEntrada().format(FORMATO_DATA) : null,
                    FONTE_TEXTO, fundo);
            adicionarCelula(tabela, "", FONTE_TEXTO, fundo); // "Conferido": em branco, marcado à caneta
            alternada = !alternada;
        }
        documento.add(tabela);

        // Fecho da conferência física
        Paragraph conferencia = new Paragraph(
                "Conferido por: ________________________________________    Matrícula: ______________    "
                        + "Data: ____/____/________    Assinatura: ______________________________", FONTE_TEXTO);
        conferencia.setSpacingBefore(24);
        documento.add(conferencia);

        documento.close(); // fecha o documento e grava tudo em "saida"
        return saida.toByteArray();
    }

    // Placa quando houver; se não o chassi (veículo sem placa, comum em apreensões); se não "-"
    private String identificacao(LinhaRelatorioVeiculo veiculo) {
        if (veiculo.placa() != null) {
            return veiculo.placa();
        }
        return veiculo.chassi() != null ? "Chassi " + veiculo.chassi() : null;
    }

    // Texto nulo vira "-"
    private void adicionarCelula(PdfPTable tabela, String texto, Font fonte, Color fundo) {
        PdfPCell celula = new PdfPCell(new Phrase(texto != null ? texto : "-", fonte));
        celula.setPadding(4);
        if (fundo != null) {
            celula.setBackgroundColor(fundo);
        }
        tabela.addCell(celula);
    }
}