package br.com.delegacia.sgidp.service.procedure;

import br.com.delegacia.sgidp.dto.procedure.ProcedimentoRequestDto;
import br.com.delegacia.sgidp.dto.procedure.ProcedimentoResponseDto;
import br.com.delegacia.sgidp.enums.custody.OrigemHistoricoCustodia;
import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.exception.RecursoDuplicadoException;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.custody.HistoricoCustodia;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.custody.HistoricoCustodiaRepository;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProcedimentoService {

    private final ProcedimentoRepository procedimentoRepository;
    private final HistoricoCustodiaRepository historicoCustodiaRepository;
    private final UsuarioRepository usuarioRepository;

    
    @Transactional
    public ProcedimentoResponseDto cadastrar(ProcedimentoRequestDto dto) {

       
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

        
        String login = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        
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

        return ProcedimentoResponseDto.de(procedimento);
    }
}