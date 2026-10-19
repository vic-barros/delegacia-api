package br.com.delegacia.sgidp.service.procedure;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import br.com.delegacia.sgidp.dto.procedure.ContextoProcedimentoDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoResponseDto;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import br.com.delegacia.sgidp.model.hearing.Oitiva;
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
public class ResumoProcedimentoService {
    private final ChatClient chatClient;
    private final ProcedimentoRepository procedimentoRepository;
    private final HistoricoStatusProcedimentoRepository statusProcedimentoRepository;
    private final RepasseRepository repasseRepository;
    private final OitivaRepository oitivaRepository;
    private final VeiculoRepository veiculoRepository;
    private final HistoricoSituacaoVeiculoRepository historicoSitaucaoVeiculoRepository;

    public ResumoProcedimentoService(ChatClient.Builder builder,
                                    ProcedimentoRepository procedimentoRepository,
                                    HistoricoStatusProcedimentoRepository statusProcedimentoRepository,
                                    RepasseRepository repasseRepository,
                                    OitivaRepository oitivaRepository,
                                    VeiculoRepository veiculoRepository,
                                    HistoricoSituacaoVeiculoRepository historicoSitaucaoVeiculoRepository) {
        this.chatClient = builder.build();
        this.procedimentoRepository = procedimentoRepository;
        this.statusProcedimentoRepository = statusProcedimentoRepository;
        this.repasseRepository = repasseRepository;
        this.oitivaRepository = oitivaRepository;
        this.veiculoRepository = veiculoRepository;
        this.historicoSitaucaoVeiculoRepository = historicoSitaucaoVeiculoRepository;
    }

    public String ask(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    public String criarContexto(Long procedimentoId) {
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

        ContextoProcedimentoDto contextoProcedimento = ContextoProcedimentoDto.de(
                ProcedimentoResponseDto.de(procedimento),
                historicoStatusProcedimento,
                historicoRepasses,
                historicoOitivas,
                listaVeiculos,
                historicoSituacaoVeiculos
        );

        return contextoProcedimento.formatarParaIA();
    }
}
