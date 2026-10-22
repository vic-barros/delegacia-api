package br.com.delegacia.sgidp.service.dashboard;

import br.com.delegacia.sgidp.dto.dashboard.MinhaMesaResponseDto;
import br.com.delegacia.sgidp.dto.dashboard.ProcedimentoMesaResponseDto;
import br.com.delegacia.sgidp.enums.dashboard.ProximoPasso;
import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.enums.hearing.StatusOitiva;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import br.com.delegacia.sgidp.model.hearing.Oitiva;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.repository.custody.HistoricoCustodiaRepository;
import br.com.delegacia.sgidp.repository.delegation.RepasseRepository;
import br.com.delegacia.sgidp.repository.hearing.OitivaRepository;
import br.com.delegacia.sgidp.repository.procedure.DataPorProcedimento;
import br.com.delegacia.sgidp.repository.procedure.HistoricoStatusProcedimentoRepository;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import br.com.delegacia.sgidp.repository.vehicle.HistoricoSituacaoVeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

// Minha mesa (RF38): procedimentos em andamento na posse do usuário, com indicadores calculados na hora
@Service
@RequiredArgsConstructor
public class MinhaMesaService {

    // Sem nenhuma movimentação há mais que isso = "parado"
    static final int DIAS_PARADO = 60;

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DIA_HORA = DateTimeFormatter.ofPattern("dd/MM, HH:mm");

    private final ProcedimentoRepository procedimentoRepository;
    private final HistoricoCustodiaRepository historicoCustodiaRepository;
    private final HistoricoStatusProcedimentoRepository historicoStatusRepository;
    private final OitivaRepository oitivaRepository;
    private final RepasseRepository repasseRepository;
    private final HistoricoSituacaoVeiculoRepository historicoSituacaoRepository;

    @Transactional(readOnly = true)
    public MinhaMesaResponseDto consultar(String loginLogado) {
        List<Procedimento> procedimentos = procedimentoRepository
                .findByDetentorAtualLoginAndStatusProcedimentoOrderByDataAberturaAsc(loginLogado,
                        StatusProcedimento.EM_ANDAMENTO);
        if (procedimentos.isEmpty()) {
            return new MinhaMesaResponseDto(0, 0, 0, 0, List.of());
        }
        List<Long> ids = new ArrayList<>();
        for (Procedimento procedimento : procedimentos) {
            ids.add(procedimento.getId());
        }

        // Uma consulta por fonte para todos os procedimentos de uma vez (não uma por procedimento).
        // Cada resultado vira um mapa "id do procedimento -> informação", consultado depois pelo id
        Map<Long, LocalDateTime> naPosseDesde = paraMapa(historicoCustodiaRepository.buscarInicioCustodiaAtual(ids));

        // Última movimentação = a data mais recente entre todas as fontes (começa com a custódia)
        Map<Long, LocalDateTime> ultimaMovimentacao = new HashMap<>(naPosseDesde);
        juntarMaisRecente(ultimaMovimentacao, historicoStatusRepository.buscarUltimaTransicao(ids));
        juntarMaisRecente(ultimaMovimentacao, oitivaRepository.buscarUltimaMovimentacao(ids, StatusOitiva.CONCLUIDA));
        juntarMaisRecente(ultimaMovimentacao, repasseRepository.buscarUltimaMovimentacao(ids));
        juntarMaisRecente(ultimaMovimentacao, historicoSituacaoRepository.buscarUltimaMovimentacao(ids));

        // A lista vem ordenada por data/hora: a primeira oitiva de cada procedimento é a próxima,
        // por isso só guarda se o procedimento ainda não estiver no mapa
        Map<Long, Oitiva> proximaOitiva = new HashMap<>();
        for (Oitiva oitiva : oitivaRepository.findByProcedimentoIdInAndStatusOitivaOrderByDataHoraAsc(ids,
                StatusOitiva.AGENDADA)) {
            Long procedimentoId = oitiva.getProcedimento().getId();
            if (!proximaOitiva.containsKey(procedimentoId)) {
                proximaOitiva.put(procedimentoId, oitiva);
            }
        }

        // Só existe um repasse PENDENTE por procedimento (regra do UC08)
        Map<Long, Repasse> repassePendente = new HashMap<>();
        for (Repasse repasse : repasseRepository.findByProcedimentoIdInAndStatusRepasse(ids,
                StatusRepasse.PENDENTE)) {
            repassePendente.put(repasse.getProcedimento().getId(), repasse);
        }

        // Monta cada linha buscando nos mapas pelo id (null = "não tem") e já conta os chips
        LocalDateTime agora = LocalDateTime.now();
        List<ProcedimentoMesaResponseDto> linhas = new ArrayList<>();
        long comOitivaMarcada = 0;
        long repasseEmAndamento = 0;
        long parados = 0;
        for (Procedimento procedimento : procedimentos) {
            Long id = procedimento.getId();
            ProcedimentoMesaResponseDto linha = montarLinha(procedimento, naPosseDesde.get(id),
                    ultimaMovimentacao.get(id), proximaOitiva.get(id), repassePendente.get(id), agora);
            linhas.add(linha);
            if (linha.proximaOitivaId() != null) {
                comOitivaMarcada++;
            }
            if (linha.repassePendenteId() != null) {
                repasseEmAndamento++;
            }
            if (linha.parado()) {
                parados++;
            }
        }

        // Mais urgente primeiro (ordem do enum); empate: oitiva mais próxima (sem oitiva vai para o fim)
        // e depois o que está há mais tempo sem movimentação
        linhas.sort(Comparator.comparing(ProcedimentoMesaResponseDto::proximoPasso)
                .thenComparing(ProcedimentoMesaResponseDto::proximaOitiva,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ProcedimentoMesaResponseDto::ultimaMovimentacao));

        return new MinhaMesaResponseDto(linhas.size(), comOitivaMarcada, repasseEmAndamento, parados, linhas);
    }

    private ProcedimentoMesaResponseDto montarLinha(Procedimento procedimento, LocalDateTime naPosseDesde,
                                                    LocalDateTime ultimaMovimentacao, Oitiva oitiva,
                                                    Repasse repasse, LocalDateTime agora) {
        // Todo procedimento cadastrado tem custódia; a data de abertura é só uma garantia
        if (naPosseDesde == null) {
            naPosseDesde = procedimento.getDataAbertura().atStartOfDay();
        }
        if (ultimaMovimentacao == null) {
            ultimaMovimentacao = naPosseDesde;
        }
        LocalDate hoje = agora.toLocalDate();
        long diasNaPosse = ChronoUnit.DAYS.between(naPosseDesde.toLocalDate(), hoje);
        long diasSemMovimentacao = ChronoUnit.DAYS.between(ultimaMovimentacao.toLocalDate(), hoje);
        boolean parado = diasSemMovimentacao > DIAS_PARADO;
        boolean oitivaAtrasada = oitiva != null && oitiva.getDataHora().isBefore(agora);

        // Próximo passo: o primeiro que se aplica, na ordem de prioridade do enum
        ProximoPasso passo;
        String descricao;
        if (repasse != null) {
            passo = ProximoPasso.AGUARDANDO_ACEITE;
            descricao = "Aguardando aceite de " + repasse.getDestinatario().getNome();
        } else if (oitivaAtrasada) {
            passo = ProximoPasso.OITIVA_ATRASADA;
            descricao = "Oitiva sem baixa desde " + oitiva.getDataHora().format(DIA_HORA);
        } else if (oitiva != null) {
            passo = ProximoPasso.OITIVA_AGENDADA;
            descricao = "Oitiva " + descreverDia(oitiva.getDataHora(), hoje);
        } else if (parado) {
            passo = ProximoPasso.PARADO;
            descricao = "Parado há " + diasSemMovimentacao + " dias";
        } else {
            passo = ProximoPasso.NENHUM;
            descricao = "Sem pendências";
        }

        return new ProcedimentoMesaResponseDto(
                procedimento.getId(),
                procedimento.getTipoProcedimento() + " " + procedimento.getNumeroProcedimento() + "/"
                        + procedimento.getAnoProcedimento(),
                procedimento.getCrime(),
                naPosseDesde,
                diasNaPosse,
                oitiva != null ? oitiva.getId() : null,
                oitiva != null ? oitiva.getDataHora() : null,
                oitivaAtrasada,
                repasse != null ? repasse.getId() : null,
                repasse != null ? repasse.getDestinatario().getNome() : null,
                ultimaMovimentacao,
                diasSemMovimentacao,
                parado,
                passo,
                descricao);
    }

    // "hoje, 16:30", "amanhã, 09:00" ou "14/10, 09:00"
    private String descreverDia(LocalDateTime dataHora, LocalDate hoje) {
        if (dataHora.toLocalDate().equals(hoje)) {
            return "hoje, " + dataHora.format(HORA);
        }
        if (dataHora.toLocalDate().equals(hoje.plusDays(1))) {
            return "amanhã, " + dataHora.format(HORA);
        }
        return dataHora.format(DIA_HORA);
    }

    // Lista de pares (id, data) -> mapa "id -> data"
    private Map<Long, LocalDateTime> paraMapa(List<DataPorProcedimento> datas) {
        Map<Long, LocalDateTime> mapa = new HashMap<>();
        for (DataPorProcedimento item : datas) {
            mapa.put(item.getProcedimentoId(), item.getData());
        }
        return mapa;
    }

    // Guarda no mapa a data mais recente de cada procedimento
    private void juntarMaisRecente(Map<Long, LocalDateTime> mapa, List<DataPorProcedimento> datas) {
        for (DataPorProcedimento item : datas) {
            LocalDateTime atual = mapa.get(item.getProcedimentoId());
            // Ainda não tinha data para esse procedimento, ou a nova é mais recente: guarda a nova
            if (atual == null || item.getData().isAfter(atual)) {
                mapa.put(item.getProcedimentoId(), item.getData());
            }
        }
    }
}