package br.com.delegacia.sgidp.model;

import br.com.delegacia.sgidp.enums.StatusRepasse;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "repasse")
@Getter
@Setter
@SequenceGenerator(name = "seq_repasse", sequenceName = "seq_repasse", allocationSize = 1, initialValue = 1)
public class Repasse {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_repasse")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusRepasse statusRepasse;

    @Column(name = "status", nullable = true)
    private String justificativaRecusa;

    @CreationTimestamp
    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @CreationTimestamp
    @Column(name = "data_resposta", nullable = false, updatable = false)
    private LocalDateTime dataRespota;

    @NotNull(message = "O repasse deve ter um procedimento vinculado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_repasse_procedimento"))
    private Procedimento procedimento;

    @NotNull(message = "O repasse do procedimento deve ter um usuário solicitante informado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_repasse_solicitante"))
    private Usuario solicitante;

    @NotNull(message = "O repaase do procedimento deve ter um usuário destinatário informado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cadastrado_por_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_oitiva_usuario_cadastrado"))
    private Usuario destinatario;


}
