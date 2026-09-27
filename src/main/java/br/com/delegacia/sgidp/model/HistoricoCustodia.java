package br.com.delegacia.sgidp.model;

import br.com.delegacia.sgidp.enums.OrigemHistoricoCustodia;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_custodia")
@SequenceGenerator(name = "seq_historico_custodia", sequenceName = "seq_historico_custodia", allocationSize = 1, initialValue = 1)
@Getter //Só getter: histórico não se altera
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoricoCustodia {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_historico_custodia")
    private Long id;

    @CreationTimestamp
    @NotNull(message = "A data do início do repasse deve ser informada")
    @Column(name = "data_inicio", nullable = false, updatable = false)
    private LocalDateTime dataInicio;

    @CreationTimestamp
    @Column(name = "data_fim", nullable = true, updatable = true)
    private LocalDateTime dataFim;

    @NotNull(message = "A origem do histórico de custódia deve ser informado")
    @Enumerated(EnumType.STRING)
    @Column(name = "origem", nullable = false)
    private OrigemHistoricoCustodia origem;

    @NotNull(message = "O histórico do repasse deve ter um procedimento vinculado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_historico_custodia_procedimento"))
    private Procedimento procedimento;

    @NotNull(message = "O usuário vinculado ao histórico de repasse deve ser informado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_historico_custodia_usuario"))
    private Usuario usuario;

    @OneToOne(optional = true)
    @JoinColumn(name = "repasse_origem_id", nullable = true, updatable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_historico_custodia_usuario"))
    private Repasse repasseOrigem;


}
