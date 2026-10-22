package br.com.delegacia.sgidp.service.dashboard;

import br.com.delegacia.sgidp.dto.dashboard.PainelResponseDto;
import br.com.delegacia.sgidp.dto.dashboard.ProcedimentoMesaResponseDto;
import br.com.delegacia.sgidp.dto.dashboard.RepassePainelResponseDto;
import br.com.delegacia.sgidp.dto.dashboard.VeiculoPainelResponseDto;
import br.com.delegacia.sgidp.dto.hearing.OitivaResponseDto;
import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.enums.examination.StatusPericia;
import br.com.delegacia.sgidp.enums.hearing.StatusOitiva;
import br.com.delegacia.sgidp.enums.vehicle.SituacaoVeiculo;
import br.com.delegacia.sgidp.repository.delegation.RepasseRepository;
import br.com.delegacia.sgidp.repository.vehicle.VeiculoRepository;
import br.com.delegacia.sgidp.service.hearing.OitivaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// Painel inicial (RF37): junta indicadores que já existem; nada é salvo
@Service
@RequiredArgsConstructor
public class PainelService {

    private final OitivaService oitivaService;
    private final MinhaMesaService minhaMesaService;
    private final RepasseRepository repasseRepository;
    private final VeiculoRepository veiculoRepository;

    @Transactional(readOnly = true)
    public PainelResponseDto consultar(String loginLogado) {
        // Reaproveita a listagem de oitivas: hoje, do usuário logado (minhas = true), sem as desmarcadas
        LocalDate hoje = LocalDate.now();
        List<OitivaResponseDto> oitivasHoje = oitivaService
                .listar(hoje, hoje, null, null, null, true, false, loginLogado)
                .stream()
                .filter(oitiva -> oitiva.status() != StatusOitiva.DESMARCADA)
                .toList();

        List<RepassePainelResponseDto> recebidos = repasseRepository
                .findByDestinatarioLoginAndStatusRepasseOrderByDataSolicitacaoAsc(loginLogado, StatusRepasse.PENDENTE)
                .stream().map(RepassePainelResponseDto::de).toList();
        List<RepassePainelResponseDto> enviados = repasseRepository
                .findBySolicitanteLoginAndStatusRepasseOrderByDataSolicitacaoAsc(loginLogado, StatusRepasse.PENDENTE)
                .stream().map(RepassePainelResponseDto::de).toList();

        // Card "Veículos nesta delegacia": só os que estão fisicamente na DEPOL (NA_DEPOL)
        VeiculoPainelResponseDto veiculos = new VeiculoPainelResponseDto(
                veiculoRepository.countBySituacaoVeiculo(SituacaoVeiculo.NA_DEPOL),
                veiculoRepository.countBySituacaoVeiculoAndStatusPericia(SituacaoVeiculo.NA_DEPOL,
                        StatusPericia.SEM_PERICIA),
                veiculoRepository.countBySituacaoVeiculoAndStatusPericia(SituacaoVeiculo.NA_DEPOL,
                        StatusPericia.PERICIA_EM_ANDAMENTO));

        // Reaproveita a Minha mesa: os parados são as linhas marcadas como paradas
        List<ProcedimentoMesaResponseDto> parados = minhaMesaService.consultar(loginLogado)
                .procedimentos().stream()
                .filter(ProcedimentoMesaResponseDto::parado)
                .toList();

        return new PainelResponseDto(oitivasHoje, recebidos, enviados, veiculos, parados);
    }
}