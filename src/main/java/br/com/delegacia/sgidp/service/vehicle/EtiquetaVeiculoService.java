package br.com.delegacia.sgidp.service.vehicle;

import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.vehicle.HistoricoSituacaoVeiculo;
import br.com.delegacia.sgidp.model.vehicle.Veiculo;
import br.com.delegacia.sgidp.repository.vehicle.HistoricoSituacaoVeiculoRepository;
import br.com.delegacia.sgidp.repository.vehicle.VeiculoRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Etiqueta adesiva do veículo (100 x 50 mm): QR code com o link da ficha + lacre e dados básicos.
@Service
@RequiredArgsConstructor
public class EtiquetaVeiculoService {

    // O PDF mede em pontos (1 pt = 1/72 de polegada), não em milímetros.
    // MM converte: "100 * MM" = 100 milímetros em pontos. Assim o resto do código pensa em mm
    private static final float MM = 72f / 25.4f;
    private static final float LARGURA = 100 * MM;
    private static final float ALTURA = 50 * MM;
    private static final float MARGEM = 3 * MM;
    private static final float LADO_QR = ALTURA - 2 * MARGEM; // QR quadrado ocupando a altura útil (44 mm)

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VeiculoRepository veiculoRepository;
    private final HistoricoSituacaoVeiculoRepository historicoRepository;

    // Endereço do front que abre a ficha pelo lacre (muda por instalação; vem do .env)
    @Value("${ETIQUETA_URL_BASE:http://localhost:4200}")
    private String urlBase;

    // Nome da unidade no topo da etiqueta (ex.: "DELEGACIA DE POLÍCIA DE ..."); vazio = não mostra
    @Value("${ETIQUETA_UNIDADE:}")
    private String unidade;

    @Transactional(readOnly = true)
    public byte[] gerar(Long veiculoId) {
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado"));

        try (PDDocument documento = new PDDocument(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            PDPage pagina = new PDPage(new PDRectangle(LARGURA, ALTURA));
            documento.addPage(pagina);

            try (PDPageContentStream conteudo = new PDPageContentStream(documento, pagina)) {
                desenharQrCode(conteudo, urlBase + "/veiculos/lacre/" + veiculo.getLacre());
                desenharTextos(conteudo, veiculo);
            }

            documento.save(saida);
            return saida.toByteArray();
        } catch (IOException | WriterException e) {
            throw new IllegalStateException("Não foi possível gerar a etiqueta do veículo", e);
        }
    }

    // QR desenhado como quadradinhos vetoriais (fica nítido em qualquer impressora).
    // Correção de erro Q: continua legível com cerca de 25% danificado (sol, chuva, sujeira no pátio)
    private void desenharQrCode(PDPageContentStream conteudo, String texto) throws WriterException, IOException {
        BitMatrix matriz = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, 0, 0,
                Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q, EncodeHintType.MARGIN, 0));
        float modulo = LADO_QR / matriz.getWidth();
        float baseY = MARGEM;

        for (int x = 0; x < matriz.getWidth(); x++) {
            for (int y = 0; y < matriz.getHeight(); y++) {
                if (matriz.get(x, y)) {
                    // No PDF o eixo Y cresce para cima; na matriz, para baixo
                    conteudo.addRect(MARGEM + x * modulo, baseY + LADO_QR - (y + 1) * modulo, modulo, modulo);
                }
            }
        }
        conteudo.fill();
    }

    private void desenharTextos(PDPageContentStream conteudo, Veiculo veiculo) throws IOException {
        PDType1Font negrito = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        // O texto começa 3 mm à direita do QR. No PDF o Y conta de baixo para cima:
        // começamos perto do topo (ALTURA) e vamos descendo (y -= ...) a cada linha
        float x = MARGEM + LADO_QR + 3 * MM;
        float larguraTexto = LARGURA - x - MARGEM;
        float y = ALTURA - MARGEM - 7;

        if (!unidade.isBlank()) {
            escrever(conteudo, negrito, 6.5f, unidade.toUpperCase(), x, y, larguraTexto);
            y -= 8;
        }
        escrever(conteudo, negrito, 6.5f, "VEÍCULO APREENDIDO", x, y, larguraTexto);
        y -= 16;
        escrever(conteudo, negrito, 13.5f, veiculo.getLacre(), x, y, larguraTexto);

        // Dados em letra maior, para leitura de longe no pátio
        for (String linha : linhasDeDados(veiculo)) {
            y -= 12;
            escrever(conteudo, normal, 9f, linha, x, y, larguraTexto);
        }

        // Rodapé: data da impressão (identifica reimpressões) e instrução do QR
        escrever(conteudo, normal, 5.5f, "Impressa em " + LocalDateTime.now().format(FORMATO_DATA_HORA),
                x, MARGEM + 8, larguraTexto);
        escrever(conteudo, normal, 5.5f, "Leia o QR para abrir a ficha (requer login)", x, MARGEM + 1, larguraTexto);
    }

    private List<String> linhasDeDados(Veiculo veiculo) {
        List<String> linhas = new ArrayList<>();
        linhas.add(veiculo.getTipoVeiculo() + " " + veiculo.getMarca() + " " + veiculo.getModelo());
        linhas.add("Cor: " + veiculo.getCor());
        if (veiculo.getPlaca() != null) {
            linhas.add("Placa: " + veiculo.getPlaca());
        }
        Procedimento procedimento = veiculo.getProcedimento();
        linhas.add("Proc.: " + procedimento.getTipoProcedimento() + " " + procedimento.getNumeroProcedimento()
                + "/" + procedimento.getAnoProcedimento());
        // Data de entrada = primeira linha do histórico (o cadastro: null -> NA_DEPOL)
        List<HistoricoSituacaoVeiculo> historico = historicoRepository.findByVeiculoIdOrderByDataTransicaoAsc(veiculo.getId());
        if (!historico.isEmpty()) {
            linhas.add("Entrada: " + historico.get(0).getDataTransicao().format(FORMATO_DATA));
        }
        return linhas;
    }

    // Escreve uma linha cortando com "…" se não couber na largura disponível.
    // O PDF não quebra nem corta texto sozinho: getStringWidth mede o texto (em milésimos do
    // tamanho da fonte, por isso "/ 1000 * tamanho") e vamos tirando letras até caber
    private void escrever(PDPageContentStream conteudo, PDType1Font fonte, float tamanho, String texto,
                          float x, float y, float larguraMaxima) throws IOException {
        String ajustado = texto;
        while (ajustado.length() > 1 && fonte.getStringWidth(ajustado) / 1000 * tamanho > larguraMaxima) {
            ajustado = ajustado.substring(0, ajustado.length() - 2) + "…";
        }
        conteudo.beginText();
        conteudo.setFont(fonte, tamanho);
        conteudo.newLineAtOffset(x, y);
        conteudo.showText(ajustado);
        conteudo.endText();
    }
}