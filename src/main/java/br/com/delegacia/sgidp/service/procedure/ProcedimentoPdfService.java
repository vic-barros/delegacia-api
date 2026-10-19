package br.com.delegacia.sgidp.service.procedure;

import br.com.delegacia.sgidp.model.procedure.Procedimento;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Relatório PDF da exportação de procedimentos (OpenPDF), em A4 paisagem.
// A PdfPTable quebra a página sozinha e repete o cabeçalho da tabela em cada página
@Service
public class ProcedimentoPdfService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] COLUNAS = {"Procedimento", "Crime", "Abertura", "Status", "Detentor atual",
            "Remessa final", "Protocolo"};

    // Larguras relativas das colunas (proporção entre elas, não medida): Crime fica com a maior parte
    private static final float[] LARGURAS = {12, 30, 9, 11, 18, 9, 11};

    private static final Font FONTE_TITULO = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font FONTE_TEXTO = new Font(Font.HELVETICA, 9);
    private static final Font FONTE_CABECALHO = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font FONTE_RODAPE = new Font(Font.HELVETICA, 8);

    private static final Color COR_CABECALHO = new Color(217, 217, 217);
    private static final Color COR_LINHA_ALTERNADA = new Color(242, 242, 242);

    public byte[] gerar(List<Procedimento> procedimentos, String filtros, String geracao) {
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
        documento.add(new Paragraph("Relatório de procedimentos", FONTE_TITULO));
        documento.add(new Paragraph(filtros, FONTE_TEXTO));
        Paragraph paragrafoGeracao = new Paragraph(geracao, FONTE_TEXTO);
        paragrafoGeracao.setSpacingAfter(10);
        documento.add(paragrafoGeracao);

        PdfPTable tabela = new PdfPTable(LARGURAS);
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1); // a primeira linha é o cabeçalho: repete no topo de cada página

        for (String coluna : COLUNAS) {
            adicionarCelula(tabela, coluna, FONTE_CABECALHO, COR_CABECALHO);
        }

        boolean alternada = false;
        for (Procedimento procedimento : procedimentos) {
            // Linhas zebradas para facilitar a leitura
            Color fundo = alternada ? COR_LINHA_ALTERNADA : null;
            adicionarCelula(tabela, procedimento.getTipoProcedimento() + " " + procedimento.getNumeroProcedimento()
                    + "/" + procedimento.getAnoProcedimento(), FONTE_TEXTO, fundo);
            adicionarCelula(tabela, procedimento.getCrime(), FONTE_TEXTO, fundo);
            adicionarCelula(tabela, formatar(procedimento.getDataAbertura()), FONTE_TEXTO, fundo);
            adicionarCelula(tabela, procedimento.getStatusProcedimento().getDescricao(), FONTE_TEXTO, fundo);
            adicionarCelula(tabela, procedimento.getDetentorAtual().getNome(), FONTE_TEXTO, fundo);
            adicionarCelula(tabela, formatar(procedimento.getDataRemessaFinal()), FONTE_TEXTO, fundo);
            adicionarCelula(tabela, procedimento.getProtocoloRemessaFinal(), FONTE_TEXTO, fundo);
            alternada = !alternada;
        }

        documento.add(tabela);
        documento.close(); // fecha o documento e grava tudo em "saida"
        return saida.toByteArray();
    }

    // Texto nulo vira "-" (ex.: remessa ainda não feita)
    private void adicionarCelula(PdfPTable tabela, String texto, Font fonte, Color fundo) {
        PdfPCell celula = new PdfPCell(new Phrase(texto != null ? texto : "-", fonte));
        celula.setPadding(4);
        if (fundo != null) {
            celula.setBackgroundColor(fundo);
        }
        tabela.addCell(celula);
    }

    private String formatar(LocalDate data) {
        return data != null ? data.format(FORMATO_DATA) : null;
    }
}