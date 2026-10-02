package br.com.delegacia.sgidp.service.auth;

import br.com.delegacia.sgidp.dto.auth.LoginRequestDto;
import br.com.delegacia.sgidp.dto.auth.TokenResponseDto;
import br.com.delegacia.sgidp.exception.CadastroNaoAprovadoException;
import br.com.delegacia.sgidp.exception.CredenciaisInvalidasException;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    // readOnly: só leitura; a transação mantém a sessão aberta para carregar usuario.getRole() (LAZY)
    @Transactional(readOnly = true)
    public TokenResponseDto login(LoginRequestDto dados) {
        Usuario usuario = usuarioRepository.findByLogin(dados.login())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(dados.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        switch (usuario.getStatusUsuario()) {
            case PENDENTE -> throw new CadastroNaoAprovadoException("Cadastro aguardando aprovação do administrador");
            case REJEITADO -> throw new CadastroNaoAprovadoException(
                    "Cadastro rejeitado pelo administrador. Motivo " +
                            usuario.getMotivoRejeicao());
            case DESATIVADO -> throw new CadastroNaoAprovadoException("Usuário desativado");
            case APROVADO -> {
            }
        }

        return new TokenResponseDto(tokenService.gerar(usuario));
    }
}
