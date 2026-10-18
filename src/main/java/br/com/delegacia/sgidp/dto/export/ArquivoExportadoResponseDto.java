package br.com.delegacia.sgidp.dto.export;

// Arquivo gerado numa exportação (procedimentos ou veículos): o controller só repassa nome, tipo e bytes
public record ArquivoExportadoResponseDto(
        String nomeArquivo,
        String tipoConteudo,
        byte[] conteudo
) {
}
