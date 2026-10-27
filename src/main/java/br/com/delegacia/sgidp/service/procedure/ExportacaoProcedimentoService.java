package br.com.delegacia.sgidp.service.procedure;

import br.com.delegacia.sgidp.dto.export.ArquivoExportadoResponseDto;
import br.com.delegacia.sgidp.enums.export.FormatoExportacao;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Os arquivos em si são montados em ProcedimentoXlsxService e ProcedimentoPdfService, aqui são as regras de negócio
@Service
@RequiredArgsConstructor
public class ExportacaoProcedimentoService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_NOME_ARQUIVO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private final ProcedimentoRepository procedimentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProcedimentoXlsxService procedimentoXlsxService;
    private final ProcedimentoPdfService procedimentoPdfService;

    @Transactional(readOnly = true)
    public ArquivoExportadoResponseDto exportar(FormatoExportacao formato, LocalDate dataInicio, LocalDate dataFim,
                                                List<TipoProcedimento> tipos, StatusProcedimento status, String login) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new RegraNegocioException("A data inicial não pode ser posterior à data final.");
        }

        // Sem tipo = todos (o IN da consulta não aceita lista vazia); EnumSet também descarta tipo repetido
        Set<TipoProcedimento> tiposFiltro = tipos == null || tipos.isEmpty()
                ? EnumSet.allOf(TipoProcedimento.class)
                : EnumSet.copyOf(tipos);

        List<Procedimento> procedimentos = procedimentoRepository.buscarParaExportacao(dataInicio, dataFim,
                tiposFiltro, status);

        if (procedimentos.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum procedimento encontrado com os filtros informados.");
        }

        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));

        // Cabeçalho do relatório: filtros usados e quem gerou, quando e quantos registros
        LocalDateTime agora = LocalDateTime.now();
        String filtros = "Filtros: " + descreverFiltros(dataInicio, dataFim, tiposFiltro, status);
        String geracao = "Gerado em " + agora.format(FORMATO_DATA_HORA) + " por " + usuario.getNome()
                + " | Total: " + procedimentos.size() + " procedimento(s)";

        byte[] conteudo = switch (formato) {
            case XLSX -> procedimentoXlsxService.gerar(procedimentos, filtros, geracao);
            case PDF -> procedimentoPdfService.gerar(procedimentos, filtros, geracao);
        };

        String nomeArquivo = "procedimentos-" + agora.format(FORMATO_NOME_ARQUIVO) + "." + formato.getExtensao();
        return new ArquivoExportadoResponseDto(nomeArquivo, formato.getTipoConteudo(), conteudo);
    }

    // Ex.: "abertura de 01/01/2025 a 31/12/2025; tipo Inquérito Policial (IP); status Em Andamento"
    // ou, com mais de um tipo, "tipos IP, APF"
    private String descreverFiltros(LocalDate dataInicio, LocalDate dataFim, Set<TipoProcedimento> tipos,
                                    StatusProcedimento status) {
        List<String> partes = new ArrayList<>();
        if (dataInicio != null && dataFim != null) {
            partes.add("abertura de " + dataInicio.format(FORMATO_DATA) + " a " + dataFim.format(FORMATO_DATA));
        } else if (dataInicio != null) {
            partes.add("abertura a partir de " + dataInicio.format(FORMATO_DATA));
        } else if (dataFim != null) {
            partes.add("abertura até " + dataFim.format(FORMATO_DATA));
        }
        if (tipos.size() == 1) {
            TipoProcedimento tipo = tipos.iterator().next();
            partes.add("tipo " + tipo.getDescricao() + " (" + tipo.name() + ")");
        } else if (tipos.size() < TipoProcedimento.values().length) {
            partes.add("tipos " + tipos.stream().map(TipoProcedimento::name).collect(Collectors.joining(", ")));
        }
        if (status != null) {
            partes.add("status " + status.getDescricao());
        }
        return partes.isEmpty() ? "nenhum (todos os procedimentos)" : String.join("; ", partes);
    }
}