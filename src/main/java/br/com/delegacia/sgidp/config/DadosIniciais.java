package br.com.delegacia.sgidp.config;

import br.com.delegacia.sgidp.enums.user.StatusUsuario;
import br.com.delegacia.sgidp.model.role.Role;
import br.com.delegacia.sgidp.model.user.Usuario;
import br.com.delegacia.sgidp.repository.role.RoleRepository;
import br.com.delegacia.sgidp.repository.user.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DadosIniciais implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_LOGIN:admin}")
    private String adminLogin;

    @Value("${ADMIN_PASSWORD}")
    private String adminSenha;

    @Override
    public void run(String... args) {
        criarRoleSeNaoExistir("ROLE_ADMIN", "Administrador");
        criarRoleSeNaoExistir("ROLE_DELEGADO", "Delegado");
        criarRoleSeNaoExistir("ROLE_POLICIAL", "Policial");
        criarRoleSeNaoExistir("ROLE_ESTAGIARIO", "Estagiário");

        if (usuarioRepository.findByLogin(adminLogin).isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setMatricula("0000");
            admin.setLogin(adminLogin);
            admin.setSenhaHash(passwordEncoder.encode(adminSenha));
            admin.setStatusUsuario(StatusUsuario.APROVADO);
            admin.setRole(roleRepository.findByAcesso("ROLE_ADMIN").orElseThrow());
            usuarioRepository.save(admin);
        }
    }

    private void criarRoleSeNaoExistir(String acesso, String descricao) {
        if (roleRepository.findByAcesso(acesso).isEmpty()) {
            Role role = new Role();
            role.setAcesso(acesso);
            role.setDescricao(descricao);
            roleRepository.save(role);
        }
    }
}