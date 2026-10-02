package br.com.delegacia.sgidp.service.user;


import br.com.delegacia.sgidp.dto.user.UsuarioAprovacaoRequestDto;
import br.com.delegacia.sgidp.dto.user.UsuarioCadastroRequestDto;
import br.com.delegacia.sgidp.dto.user.UsuarioResponseDto;
import br.com.delegacia.sgidp.dto.user.UsuarioRejeicaoRequestDto;
import br.com.delegacia.sgidp.enums.notification.TipoNotificacao;
import br.com.delegacia.sgidp.enums.user.StatusUsuario;
import br.com.delegacia.sgidp.exception.RecursoDuplicadoException;
import br.com.delegacia.sgidp.exception.RecursoNaoEncontradoException;
import br.com.delegacia.sgidp.exception.RegraNegocioException;
import br.com.delegacia.sgidp.model.role.Role;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.role.RoleRepository;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import br.com.delegacia.sgidp.service.notification.NotificacaoService;
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

        Role role = buscarPapelPermitido(dto.roleId());

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

    // UC03 - Listar usuários (status opcional: sem filtro devolve todos), em ordem alfabética
    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> listar(StatusUsuario status) {
        List<Usuario> usuarios;

        if (status == null) {
            usuarios = usuarioRepository.findAllByOrderByNomeAsc();
        } else {
            usuarios = usuarioRepository.findByStatusUsuarioOrderByNomeAsc(status);
        }

        return usuarios.stream()
                .map(UsuarioResponseDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDto buscarPorId(Long id) {
        return UsuarioResponseDto.de(buscarUsuario(id));
    }

    // UC03 - Aprovar: só a partir de PENDENTE; o Admin pode confirmar ou trocar o papel

    @Transactional
    public UsuarioResponseDto aprovar(Long id, UsuarioAprovacaoRequestDto dto) {
        Usuario usuario = buscarUsuario(id);
        exigirPendente(usuario, "aprovados");

        if (dto != null && dto.roleId() != null) {
            usuario.setRole(buscarPapelPermitido(dto.roleId()));
        }
        usuario.setStatusUsuario(StatusUsuario.APROVADO);
        // Sem save(): o usuário foi carregado nesta transação e o Hibernate grava as mudanças no commit
        return UsuarioResponseDto.de(usuario);
    }

    public UsuarioResponseDto rejeitar(Long id, UsuarioRejeicaoRequestDto dto) {
        Usuario usuario = buscarUsuario(id);
        exigirPendente(usuario, "rejeitados");

        usuario.setStatusUsuario(StatusUsuario.REJEITADO);
        usuario.setMotivoRejeicao(dto.motivo());
        return UsuarioResponseDto.de(usuario);
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado"));
    }


    private void exigirPendente(Usuario usuario, String acao) {
        if (usuario.getStatusUsuario() != StatusUsuario.PENDENTE) {
            throw new RegraNegocioException("Somente cadastro pendentes podem ser " + acao);
        }
    }

    // Usado no cadastro e na aprovação: o papel precisa existir e não pode ser ROLE_ADMIN
    private Role buscarPapelPermitido(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RegraNegocioException("Papel inválido"));
        if (ROLE_ADMIN.equals(role.getAcesso())) {
            throw new RegraNegocioException("O papel de Admin não pode ser atribuído por este fluxo");
        }
        return role;
    }


}





