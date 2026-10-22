package br.com.delegacia.sgidp.service.procedure;

import br.com.delegacia.sgidp.dto.custody.HistoricoCustodiaResponseDto;
import br.com.delegacia.sgidp.dto.delegation.RepassePendenteResponseDto;
import br.com.delegacia.sgidp.dto.procedure.HistoricoStatusResponseDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoRequestDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoResponseDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoDetalheResponseDto;
import br.com.delegacia.sgidp.enums.custody.OrigemHistoricoCustodia;
import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.exception.RecursoDuplicadoException;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.custody.HistoricoCustodia;
import br.com.delegacia.sgidp.model.procedure.HistoricoStatusProcedimento;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.custody.HistoricoCustodiaRepository;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import br.com.delegacia.sgidp.repository.procedure.HistoricoStatusProcedimentoRepository;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcedimentoService {

    private final ProcedimentoRepository procedimentoRepository;
    private final HistoricoCustodiaRepository historicoCustodiaRepository;
    private final HistoricoStatusProcedimentoRepository historicoStatusProcedimentoRepository;
    private final UsuarioRepository usuarioRepository;


    @Transactional
    public ProcedimentoResponseDto cadastrar(ProcedimentoRequestDto dto, String login) {


        if (procedimentoRepository.existsByTipoProcedimentoAndNumeroProcedimentoAndAnoProcedimento(
                dto.tipoProcedimento(),
                dto.numeroProcedimento(),
                dto.anoProcedimento())) {

            throw new RecursoDuplicadoException(
                    "Já existe um procedimento com esse tipo, número e ano"
            );
        }

        LocalDate hoje = LocalDate.now();


        if (dto.anoProcedimento() > hoje.getYear()) {
            throw new RegraNegocioException(
                    "O ano do procedimento não pode ser futuro"
            );
        }


        if (dto.dataAbertura().isAfter(hoje)) {
            throw new RegraNegocioException(
                    "A data de abertura não pode ser futura"
            );
        }

        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário autenticado não encontrado"
                ));


        Procedimento procedimento = new Procedimento();

        procedimento.setTipoProcedimento(dto.tipoProcedimento());
        procedimento.setNumeroProcedimento(dto.numeroProcedimento());
        procedimento.setAnoProcedimento(dto.anoProcedimento());
        procedimento.setCrime(dto.crime());
        procedimento.setDataAbertura(dto.dataAbertura());


        procedimento.setStatusProcedimento(StatusProcedimento.EM_ANDAMENTO);

        procedimento.setDetentorAtual(usuario);

        procedimento = procedimentoRepository.save(procedimento);

        HistoricoCustodia historico = new HistoricoCustodia(
                procedimento,
                usuario,
                OrigemHistoricoCustodia.CADASTRO_INICIAL,
                null
        );

        historicoCustodiaRepository.save(historico);

        historicoStatusProcedimentoRepository.save(new HistoricoStatusProcedimento(
                procedimento, null, StatusProcedimento.EM_ANDAMENTO, "Cadastro do procedimento", usuario
        ));

        return ProcedimentoResponseDto.de(procedimento);
    }

    // T08: quem teve a posse e quando (cadastro inicial e cada repasse aceito)
    @Transactional(readOnly = true)
    public List<HistoricoCustodiaResponseDto> listarHistoricoCustodia(Long id) {
        verificarExistencia(id);
        return historicoCustodiaRepository.findByProcedimentoIdOrderByDataInicioAsc(id)
                .stream()
                .map(HistoricoCustodiaResponseDto::de)
                .toList();
    }

    // T08: mudanças de status com motivo e responsável
    @Transactional(readOnly = true)
    public List<HistoricoStatusResponseDto> listarHistoricoStatus(Long id) {
        verificarExistencia(id);
        return historicoStatusProcedimentoRepository.findByProcedimentoIdOrderByDataTransicaoAsc(id)
                .stream()
                .map(HistoricoStatusResponseDto::de)
                .toList();
    }


    // Sem esta checagem, um id inexistente devolveria lista vazia (200) em vez de 404
    private void verificarExistencia(Long id) {
        if (!procedimentoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Procedimento não encontrado");
        }
    }

}