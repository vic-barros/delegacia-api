package br.com.delegacia.sgidp.repository.notification;

import br.com.delegacia.sgidp.enums.notification.TipoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.delegacia.sgidp.model.notification.Notificacao;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    // Todas as notificações do usuário (navega notificacao -> usuarioDestinatario -> login), mais recentes primeiro
    List<Notificacao> findByUsuarioDestinatarioLoginOrderByDataCriacaoDesc(String login);

    // Filtradas por lida/não lida
    List<Notificacao> findByUsuarioDestinatarioLoginAndLidaOrderByDataCriacaoDesc(String login, Boolean lida);

    // Número do sininho: conta no banco, sem trazer a lista
    long countByUsuarioDestinatarioLoginAndLidaFalse(String login);

    // Busca já filtrada pelo dono: notificação de outra pessoa "não existe" para quem pergunta
    Optional<Notificacao> findByIdAndUsuarioDestinatarioLogin(Long id, String login);

    // Avisos de um mesmo registro de origem (ex.: CADASTRO_PENDENTE do usuário X para todos os Admins)
    List<Notificacao> findByTipoNotificacaoAndReferenciaIdAndLidaFalse(TipoNotificacao tipo, Long referenciaId);
}
