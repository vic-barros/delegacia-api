package br.com.delegacia.sgidp.dto.procedure;

import java.util.List;

import br.com.delegacia.sgidp.dto.user.UsuarioResponseDto;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import br.com.delegacia.sgidp.model.hearing.Oitiva;
import br.com.delegacia.sgidp.model.hearing.OitivaParte;
import br.com.delegacia.sgidp.model.procedure.HistoricoStatusProcedimento;
import br.com.delegacia.sgidp.model.vehicle.HistoricoSituacaoVeiculo;
import br.com.delegacia.sgidp.model.vehicle.Veiculo;

public record ContextoProcedimentoDto(
        ProcedimentoResponseDto procedimento,
        List<HistoricoStatusProcedimento> historicoStatusProcedimento,
        List<Repasse> historicoRepasses,
        List<Oitiva> historicoOitivas,
        List<Veiculo> listaVeiculos,
        List<HistoricoSituacaoVeiculo> historicoSituacaoVeiculo
) {
    public static ContextoProcedimentoDto de(ProcedimentoResponseDto procedimento,
                                            List<HistoricoStatusProcedimento> historicoStatusProcedimento,
                                            List<Repasse> historicoRepasses,
                                            List<Oitiva> historOitivas,
                                            List<Veiculo> listaVeiculos,
                                            List<HistoricoSituacaoVeiculo> historicoSituacaoVeiculo) {
        return new ContextoProcedimentoDto(procedimento, historicoStatusProcedimento, historicoRepasses, historOitivas, listaVeiculos, historicoSituacaoVeiculo);
    }

    
	public String formatarParaIA() {
		StringBuilder sb = new StringBuilder();

		// ==================== DADOS DO PROCEDIMENTO ====================
		adicionarTitulo(sb, "DADOS DO PROCEDIMENTO");

		adicionarCampo(sb, "ID", procedimento.id());
		adicionarCampo(sb, "Tipo", procedimento.tipoProcedimento());
		adicionarCampo(
				sb,
				"Número",
				procedimento.numeroProcedimento() + "/" + procedimento.anoProcedimento()
		);
		adicionarCampo(sb, "Crime", procedimento.crime());
		adicionarCampo(sb, "Status atual", procedimento.statusProcedimento());
		adicionarCampo(sb, "Data de abertura", procedimento.dataAbertura());


		// ==================== HISTÓRICO DE STATUS ====================
		adicionarTitulo(sb, "HISTÓRICO DE STATUS");

		if (historicoStatusProcedimento != null
				&& !historicoStatusProcedimento.isEmpty()) {

			for (int i = 0; i < historicoStatusProcedimento.size(); i++) {
				HistoricoStatusProcedimento historico =
						historicoStatusProcedimento.get(i);

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

		if (historicoRepasses != null && !historicoRepasses.isEmpty()) {

			for (int i = 0; i < historicoRepasses.size(); i++) {
				Repasse repasse = historicoRepasses.get(i);

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

		if (historicoOitivas != null && !historicoOitivas.isEmpty()) {

			for (int i = 0; i < historicoOitivas.size(); i++) {
				Oitiva oitiva = historicoOitivas.get(i);

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

		if (listaVeiculos != null && !listaVeiculos.isEmpty()) {

			for (int i = 0; i < listaVeiculos.size(); i++) {
				Veiculo veiculo = listaVeiculos.get(i);

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

		if (historicoSituacaoVeiculo != null
				&& !historicoSituacaoVeiculo.isEmpty()) {

			for (int i = 0; i < historicoSituacaoVeiculo.size(); i++) {
				HistoricoSituacaoVeiculo historico =
						historicoSituacaoVeiculo.get(i);

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
