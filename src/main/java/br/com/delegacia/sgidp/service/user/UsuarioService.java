package br.com.delegacia.sgidp.service.user;


import br.com.delegacia.sgidp.dto.user.*;
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
        notificacaoService.marcarComoLidas(TipoNotificacao.CADASTRO_PENDENTE, usuario.getId());  // O cadastro foi resolvido: o aviso some para todos os Admins
        // Sem save(): o usuário foi carregado nesta transação e o Hibernate grava as mudanças no commit
        return UsuarioResponseDto.de(usuario);
    }

    @Transactional
    public UsuarioResponseDto rejeitar(Long id, UsuarioRejeicaoRequestDto dto) {
        Usuario usuario = buscarUsuario(id);
        exigirPendente(usuario, "rejeitados");

        usuario.setStatusUsuario(StatusUsuario.REJEITADO);
        usuario.setMotivoRejeicao(dto.motivo());
        notificacaoService.marcarComoLidas(TipoNotificacao.CADASTRO_PENDENTE, usuario.getId());
        return UsuarioResponseDto.de(usuario);
    }

    // UC13 - Editar nome, matrícula e papel. O login não muda (é a identidade no token)
    @Transactional
    public UsuarioResponseDto editar(Long id, UsuarioEdicaoRequestDto dto, String loginLogado) {

        Usuario usuario = buscarUsuario(id);
        if (usuario.getStatusUsuario() != StatusUsuario.APROVADO
                && usuario.getStatusUsuario() != StatusUsuario.DESATIVADO) {
            throw new RegraNegocioException("Somente usuários aprovados ou desativados podem ser editados");
        }
        if (usuarioRepository.existsByMatriculaAndIdNot(dto.matricula(), id)) {
            throw new RecursoDuplicadoException("Matrícula já cadastrada para outro usuário");
        }


        // Na gestão o papel ADMIN é permitido (promoção); só não pode remover o próprio papel de Admin
        Role novoPapel = roleRepository.findById(dto.roleId())
                .orElseThrow(() -> new RegraNegocioException("Papel inválido"));
        if (ehOProprioUsuario(usuario, loginLogado) &&
                !ROLE_ADMIN.equals(novoPapel.getAcesso())) {
            throw new RegraNegocioException("O Admin não pode remover o próprio papel de Admin");
        }

        usuario.setNome(dto.nome());
        usuario.setMatricula((dto.matricula()));
        usuario.setRole(novoPapel);
        return UsuarioResponseDto.de(usuario);
    }

    @Transactional
    public UsuarioResponseDto desativar(Long id, String loginLogado) {
        Usuario usuario = buscarUsuario(id);
        if (ehOProprioUsuario(usuario, loginLogado)) {
            throw new RegraNegocioException("O Admin não pode desativar a si mesmo");
        }
        if ((usuario.getStatusUsuario() != StatusUsuario.APROVADO)) {
            throw new RegraNegocioException("Somente usuários aprovados podem ser desativados");
        }

        usuario.setStatusUsuario(StatusUsuario.DESATIVADO);
        return UsuarioResponseDto.de(usuario);
    }

    // UC13 - Reativar: só a partir de DESATIVADO
    @Transactional
    public UsuarioResponseDto reativar(Long id) {
        Usuario usuario = buscarUsuario(id);
        if (usuario.getStatusUsuario() != StatusUsuario.DESATIVADO) {
            throw new RegraNegocioException("Somente usuários desativados podem ser reativados");
        }

        usuario.setStatusUsuario(StatusUsuario.APROVADO);
        return UsuarioResponseDto.de(usuario);
    }

    // UC14 - Redefinir senha: o Admin define uma senha provisória, gravada com BCrypt
    @Transactional
    public void redefinirSenha(Long id, UsuarioRedefinicaoSenhaRequestDto dto) {
        Usuario usuario = buscarUsuario(id);
        usuario.setSenhaHash(passwordEncoder.encode(dto.novaSenha()));
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


    // Compara o usuário alvo da ação com quem está logado (login vindo do token)
    private boolean ehOProprioUsuario(Usuario usuario, String loginLogado) {
        return usuario.getLogin().equals(loginLogado);
    }


}





