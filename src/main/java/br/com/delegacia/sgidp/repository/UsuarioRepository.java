package br.com.delegacia.sgidp.repository;

import br.com.delegacia.sgidp.enums.StatusUsuario;
import br.com.delegacia.sgidp.model.Role;
import br.com.delegacia.sgidp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca usuário pelo login correto, o retorno da consulta com Optional espera encontrar no máximo um registro
    Optional<Usuario> findByLogin(String login);

    // Método para checar se o login já existe
    boolean existsByLogin(String login);

    // Método para chegar se a matrícula já existe
    boolean existsByMatricula(String matricula);

    // Método para checar se a matrícula pertence a outro usuário (ignornado o próprio Id na consulta)
    boolean existsByMatriculaAndIdNot(String matricula, Long id);

    // Busca usuário pelo status, o retorno é List porque um status pode estar associado a mais de um usuário
    // Order By para vir a consulta em ordem alfabética
    List<Usuario> findByStatusUsuarioOrderByNomeAsc(StatusUsuario statusUsuario);

    // Busca todos os usuários filtrando pelo tipo de acesso associado a seu papel e pelo status atual da conta.
    List<Usuario> findByRoleAcessoAndStatusUsuario(String acesso, StatusUsuario statusUsuario);

    // Listagem pelo nome sem filtro
    List<Usuario> findAllByOrderByNomeAsc();
}
