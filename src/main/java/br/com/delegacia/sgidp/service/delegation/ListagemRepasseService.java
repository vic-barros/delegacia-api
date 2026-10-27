package br.com.delegacia.sgidp.service.delegation;

import br.com.delegacia.sgidp.dto.delegation.RepasseListagemItemResponseDto;
import br.com.delegacia.sgidp.dto.delegation.RepasseListagemResponseDto;
import br.com.delegacia.sgidp.enums.delegation.AbaRepasse;
import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import br.com.delegacia.sgidp.repository.delegation.RepasseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListagemRepasseService {

    private static final int TAMANHO_MAXIMO = 100;

    private final RepasseRepository repasseRepository;

    // T09: só os repasses em que o usuário logado é destinatário ou solicitante (aba padrão RECEBIDOS)
    @Transactional(readOnly = true)
    public RepasseListagemResponseDto listar(AbaRepasse aba, String texto, int pagina, int tamanho, String login) {
        if (pagina < 0 || tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new RegraNegocioException("A página deve ser 0 ou maior e o tamanho entre 1 e " + TAMANHO_MAXIMO);
        }

        if (aba == null) {
            aba = AbaRepasse.RECEBIDOS;
        }

        // A aba define o papel do usuário no repasse e os status; o histórico junta recebidos e enviados já respondidos
        boolean comoDestinatario = aba != AbaRepasse.ENVIADOS;
        boolean comoSolicitante = aba != AbaRepasse.RECEBIDOS;
        List<StatusRepasse> status = List.of(StatusRepasse.PENDENTE);
        Sort ordem = Sort.by("dataSolicitacao").descending();
        if (aba == AbaRepasse.HISTORICO) {
            status = List.of(StatusRepasse.ACEITO, StatusRepasse.RECUSADO);
            ordem = Sort.by("dataResposta").descending();
        }

        Long numero = null;
        String termo = null;
        if (texto != null && !texto.isBlank()) {
            String limpo = texto.trim();
            if (limpo.matches("\\d{1,9}")) {
                numero = Long.valueOf(limpo);
            } else {
                termo = limpo;
            }
        }

        // Mais recentes primeiro; o id desempata
        Pageable paginacao = PageRequest.of(pagina, tamanho, ordem.and(Sort.by("id").descending()));

        Page<Repasse> resultado = repasseRepository.listar(login, comoDestinatario, comoSolicitante, status,
                numero, termo, paginacao);

        List<RepasseListagemItemResponseDto> itens = new ArrayList<>();
        for (Repasse repasse : resultado.getContent()) {
            itens.add(RepasseListagemItemResponseDto.de(repasse));
        }

        long recebidos = repasseRepository.countByDestinatarioLoginAndStatusRepasse(login, StatusRepasse.PENDENTE);
        long enviados = repasseRepository.countBySolicitanteLoginAndStatusRepasse(login, StatusRepasse.PENDENTE);

        return new RepasseListagemResponseDto(itens, resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages(), recebidos, enviados);
    }
}