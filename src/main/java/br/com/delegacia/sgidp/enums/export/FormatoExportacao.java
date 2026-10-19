package br.com.delegacia.sgidp.enums.export;

import lombok.Getter;

@Getter
public enum FormatoExportacao {

    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PDF("pdf", "application/pdf");

    private final String extensao;
    private final String tipoConteudo;

    FormatoExportacao(String extensao, String tipoConteudo) {
        this.extensao = extensao;
        this.tipoConteudo = tipoConteudo;
    }
}
