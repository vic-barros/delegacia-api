package br.com.delegacia.sgidp.repository.role;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.delegacia.sgidp.model.role.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    //Busca pelo campo acesso com os 4 acessos
    Optional<Role> findByAcesso(String acesso);

    // Lista os papéis disponíveis no cadastro público de usuário, exceto ROLE_ADMIN
    List<Role> findByAcessoNotOrderByIdAsc(String acesso);
}
