package br.com.delegacia.sgidp.service;


import br.com.delegacia.sgidp.dto.UsuarioCadastroRequestDto;
import br.com.delegacia.sgidp.dto.UsuarioResponseDto;
import br.com.delegacia.sgidp.enums.StatusUsuario;
import br.com.delegacia.sgidp.enums.TipoNotificacao;
import br.com.delegacia.sgidp.exception.RecursoDuplicadoException;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.Role;
import br.com.delegacia.sgidp.model.Usuario;
import br.com.delegacia.sgidp.repository.RoleRepository;
import br.com.delegacia.sgidp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificacaoService notificacaoService;

    // UC02 - Solicitar cadastro: o usuário nasce PENDENTE e os Admins são notificados
    @Transactional
    public UsuarioResponseDto solicitarCadastro(UsuarioCadastroRequestDto dto) {
        if (usuarioRepository.existsByLogin(dto.login())) {
            throw new RecursoDuplicadoException("Login já cadastrado");
        }
        if (usuarioRepository.existsByMatricula(dto.matricula())) {
            throw new RecursoDuplicadoException("Matrícula já cadastrada");
        }

        Role role = roleRepository.findById(dto.roleId())
                .orElseThrow(() -> new RegraNegocioException("Papel Inválido"));
        if (ROLE_ADMIN.equals(role.getAcesso())) {
            throw new RegraNegocioException("Não é permitido solicitar cadastro como Admin");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setMatricula(dto.matricula());
        usuario.setLogin(dto.login());
        usuario.setSenhaHash(passwordEncoder.encode(dto.senha()));
        usuario.setStatusUsuario(StatusUsuario.PENDENTE);
        usuario.setRole(role);

        usuario = usuarioRepository.save(usuario);


        List<Usuario> admins = usuarioRepository.findByRoleAcessoAndStatusUsuario(ROLE_ADMIN, StatusUsuario.APROVADO);

        for (Usuario admin : admins) {
            notificacaoService.notificar(admin, TipoNotificacao.CADASTRO_PENDENTE,
                    "Novo cadastro pendente de aprovação: " + usuario.getNome(),
                    usuario.getId());
        }

        return UsuarioResponseDto.de(usuario);
    }
}



