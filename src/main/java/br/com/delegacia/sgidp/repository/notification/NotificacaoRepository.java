package br.com.delegacia.sgidp.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.delegacia.sgidp.model.notification.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
}
