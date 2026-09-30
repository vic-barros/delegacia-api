package br.com.delegacia.sgidp.model.procedure;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.model.user.Usuario;

import java.time.LocalDateTime;

@Entity
@Immutable //O hibernate ignora qualquer alteração dos do insert, o updtae nunca vai para o banco
@Table(name = "historico_status_procedimento")
@SequenceGenerator(name = "seq_historico_status_procedimento", sequenceName = "seq_historico_status_procedimento", allocationSize = 1, initialValue = 1)
@Getter //Só getter: histórico não se altera
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoricoStatusProcedimento {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_historico_status_procedimento")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior")
    private StatusProcedimento statusAnterior;

    @NotNull(message = "O novo status deve ser informado")
    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false)
    private StatusProcedimento statusNovo;

    @NotBlank(message = "O motivo da mudança de status deve ser informado")
    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @CreationTimestamp
    @Column(name = "data_transicao", nullable = false, updatable = false)
    private LocalDateTime dataTransicao;

    @NotNull(message = "O histórico deve estar vinculado a um procedimento")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_historico_status_procedimento"))
    private Procedimento procedimento;

    @NotNull(message = "O histórico deve registrar o responsável pela mudança")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_historico_status_responsavel"))
    private Usuario responsavel;

    public HistoricoStatusProcedimento(Procedimento procedimento, StatusProcedimento statusAnterior,
                                       StatusProcedimento statusNovo, String motivo, Usuario responsavel) {
        this.procedimento = procedimento;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.motivo = motivo;
        this.responsavel = responsavel;
    }


}
