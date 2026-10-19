package br.com.delegacia.sgidp.service.hearing;

import br.com.delegacia.sgidp.enums.hearing.StatusOitiva;
import br.com.delegacia.sgidp.enums.notification.TipoNotificacao;
import br.com.delegacia.sgidp.model.hearing.Oitiva;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.hearing.OitivaRepository;
import br.com.delegacia.sgidp.service.notification.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OitivaNotificacaoService {

    // Oitiva próxima: avisa as AGENDADAS das próximas 24 horas
    private static final long HORAS_ANTECEDENCIA = 24;

    // Sem baixa: AGENDADA cuja data passou há mais de 2 horas (folga para a oitiva acontecer)
    private static final long HORAS_TOLERANCIA_SEM_BAIXA = 2;

    // Avisos que dependem da data ou do responsável: deixam de valer na remarcação ou na troca de responsável
    private static final List<TipoNotificacao> AVISOS_DA_OITIVA =
            List.of(TipoNotificacao.OITIVA_PROXIMA, TipoNotificacao.OITIVA_SEM_BAIXA);

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final OitivaRepository oitivaRepository;
    private final NotificacaoService notificacaoService;

    // Tarefa agendada. Frequência no .env (NOTIFICACAO_CRON), padrão a cada 10 minutos.
    // Formato cron do Spring: segundo minuto hora dia mês dia-da-semana
    @Scheduled(cron = "${NOTIFICACAO_CRON:0 */10 * * * *}")
    @Transactional
    public void verificarOitivas() {
        LocalDateTime agora = LocalDateTime.now();
        avisarOitivasProximas(agora);
        avisarOitivasSemBaixa(agora);
    }

    // Chamado no agendamento e na troca de responsável. Quem agenda para si mesmo não é avisado
    public void avisarAtribuicao(Oitiva oitiva, Usuario quemAtribuiu) {
        if (oitiva.getResponsavel().getId().equals(quemAtribuiu.getId())) {
            return;
        }
        notificacaoService.notificar(oitiva.getResponsavel(), TipoNotificacao.OITIVA_ATRIBUIDA,
                "Nova oitiva atribuída a você por " + quemAtribuiu.getNome() + ": " + descrever(oitiva),
                oitiva.getId());
    }

    // Remarcação ou troca de responsável: apaga os avisos antigos para a tarefa gerar de novo
    public void apagarAvisos(Oitiva oitiva) {
        notificacaoService.apagar(AVISOS_DA_OITIVA, oitiva.getId());
    }

    // Concluída ou desmarcada: o assunto foi resolvido, os avisos pendentes viram lidos
    public void resolverAvisos(Oitiva oitiva) {
        for (TipoNotificacao tipo : List.of(TipoNotificacao.OITIVA_PROXIMA, TipoNotificacao.OITIVA_SEM_BAIXA,
                TipoNotificacao.OITIVA_ATRIBUIDA)) {
            notificacaoService.marcarComoLidas(tipo, oitiva.getId());
        }
    }

    private void avisarOitivasProximas(LocalDateTime agora) {
        List<Oitiva> proximas = oitivaRepository.findByStatusOitivaAndDataHoraBetween(
                StatusOitiva.AGENDADA, agora, agora.plusHours(HORAS_ANTECEDENCIA));
        for (Oitiva oitiva : proximas) {
            if (!notificacaoService.existe(TipoNotificacao.OITIVA_PROXIMA, oitiva.getId())) {
                notificacaoService.notificar(oitiva.getResponsavel(), TipoNotificacao.OITIVA_PROXIMA,
                        "Oitiva próxima: " + descrever(oitiva), oitiva.getId());
            }
        }
    }

    private void avisarOitivasSemBaixa(LocalDateTime agora) {
        List<Oitiva> vencidas = oitivaRepository.findByStatusOitivaAndDataHoraBefore(
                StatusOitiva.AGENDADA, agora.minusHours(HORAS_TOLERANCIA_SEM_BAIXA));
        for (Oitiva oitiva : vencidas) {
            if (!notificacaoService.existe(TipoNotificacao.OITIVA_SEM_BAIXA, oitiva.getId())) {
                notificacaoService.notificar(oitiva.getResponsavel(), TipoNotificacao.OITIVA_SEM_BAIXA,
                        "Oitiva sem baixa: " + descrever(oitiva) + ". Conclua ou desmarque.", oitiva.getId());
            }
        }
    }

    // Ex.: "20/11/2026 às 14:00 (IP 123/2026)"
    private String descrever(Oitiva oitiva) {
        Procedimento procedimento = oitiva.getProcedimento();
        return oitiva.getDataHora().format(FORMATO_DATA_HORA) + " (" + procedimento.getTipoProcedimento() + " "
                + procedimento.getNumeroProcedimento() + "/" + procedimento.getAnoProcedimento() + ")";
    }
}