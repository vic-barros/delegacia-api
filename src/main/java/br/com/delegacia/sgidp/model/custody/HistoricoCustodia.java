package br.com.delegacia.sgidp.model.custody;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import br.com.delegacia.sgidp.enums.custody.OrigemHistoricoCustodia;
import br.com.delegacia.sgidp.model.delegation.Repasse;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_custodia")
@SequenceGenerator(name = "seq_historico_custodia", sequenceName = "seq_historico_custodia", allocationSize = 1, initialValue = 1)
@Getter // Só getter; a única alteração permitida é encerrar()
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoricoCustodia {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_historico_custodia")
    private Long id;

    @CreationTimestamp
    @Column(name = "data_inicio", nullable = false, updatable = false)
    private LocalDateTime dataInicio;

    // Nula = custódia atual. Única coluna atualizável: preenchida em encerrar()
    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @NotNull(message = "A origem da custódia deve ser informada")
    @Enumerated(EnumType.STRING)
    @Column(name = "origem", nullable = false, updatable = false)
    private OrigemHistoricoCustodia origem;

    @NotNull(message = "A custódia deve estar vinculada a um procedimento")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_historico_custodia_procedimento"))
    private Procedimento procedimento;

    @NotNull(message = "A custódia deve estar vinculada a um usuário")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_historico_custodia_usuario"))
    private Usuario usuario;

    // Nulo quando a origem é CADASTRO_INICIAL
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repasse_origem_id", updatable = false,
            foreignKey = @ForeignKey(name = "fk_historico_custodia_repasse"))
    private Repasse repasseOrigem;

    public HistoricoCustodia(Procedimento procedimento, Usuario usuario,
                             OrigemHistoricoCustodia origem, Repasse repasseOrigem) {
        this.procedimento = procedimento;
        this.usuario = usuario;
        this.origem = origem;
        this.repasseOrigem = repasseOrigem;
    }

    public void encerrar() {
        if (this.dataFim != null) {
            throw new IllegalStateException("Esta custódia já foi encerrada");
        }
        this.dataFim = LocalDateTime.now();
    }
}
