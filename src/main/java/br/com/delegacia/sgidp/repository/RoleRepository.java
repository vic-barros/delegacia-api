package br.com.delegacia.sgidp.repository;

import br.com.delegacia.sgidp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    //Busca pelo campo acesso com os 4 acessos
    Optional<Role> findByAcesso(String acesso);
}
