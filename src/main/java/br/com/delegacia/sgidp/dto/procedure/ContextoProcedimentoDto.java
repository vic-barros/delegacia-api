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
        sb.append("=== DADOS DO PROCEDIMENTO ===\n");
        sb.append("ID: ").append(procedimento.id()).append("\n");
        sb.append("Tipo: ").append(procedimento.tipoProcedimento()).append("\n");
        sb.append("Número: ").append(procedimento.numeroProcedimento()).append("/").append(procedimento.anoProcedimento()).append("\n");
        sb.append("Crime: ").append(procedimento.crime()).append("\n");
        sb.append("Status Atual: ").append(procedimento.statusProcedimento()).append("\n");
        sb.append("Data de Abertura: ").append(procedimento.dataAbertura()).append("\n");
        
        sb.append("\n=== HISTÓRICO DE STATUS ===\n");
        if (historicoStatusProcedimento != null && !historicoStatusProcedimento.isEmpty()) {
            for (HistoricoStatusProcedimento h : historicoStatusProcedimento) {
                // statusAnterior é nulo apenas na primeira transição, registrada no cadastro do procedimento
                sb.append("Status Anterior: ").append(
                        h.getStatusAnterior() != null ? h.getStatusAnterior() : "Nenhum (cadastro inicial)")
                  .append("\nNovo status: ").append(h.getStatusNovo())
                  .append("\nData: ").append(h.getDataTransicao())
                  .append("\nResponsável: ").append(h.getResponsavel().getNome())
                  .append("\nMotivo: ").append(h.getMotivo())
                  .append("\n\n");
            }
        } else {
            sb.append("Nenhum histórico registrado.\n");
        }

        sb.append("\n=== HISTÓRICO DE REPASSES ===\n");
        if (historicoRepasses != null && !historicoRepasses.isEmpty()) {
            for (Repasse repasse : historicoRepasses) {
                sb.append("Status do repasse: ").append(repasse.getStatusRepasse());
                sb.append("\nData de solicitação: ").append(repasse.getDataSolicitacao());
                sb.append("\nSolicitante: ").append(repasse.getSolicitante().getNome());
                sb.append("\nDestinatário: ").append(repasse.getDestinatario().getNome());
                // dataResposta é nula enquanto o repasse estiver PENDENTE
                sb.append("\nData de resposta: ").append(
                        repasse.getDataResposta() != null ? repasse.getDataResposta() : "Aguardando resposta");
                // justificativaRecusa só existe quando o repasse foi RECUSADO
                sb.append("\nJustificativa de recusa: ").append(
                        repasse.getJustificativaRecusa() != null ? repasse.getJustificativaRecusa() : "Não se aplica");
                sb.append("\n\n");
            } 
        } else {
            sb.append("Nenhum histórico registrado.\n");
        }

        sb.append("\n=== HISTÓRICO DE OITIVAS ===\n");
        if (historicoOitivas != null && !historicoOitivas.isEmpty()) {
            for (Oitiva oitiva : historicoOitivas) {
                int contador = 1;
                sb.append("Data de cadastro: ").append(oitiva.getDataCadastro());
                sb.append("\nUsuário que cadastrou: ").append(oitiva.getCadastradoPor().getNome());
                sb.append("\nResponsável: ").append(oitiva.getResponsavel().getNome());
                sb.append("\nData e Hora: ").append(oitiva.getDataHora());
                sb.append("\nStatus: ").append(oitiva.getStatusOitiva());
                // alteradoPor é nulo até a primeira edição/remarcação/desmarcação/conclusão da oitiva
                sb.append("\nÚltima alteração por: ").append(
                        oitiva.getAlteradoPor() != null ? oitiva.getAlteradoPor().getNome() : "Nenhuma alteração registrada");
                // motivoCancelamento só é preenchido quando a oitiva é DESMARCADA
                sb.append("\nMotivo de cancelamento: ").append(
                        oitiva.getMotivoCancelamento() != null ? oitiva.getMotivoCancelamento() : "Não se aplica");
                for (OitivaParte oitivaParte : oitiva.getPartes()) {
                    sb.append("\n== Oitiva parte número " + contador + " ==\n");
                    sb.append("\nTipo de parte: ").append(oitivaParte.getTipoParte());
                    sb.append("\nNome da parte").append(oitivaParte.getNomeParte());
                    contador++;
                }
            }
            
        } else {
            sb.append("Nenhum histórico registrado.\n");
        }

        sb.append("\n=== LISTA DE VEÍCULOS ===\n");
        if (listaVeiculos != null && !listaVeiculos.isEmpty()) {
            for(Veiculo veiculo : listaVeiculos) {
				sb.append("Id do veículo: ").append(veiculo.getId());
                sb.append("\nTipo do veículo: ").append(veiculo.getTipoVeiculo());
                sb.append("\nModelo do veículo: ").append(veiculo.getModelo());
                sb.append("\nMarca do veículo: ").append(veiculo.getMarca());
                // placa, chassi e motor são opcionais (veículo pode estar sem placa ou com identificação adulterada)
                sb.append("\nPlaca: ").append(
                        veiculo.getPlaca() != null ? veiculo.getPlaca() : "Não informada");
                sb.append("\nChassi: ").append(
                        veiculo.getChassi() != null ? veiculo.getChassi() : "Não informado");
                sb.append("\nMotor: ").append(
                        veiculo.getMotor() != null ? veiculo.getMotor() : "Não informado");
                sb.append("\nCaracterísticas visuais: ").append(
                        veiculo.getCaracteristicasVisuais() != null ? veiculo.getCaracteristicasVisuais() : "Não informadas");
                sb.append("\nCor do veículo: ").append(veiculo.getCor());
                sb.append("\nStatus da perícia: ").append(veiculo.getStatusPericia());
                sb.append("\nSituação do veículo: ").append(veiculo.getSituacaoVeiculo());
                sb.append("\nObservações: ").append(
                        veiculo.getObservacoes() != null ? veiculo.getObservacoes() : "Nenhuma");

            }
            
        } else {
            sb.append("Nenhum veículo registrado.\n");
        }

        sb.append("\n=== HISTÓRICO DA SITUAÇÃO DOS VEÍCULOS ===\n");
        if (historicoSituacaoVeiculo != null && !historicoSituacaoVeiculo.isEmpty()) {
            for(HistoricoSituacaoVeiculo veiculo : historicoSituacaoVeiculo) {
				sb.append("Id do veículo: ").append(veiculo.getVeiculo().getId());
                sb.append("\nData de transição: ").append(veiculo.getDataTransicao());
                sb.append("\nresponsável: ").append(veiculo.getResponsavel().getNome());
                // situacaoAnterior é nula apenas no primeiro registro (cadastro do veículo)
                sb.append("\nSituação anterior: ").append(
                        veiculo.getSituacaoAnterior() != null ? veiculo.getSituacaoAnterior() : "Nenhuma (cadastro inicial)");
                sb.append("\nNova situação: ").append(veiculo.getSituacaoNova());
                sb.append("\nMotivo: ").append(veiculo.getMotivo());
            }
        } else {
            sb.append("Nenhum histórico registrado.\n");
        }
        
        return sb.toString();
    }}