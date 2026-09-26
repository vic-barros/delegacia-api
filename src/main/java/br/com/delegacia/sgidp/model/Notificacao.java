package br.com.delegacia.sgidp.model;

import br.com.delegacia.sgidp.enums.TipoNotificacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao", indexes = {
        @Index(name =
        "idx_notificacao_usuario_lida", columnList =
                "usuario_destinatario_id, lida")}) //Index para facilitar uma consulta rápida das notificações
@Getter
@Setter
@SequenceGenerator(name= "seq_notificacao", sequenceName = "seq_notificacao", allocationSize = 1, initialValue = 1)
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_notificacao")
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O campo do tipo de notificação deve ser informado")
    @Column(name = "tipo", nullable = false)
    private TipoNotificacao tipoNotificacao;

    @NotBlank(message = "A mensagem da notificação não pode ser nula ou vazia")
    @Column(name = "mensagem", nullable = false)
    private String mensagem;

    @Column(name = "lida", nullable = false)
    private Boolean lida = false;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    // Id do registro que originou a notificação; a tabela depende do tipo (ver TipoNotificacao)
    @Column(name = "referencia_id")
    private Long referenciaId;

    @NotNull(message = "A oitiva deve possuir um usuário respnsável pela oitiva")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_destinatario_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_notificacao_usuario"))
    private Usuario usuarioDestinatario;
}
