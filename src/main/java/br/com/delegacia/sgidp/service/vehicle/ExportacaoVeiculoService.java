package br.com.delegacia.sgidp.service.vehicle;

import br.com.delegacia.sgidp.dto.export.ArquivoExportadoResponseDto;
import br.com.delegacia.sgidp.enums.examination.StatusPericia;
import br.com.delegacia.sgidp.enums.export.FormatoExportacao;
import br.com.delegacia.sgidp.enums.vehicle.SituacaoVeiculo;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.model.vehicle.Veiculo;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import br.com.delegacia.sgidp.repository.vehicle.HistoricoSituacaoVeiculoRepository;
import br.com.delegacia.sgidp.repository.vehicle.HistoricoSituacaoVeiculoRepository.DataEntradaVeiculo;
import br.com.delegacia.sgidp.repository.vehicle.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Exportação de veículos / relatório de carga: aplica filtros e regras, monta as linhas e escolhe o gerador.
// "Carga" = veículos ainda sob responsabilidade da delegacia (NA_DEPOL e EM_PATIO)
@Service
@RequiredArgsConstructor
public class ExportacaoVeiculoService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_NOME_ARQUIVO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private static final Set<SituacaoVeiculo> CARGA = EnumSet.of(SituacaoVeiculo.NA_DEPOL, SituacaoVeiculo.EM_PATIO);

    private final VeiculoRepository veiculoRepository;
    private final HistoricoSituacaoVeiculoRepository historicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VeiculoXlsxService veiculoXlsxService;
    private final VeiculoPdfService veiculoPdfService;

    @Transactional(readOnly = true)
    public ArquivoExportadoResponseDto exportar(FormatoExportacao formato, List<SituacaoVeiculo> situacoes,
                                                StatusPericia statusPericia, Long procedimentoId,
                                                LocalDate dataInicio, LocalDate dataFim, String login) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new RegraNegocioException("A data inicial não pode ser posterior à data final.");
        }

        // Sem situação no filtro = todas. EnumSet tira repetidas e mantém a ordem do enum
        Set<SituacaoVeiculo> situacoesFiltro = situacoes == null || situacoes.isEmpty()
                ? EnumSet.allOf(SituacaoVeiculo.class)
                : EnumSet.copyOf(situacoes);

        // Período de entrada: do início do primeiro dia até o fim do último (< meia-noite do dia seguinte)
        LocalDateTime entradaInicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime entradaFim = dataFim != null ? dataFim.plusDays(1).atStartOfDay() : null;

        List<Veiculo> veiculos = new ArrayList<>(veiculoRepository.buscarParaExportacao(situacoesFiltro, statusPericia,
                procedimentoId, entradaInicio, entradaFim));

        // UC16 (mesma regra dos procedimentos): sem resultado, avisa em vez de gerar um arquivo vazio
        if (veiculos.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum veículo encontrado com os filtros informados.");
        }

        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));

        // Ordem: situação (na ordem do enum: Na DEPOL, Em pátio, Devolvido, Descartado) e, dentro dela, lacre
        veiculos.sort(Comparator.comparing(Veiculo::getSituacaoVeiculo).thenComparing(Veiculo::getLacre));

        // Datas de entrada de todos numa consulta só (em vez de uma consulta por veículo)
        Map<Long, LocalDateTime> datasEntrada = historicoRepository
                .buscarDatasEntrada(veiculos.stream().map(Veiculo::getId).toList()).stream()
                .collect(Collectors.toMap(DataEntradaVeiculo::getVeiculoId, DataEntradaVeiculo::getDataEntrada));

        List<LinhaRelatorioVeiculo> linhas = veiculos.stream()
                .map(veiculo -> montarLinha(veiculo, datasEntrada.get(veiculo.getId())))
                .toList();

        // Cabeçalho: título, filtros, quem gerou e quando, e os totais por situação
        boolean relatorioDeCarga = situacoesFiltro.equals(CARGA);
        String titulo = relatorioDeCarga ? "Relatório de carga - veículos apreendidos" : "Relatório de veículos apreendidos";
        LocalDateTime agora = LocalDateTime.now();
        // Filtro por procedimento aparece como "IP 123/2026" (todas as linhas são dele)
        String procedimento = procedimentoId != null ? linhas.get(0).procedimento() : null;
        String filtros = "Filtros: " + descreverFiltros(situacoesFiltro, statusPericia, procedimento, dataInicio, dataFim);
        String geracao = "Gerado em " + agora.format(FORMATO_DATA_HORA) + " por " + usuario.getNome();
        String totais = descreverTotais(veiculos);

        byte[] conteudo = switch (formato) {
            case XLSX -> veiculoXlsxService.gerar(linhas, titulo, filtros, geracao, totais);
            case PDF -> veiculoPdfService.gerar(linhas, titulo, filtros, geracao, totais);
        };

        String nomeArquivo = (relatorioDeCarga ? "carga-veiculos-" : "veiculos-") + agora.format(FORMATO_NOME_ARQUIVO)
                + "." + formato.getExtensao();
        return new ArquivoExportadoResponseDto(nomeArquivo, formato.getTipoConteudo(), conteudo);
    }

    // Rótulo curto para o relatório (a descrição do enum é uma frase longa)
    static String rotulo(SituacaoVeiculo situacao) {
        return switch (situacao) {
            case NA_DEPOL -> "Na DEPOL";
            case EM_PATIO -> "Em pátio";
            case DEVOLVIDO -> "Devolvido";
            case DESCARTADO -> "Descartado";
        };
    }

    private LinhaRelatorioVeiculo montarLinha(Veiculo veiculo, LocalDateTime dataEntrada) {
        Procedimento procedimento = veiculo.getProcedimento();
        return new LinhaRelatorioVeiculo(
                veiculo.getLacre(),
                veiculo.getTipoVeiculo(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getCor(),
                veiculo.getPlaca(),
                veiculo.getChassi(),
                veiculo.getMotor(),
                rotulo(veiculo.getSituacaoVeiculo()),
                veiculo.getStatusPericia().getDescricao(),
                procedimento.getTipoProcedimento() + " " + procedimento.getNumeroProcedimento() + "/"
                        + procedimento.getAnoProcedimento(),
                dataEntrada != null ? dataEntrada.toLocalDate() : null,
                veiculo.getCaracteristicasVisuais(),
                veiculo.getObservacoes());
    }

    // Ex.: "Na DEPOL: 12 | Em pátio: 30 | Total: 42" (só as situações que aparecem no relatório)
    private String descreverTotais(List<Veiculo> veiculos) {
        Map<SituacaoVeiculo, Long> porSituacao = veiculos.stream()
                .collect(Collectors.groupingBy(Veiculo::getSituacaoVeiculo,
                        () -> new EnumMap<>(SituacaoVeiculo.class), Collectors.counting()));
        List<String> partes = new ArrayList<>();
        porSituacao.forEach((situacao, quantidade) -> partes.add(rotulo(situacao) + ": " + quantidade));
        partes.add("Total: " + veiculos.size());
        return String.join(" | ", partes);
    }

    private String descreverFiltros(Set<SituacaoVeiculo> situacoes, StatusPericia statusPericia, String procedimento,
                                    LocalDate dataInicio, LocalDate dataFim) {
        List<String> partes = new ArrayList<>();
        if (situacoes.size() < SituacaoVeiculo.values().length) {
            partes.add("situação " + situacoes.stream().map(ExportacaoVeiculoService::rotulo)
                    .collect(Collectors.joining(", ")));
        }
        if (statusPericia != null) {
            partes.add("perícia " + statusPericia.getDescricao());
        }
        if (procedimento != null) {
            partes.add("procedimento " + procedimento);
        }
        if (dataInicio != null && dataFim != null) {
            partes.add("entrada de " + dataInicio.format(FORMATO_DATA) + " a " + dataFim.format(FORMATO_DATA));
        } else if (dataInicio != null) {
            partes.add("entrada a partir de " + dataInicio.format(FORMATO_DATA));
        } else if (dataFim != null) {
            partes.add("entrada até " + dataFim.format(FORMATO_DATA));
        }
        return partes.isEmpty() ? "nenhum (todos os veículos)" : String.join("; ", partes);
    }
}