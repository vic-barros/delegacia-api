package br.com.delegacia.sgidp.repository;

import br.com.delegacia.sgidp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository  extends JpaRepository<Usuario, Long> {

    //Busca usuário pelo login correto
    Optional<Usuario> findByLogin(String login);
}
