package br.com.delegacia.sgidp.service.vehicle;

import br.com.delegacia.sgidp.dto.vehicle.*;
import br.com.delegacia.sgidp.enums.vehicle.SituacaoVeiculo;
import br.com.delegacia.sgidp.exception.ConfirmacaoNecessariaException;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.model.vehicle.HistoricoSituacaoVeiculo;
import br.com.delegacia.sgidp.model.vehicle.Veiculo;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import br.com.delegacia.sgidp.repository.vehicle.HistoricoSituacaoVeiculoRepository;
import br.com.delegacia.sgidp.repository.vehicle.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    // Veículo "ativo" = ainda sob guarda da polícia. Só esses contam para o aviso de duplicidade
    private static final Set<SituacaoVeiculo> SITUACOES_ATIVAS =
            Set.of(SituacaoVeiculo.NA_DEPOL, SituacaoVeiculo.EM_PATIO);

    // Situações normalmente finais: sair delas exige confirmação
    private static final Set<SituacaoVeiculo> SITUACOES_FINAIS =
            Set.of(SituacaoVeiculo.DEVOLVIDO, SituacaoVeiculo.DESCARTADO);
    private final VeiculoRepository veiculoRepository;
    private final HistoricoSituacaoVeiculoRepository historicoRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final UsuarioRepository usuarioRepository;

    // RF24 / UC18 - Cadastro: gera o lacre, nasce NA_DEPOL e registra o primeiro histórico (null -> NA_DEPOL)
    @Transactional
    public VeiculoResponseDto cadastrar(VeiculoCadastroRequestDto dados, String loginLogado) {
        Procedimento procedimento = procedimentoRepository.findById(dados.procedimentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado"));

        Usuario responsavel = buscarUsuarioLogado(loginLogado);

        String placa = normalizar(dados.placa());
        String chassi = normalizar(dados.chassi());
        String motor = normalizar(dados.motor());
        verificarDuplicidade(placa, chassi, motor, dados.confirmarDuplicidade());

        Veiculo veiculo = new Veiculo();
        veiculo.setTipoVeiculo(dados.tipoVeiculo().trim());
        veiculo.setMarca(dados.marca().trim());
        veiculo.setModelo(dados.modelo().trim());
        veiculo.setCor(dados.cor().trim());
        veiculo.setPlaca(placa);
        veiculo.setChassi(chassi);
        veiculo.setMotor(motor);
        veiculo.setCaracteristicasVisuais(dados.caracteristicasVisuais());
        veiculo.setObservacoes(dados.observacoes());
        veiculo.setStatusPericia(dados.statusPericia());
        veiculo.setProcedimento(procedimento);
        veiculo.definirLacre(gerarLacre());
        // situacaoVeiculo já começa NA_DEPOL na entidade
        veiculoRepository.save(veiculo);

        historicoRepository.save(new HistoricoSituacaoVeiculo(
                null, SituacaoVeiculo.NA_DEPOL, "Cadastro do veículos",
                veiculo, responsavel));

        return VeiculoResponseDto.de(veiculo);
    }

    // RF25 - Consulta com filtros opcionais (todos nulos = lista tudo)
    @Transactional(readOnly = true)
    public List<VeiculoResponseDto> listar(String lacre, String placa, String chassi,
                                           String motor,
                                           SituacaoVeiculo situacao,
                                           Long procedimentoId) {
        return veiculoRepository.buscar(normalizar(lacre), normalizar(placa),
                        normalizar(chassi), normalizar(motor), situacao, procedimentoId)
                .stream()
                .map(VeiculoResponseDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public VeiculoResponseDto buscarPorId(Long id) {
        return VeiculoResponseDto.de(buscarVeiculo(id));
    }

    @Transactional(readOnly = true)
    public List<HistoricoSituacaoVeiculoResponseDto> listarHistorico(Long id) {
        buscarVeiculo(id);
        return historicoRepository.findByVeiculoIdOrderByDataTransicaoAsc(id).stream()
                .map(HistoricoSituacaoVeiculoResponseDto::de)
                .toList();
    }

    // RF26 - Edição de dados descritivos e perícia (não rastreada no MVP; ver "Melhorias futuras")
    @Transactional
    public VeiculoResponseDto editar(Long id, VeiculoEdicaoRequestDto dados) {
        Veiculo veiculo = buscarVeiculo(id);

        String placa = normalizar(dados.placa());
        String chassi = normalizar(dados.chassi());
        String motor = normalizar(dados.motor());

        verificarDuplicidade(
                Objects.equals(placa, veiculo.getPlaca()) ? null : placa,
                Objects.equals(chassi, veiculo.getChassi()) ? null : chassi,
                Objects.equals(motor, veiculo.getMotor()) ? null : motor,
                dados.confirmarDuplicidade());

        veiculo.setTipoVeiculo(dados.tipoVeiculo().trim());
        veiculo.setMarca(dados.marca().trim());
        veiculo.setModelo(dados.modelo().trim());
        veiculo.setCor(dados.cor().trim());
        veiculo.setPlaca(placa);
        veiculo.setChassi(chassi);
        veiculo.setMotor(motor);
        veiculo.setCaracteristicasVisuais(dados.caracteristicasVisuais());
        veiculo.setObservacoes(dados.observacoes());
        veiculo.setStatusPericia(dados.statusPericia());
        // Sem save: objeto carregado dentro do @Transactional (dirty checking)
        return VeiculoResponseDto.de(veiculo);
    }

    // RF27 - Mudança de situação: muda o veículo e grava o histórico na mesma transação
    @Transactional
    public VeiculoResponseDto alterarSituacao(Long id, VeiculoSituacaoRequestDto dados, String loginLogado) {
        Veiculo veiculo = buscarVeiculo(id);
        SituacaoVeiculo atual = veiculo.getSituacaoVeiculo();

        if (atual == dados.situacaoNova()) {
            throw new RegraNegocioException("O veículo já está na situação " + atual);
        }
        // Transição não é bloqueada, só pede confirmação ao sair de uma situação final
        if (SITUACOES_FINAIS.contains(atual) && !Boolean.TRUE.equals(dados.confirmarReversao())) {
            throw new ConfirmacaoNecessariaException(
                    "O veículo está " + atual + ". Confirme para alterar a situação mesmo assim.",
                    "confirmarReversao");
        }

        Usuario responsavel = buscarUsuarioLogado(loginLogado);
        historicoRepository.save(new HistoricoSituacaoVeiculo(
                atual, dados.situacaoNova(), dados.motivo().trim(), veiculo, responsavel));
        veiculo.setSituacaoVeiculo(dados.situacaoNova());

        return VeiculoResponseDto.de(veiculo);
    }

    // métodos auxiliares

    private Veiculo buscarVeiculo(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado"));
    }

    private Usuario buscarUsuarioLogado(String login) {
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));
    }

    // Formato LAC-AAAA-NNNNNN
    private String gerarLacre() {
        return String.format("LAC-%d-%06d", Year.now().getValue(),
                veiculoRepository.proximoNumeroLacre());
    }

    // "  abc1d23 " -> "ABC1D23"; vazio vira null (placa/chassi/motor são opcionais)
    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim().toUpperCase();
    }

    // Placa, chassi ou motor repetido em veículo ativo: avisa (409) até o usuário confirmar
    private void verificarDuplicidade(String placa, String chassi, String motor, Boolean confirmado) {
        if (Boolean.TRUE.equals(confirmado)) {
            return;
        }
        if (placa != null) {
            avisarSeExistir(veiculoRepository.findFirstByPlacaAndSituacaoVeiculoIn(placa, SITUACOES_ATIVAS),
                    "a placa " + placa);
        }
        if (chassi != null) {
            avisarSeExistir(veiculoRepository.findFirstByChassiAndSituacaoVeiculoIn(chassi, SITUACOES_ATIVAS),
                    "o chassi " + chassi);
        }
        if (motor != null) {
            avisarSeExistir(veiculoRepository.findFirstByMotorAndSituacaoVeiculoIn(motor, SITUACOES_ATIVAS),
                    "o motor " + motor);
        }
    }

    // Se a busca encontrou um veículo, lança o 409 com o lacre dele na mensagem
    private void avisarSeExistir(Optional<Veiculo> encontrado, String descricao) {
        encontrado.ifPresent(existente -> {
            throw new ConfirmacaoNecessariaException(
                    "Já existe veículo ativo com " + descricao + " (lacre " + existente.getLacre()
                            + "). Confirme para salvar mesmo assim.", "confirmarDuplicidade");
        });
    }
}
