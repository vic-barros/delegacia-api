package br.com.delegacia.sgidp.repository;

import br.com.delegacia.sgidp.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
}
