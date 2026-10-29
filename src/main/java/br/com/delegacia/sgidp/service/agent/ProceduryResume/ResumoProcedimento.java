package br.com.delegacia.sgidp.service.agent.ProceduryResume;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.delegacia.sgidp.dto.agent.ResumoProcedimentoDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoResponseDto;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import br.com.delegacia.sgidp.model.hearing.Oitiva;
import br.com.delegacia.sgidp.model.hearing.OitivaParte;
import br.com.delegacia.sgidp.model.procedure.HistoricoStatusProcedimento;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.vehicle.HistoricoSituacaoVeiculo;
import br.com.delegacia.sgidp.model.vehicle.Veiculo;
import br.com.delegacia.sgidp.repository.delegation.RepasseRepository;
import br.com.delegacia.sgidp.repository.hearing.OitivaRepository;
import br.com.delegacia.sgidp.repository.procedure.HistoricoStatusProcedimentoRepository;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import br.com.delegacia.sgidp.repository.vehicle.HistoricoSituacaoVeiculoRepository;
import br.com.delegacia.sgidp.repository.vehicle.VeiculoRepository;

@Service 
public class ResumoProcedimento {
    private final ProcedimentoRepository procedimentoRepository;
    private final HistoricoStatusProcedimentoRepository statusProcedimentoRepository;
    private final RepasseRepository repasseRepository;
    private final OitivaRepository oitivaRepository;
    private final VeiculoRepository veiculoRepository;
    private final HistoricoSituacaoVeiculoRepository historicoSitaucaoVeiculoRepository;

    public ResumoProcedimento (
        ProcedimentoRepository procedimentoRepository,
        HistoricoStatusProcedimentoRepository statusProcedimentoRepository,
        RepasseRepository repasseRepository,
        OitivaRepository oitivaRepository,
        VeiculoRepository veiculoRepository,
        HistoricoSituacaoVeiculoRepository historicoSitaucaoVeiculoRepository
    ) {
        this.procedimentoRepository = procedimentoRepository;
        this.statusProcedimentoRepository = statusProcedimentoRepository;
        this.repasseRepository = repasseRepository;
        this.oitivaRepository = oitivaRepository;
        this.veiculoRepository = veiculoRepository;
        this.historicoSitaucaoVeiculoRepository = historicoSitaucaoVeiculoRepository;
    }

	@Transactional(readOnly = true)
    public String criarContextoProcedimento(Long procedimentoId) {
        // verifica se o procedimento existe
        Procedimento procedimento = procedimentoRepository.findById(procedimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado."));

        // armazena o histórico do status do procedimento
        List<HistoricoStatusProcedimento> historicoStatusProcedimento =
                statusProcedimentoRepository.findByProcedimentoIdOrderByDataTransicaoAsc(procedimentoId);

        // armazena o histórico de repasses do procedimento
        List<Repasse> historicoRepasses =
                repasseRepository.findByProcedimentoIdOrderByDataSolicitacaoAsc(procedimentoId);

        // armazena as oitivas relacionadas ao procedimeto
        List<Oitiva> historicoOitivas = 
                oitivaRepository.findByProcedimentoIdOrderByDataCadastroAsc(procedimentoId);

        // armazena os veículos relacionados ao procedimento
        List<Veiculo> listaVeiculos = 
                veiculoRepository.findByProcedimentoIdOrderByIdAsc(procedimentoId);


        // armazena o historico dos veículos
        List<HistoricoSituacaoVeiculo> historicoSituacaoVeiculos = 
                historicoSitaucaoVeiculoRepository.findByVeiculoProcedimentoIdOrderByDataTransicaoAsc(procedimentoId);

        ResumoProcedimentoDto contextoProcedimento = ResumoProcedimentoDto.de(
                ProcedimentoResponseDto.de(procedimento),
                historicoStatusProcedimento,
                historicoRepasses,
                historicoOitivas,
                listaVeiculos,
                historicoSituacaoVeiculos
        );

        String contextoFormatado = formatarParaString(contextoProcedimento);
        return contextoFormatado;
    }

    private static String formatarParaString(ResumoProcedimentoDto procedimentoDto) {
		StringBuilder sb = new StringBuilder();

		// ==================== DADOS DO PROCEDIMENTO ====================
		adicionarTitulo(sb, "DADOS DO PROCEDIMENTO");

		adicionarCampo(sb, "ID", procedimentoDto.procedimento().id());
		adicionarCampo(sb, "Tipo", procedimentoDto.procedimento().tipoProcedimento());
		adicionarCampo(
				sb,
				"Número",
				procedimentoDto.procedimento().numeroProcedimento() + "/" + procedimentoDto.procedimento().anoProcedimento()
		);
		adicionarCampo(sb, "Crime", procedimentoDto.procedimento().crime());
		adicionarCampo(sb, "Status atual", procedimentoDto.procedimento().statusProcedimento());
		adicionarCampo(sb, "Data de abertura", procedimentoDto.procedimento().dataAbertura());


		// ==================== HISTÓRICO DE STATUS ====================
		adicionarTitulo(sb, "HISTÓRICO DE STATUS");

		if (procedimentoDto.historicoStatusProcedimento() != null
				&& !procedimentoDto.historicoStatusProcedimento().isEmpty()) {

			for (int i = 0; i < procedimentoDto.historicoStatusProcedimento().size(); i++) {
				HistoricoStatusProcedimento historico =
						procedimentoDto.historicoStatusProcedimento().get(i);

				if (i > 0) {
					sb.append("\n");
				}

				sb.append("REGISTRO Nº ").append(i + 1).append("\n");

				adicionarCampo(
						sb,
						"Status anterior",
						historico.getStatusAnterior() != null
								? historico.getStatusAnterior()
								: "Nenhum (cadastro inicial)"
				);

				adicionarCampo(sb, "Novo status", historico.getStatusNovo());
				adicionarCampo(sb, "Data", historico.getDataTransicao());
				adicionarCampo(
						sb,
						"Responsável",
						historico.getResponsavel() != null
								? historico.getResponsavel().getNome()
								: "Não informado"
				);
				adicionarCampo(sb, "Motivo", historico.getMotivo());
			}
		} else {
			sb.append("Nenhum histórico registrado.\n");
		}


		// ==================== HISTÓRICO DE REPASSES ====================
		adicionarTitulo(sb, "HISTÓRICO DE REPASSES");

		if (procedimentoDto.historicoRepasses() != null && !procedimentoDto.historicoRepasses().isEmpty()) {

			for (int i = 0; i < procedimentoDto.historicoRepasses().size(); i++) {
				Repasse repasse = procedimentoDto.historicoRepasses().get(i);

				if (i > 0) {
					sb.append("\n");
				}

				sb.append("REPASSE Nº ").append(i + 1).append("\n");

				adicionarCampo(
						sb,
						"Status do repasse",
						repasse.getStatusRepasse()
				);

				adicionarCampo(
						sb,
						"Data de solicitação",
						repasse.getDataSolicitacao()
				);

				adicionarCampo(
						sb,
						"Solicitante",
						repasse.getSolicitante() != null
								? repasse.getSolicitante().getNome()
								: "Não informado"
				);

				adicionarCampo(
						sb,
						"Destinatário",
						repasse.getDestinatario() != null
								? repasse.getDestinatario().getNome()
								: "Não informado"
				);

				adicionarCampo(
						sb,
						"Data de resposta",
						repasse.getDataResposta() != null
								? repasse.getDataResposta()
								: "Aguardando resposta"
				);

				adicionarCampo(
						sb,
						"Justificativa de recusa",
						repasse.getJustificativaRecusa() != null
								&& !repasse.getJustificativaRecusa().isBlank()
								? repasse.getJustificativaRecusa()
								: "Não se aplica"
				);
			}
		} else {
			sb.append("Nenhum repasse registrado.\n");
		}


		// ==================== HISTÓRICO DE OITIVAS ====================
		adicionarTitulo(sb, "HISTÓRICO DE OITIVAS");

		if (procedimentoDto.historicoOitivas() != null && !procedimentoDto.historicoOitivas().isEmpty()) {

			for (int i = 0; i < procedimentoDto.historicoOitivas().size(); i++) {
				Oitiva oitiva = procedimentoDto.historicoOitivas().get(i);

				if (i > 0) {
					sb.append("\n");
				}

				// Numeração sequencial das oitivas.
				sb.append("OITIVA Nº ").append(i + 1).append("\n");

				adicionarCampo(
						sb,
						"Data de cadastro",
						oitiva.getDataCadastro()
				);

				adicionarCampo(
						sb,
						"Usuário que cadastrou",
						oitiva.getCadastradoPor() != null
								? oitiva.getCadastradoPor().getNome()
								: "Não informado"
				);

				adicionarCampo(
						sb,
						"Responsável",
						oitiva.getResponsavel() != null
								? oitiva.getResponsavel().getNome()
								: "Não informado"
				);

				adicionarCampo(
						sb,
						"Data e hora",
						oitiva.getDataHora()
				);

				adicionarCampo(
						sb,
						"Status",
						oitiva.getStatusOitiva()
				);

				adicionarCampo(
						sb,
						"Última alteração por",
						oitiva.getAlteradoPor() != null
								? oitiva.getAlteradoPor().getNome()
								: "Nenhuma alteração registrada"
				);

				adicionarCampo(
						sb,
						"Motivo de cancelamento",
						oitiva.getMotivoCancelamento() != null
								&& !oitiva.getMotivoCancelamento().isBlank()
								? oitiva.getMotivoCancelamento()
								: "Não se aplica"
				);

				// Partes associadas à oitiva atual.
				List<OitivaParte> partes = oitiva.getPartes();

				if (partes != null && !partes.isEmpty()) {
					for (int j = 0; j < partes.size(); j++) {
						OitivaParte parte = partes.get(j);

						sb.append("  PARTE Nº ")
								.append(j + 1)
								.append("\n");

						adicionarCampo(
								sb,
								"  Tipo de parte",
								parte.getTipoParte()
						);

						adicionarCampo(
								sb,
								"  Nome da parte",
								parte.getNomeParte()
						);
					}
				} else {
					sb.append("Nenhuma parte registrada.\n");
				}
			}
		} else {
			sb.append("Nenhuma oitiva registrada.\n");
		}


		// ==================== LISTA DE VEÍCULOS ====================
		adicionarTitulo(sb, "LISTA DE VEÍCULOS");

		if (procedimentoDto.listaVeiculos() != null && !procedimentoDto.listaVeiculos().isEmpty()) {

			for (int i = 0; i < procedimentoDto.listaVeiculos().size(); i++) {
				Veiculo veiculo = procedimentoDto.listaVeiculos().get(i);

				if (i > 0) {
					sb.append("\n");
				}

				sb.append("VEÍCULO Nº ").append(i + 1).append("\n");

				adicionarCampo(sb, "ID do veículo", veiculo.getId());
				adicionarCampo(sb, "Tipo do veículo", veiculo.getTipoVeiculo());
				adicionarCampo(sb, "Modelo do veículo", veiculo.getModelo());
				adicionarCampo(sb, "Marca do veículo", veiculo.getMarca());

				adicionarCampo(
						sb,
						"Placa",
						veiculo.getPlaca() != null
								? veiculo.getPlaca()
								: "Não informada"
				);

				adicionarCampo(
						sb,
						"Chassi",
						veiculo.getChassi() != null
								? veiculo.getChassi()
								: "Não informado"
				);

				adicionarCampo(
						sb,
						"Motor",
						veiculo.getMotor() != null
								? veiculo.getMotor()
								: "Não informado"
				);

				adicionarCampo(
						sb,
						"Características visuais",
						veiculo.getCaracteristicasVisuais() != null
								? veiculo.getCaracteristicasVisuais()
								: "Não informadas"
				);

				adicionarCampo(
						sb,
						"Cor do veículo",
						veiculo.getCor()
				);

				adicionarCampo(
						sb,
						"Status da perícia",
						veiculo.getStatusPericia()
				);

				adicionarCampo(
						sb,
						"Situação do veículo",
						veiculo.getSituacaoVeiculo()
				);

				adicionarCampo(
						sb,
						"Observações",
						veiculo.getObservacoes() != null
								? veiculo.getObservacoes()
								: "Nenhuma"
				);
			}
		} else {
			sb.append("Nenhum veículo registrado.\n");
		}


		// ==================== HISTÓRICO DA SITUAÇÃO DOS VEÍCULOS ====================
		adicionarTitulo(sb, "HISTÓRICO DA SITUAÇÃO DOS VEÍCULOS");

		if (procedimentoDto.historicoSituacaoVeiculo() != null
				&& !procedimentoDto.historicoSituacaoVeiculo().isEmpty()) {

			for (int i = 0; i < procedimentoDto.historicoSituacaoVeiculo().size(); i++) {
				HistoricoSituacaoVeiculo historico =
						procedimentoDto.historicoSituacaoVeiculo().get(i);

				if (i > 0) {
					sb.append("\n");
				}

				sb.append("TRANSIÇÃO Nº ").append(i + 1).append("\n");

				adicionarCampo(
						sb,
						"ID do veículo",
						historico.getVeiculo() != null
								? historico.getVeiculo().getId()
								: "Não informado"
				);

				adicionarCampo(
						sb,
						"Data de transição",
						historico.getDataTransicao()
				);

				adicionarCampo(
						sb,
						"Responsável",
						historico.getResponsavel() != null
								? historico.getResponsavel().getNome()
								: "Não informado"
				);

				adicionarCampo(
						sb,
						"Situação anterior",
						historico.getSituacaoAnterior() != null
								? historico.getSituacaoAnterior()
								: "Nenhuma (cadastro inicial)"
				);

				adicionarCampo(
						sb,
						"Nova situação",
						historico.getSituacaoNova()
				);

				adicionarCampo(
						sb,
						"Motivo",
						historico.getMotivo()
				);
			}
		} else {
			sb.append("Nenhum histórico registrado.\n");
		}

		return sb.toString();
	}

    /**
	 * Adiciona o título de uma seção, mantendo uma linha vazia
	 * entre as seções.
	 */
	private static void adicionarTitulo(StringBuilder sb, String titulo) {
		if (sb.length() > 0) {
			sb.append("\n");
		}

		sb.append("=== ")
				.append(titulo)
				.append(" ===\n");
	}


	/**
	 * Adiciona um campo por linha.
	 * Se o valor for nulo, utiliza uma indicação padrão.
	 */
	private static void adicionarCampo(
			StringBuilder sb,
			String nome,
			Object valor
	) {
		sb.append(nome)
				.append(": ")
				.append(valor != null ? valor : "Não informado")
				.append("\n");
	}

}
