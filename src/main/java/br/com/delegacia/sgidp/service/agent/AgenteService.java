package br.com.delegacia.sgidp.service.agent;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import br.com.delegacia.sgidp.dto.agent.ResumoProcedimentoResponseDto;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.procedure.ProcedimentoRepository;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import br.com.delegacia.sgidp.service.agent.ProceduryResume.ResumoProcedimento;


@Service
public class AgenteService {
    private final ChatClient chatClient;
    private final ResumoProcedimento resumoProcedimento;
    private final UsuarioRepository usuarioRepository;
    private final ProcedimentoRepository procedimentoRepository;

    public AgenteService(
        ChatClient.Builder builder,
        ResumoProcedimento resumoProcedimento,
        UsuarioRepository usuarioRepository,
        ProcedimentoRepository procedimentoRepository
    ) {
        this.chatClient = builder.build();
        this.resumoProcedimento = resumoProcedimento;
        this.usuarioRepository = usuarioRepository;
        this.procedimentoRepository = procedimentoRepository;
    }


    public ResumoProcedimentoResponseDto gerarResumoProcedimento(Long procedimentoId, String login) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário autenticado não encontrado"
                ));
        
        Procedimento procedimento = procedimentoRepository.findById(procedimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Procedimento não encontrado"
                ));
        
        if (!usuario.getId().equals(procedimento.getDetentorAtual().getId())) {
            throw new RegraNegocioException("Somente o detentor do procedimento pode solicitar seu resumo.");
        }

        
        String contextoProcedimento = resumoProcedimento.criarContextoProcedimento(procedimentoId);

        String resposta = chatClient.prompt()
                .system("""
                                Você auxilia na elaboração de resumos de procedimentos policiais.

                                Regras:
                                - Utilize somente os dados fornecidos.
                                - Não invete fatos, datas ou pessoas.
                                - Não trate alegações como fatos comprovados.
                                - Não conclua culpa ou responsabilidade.
                                - Escreva em português, com clareza e objetividade.
                                - Escreva o resumo em linguagem natural, conectando os eventos com coesão e coerência.
                                - Escreva como se estivesse explicando o procedimento para outra pessoa de forma natural numa conversa.
                                - Não utilize técnicas de formatação na mensagem, como tags HTML ou tags Markdown. O resumo deve ser escrito em texto corrido. 
                                
                                Regras adicionais:
                                - Diferencie eventos registrados, alegações, suspeitas e conclusões periciais.
                                - Não infira alterações de titularidade a partir de um repasse sem confirmação explícita.
                                - Preserve o significado dos status e das datas presentes no contexto.
                                - Diferencie oitivas realizadas, agendadas, canceladas e desmarcadas.
                                - Identifique diligências pendentes quando houver dados que as sustentem.
                                - Não afirme que uma diligência foi concluída apenas porque ela foi mencionada.
                                - Se houver informações contraditórias ou insuficientes, explicite a limitação.
                                - Não invente relações causais entre eventos que apenas ocorreram em sequência.
                                - Não apresente o resumo como substituto dos registros oficiais.
                                """)
                .user(user -> user.text("""
                                Elabore um resumo do procedimento com base exclusivamente no contexto abaixo.
                                <contexto>
                                {contexto}
                                </contexto>
                                """)
                        .param("contexto", contextoProcedimento))
                .call()
                .content();

        ResumoProcedimentoResponseDto resumo = new ResumoProcedimentoResponseDto(resposta);
        return resumo;
    }

}

