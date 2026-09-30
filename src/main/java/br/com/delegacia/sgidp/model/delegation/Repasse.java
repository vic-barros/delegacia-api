package br.com.delegacia.sgidp.model.delegation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import br.com.delegacia.sgidp.enums.delegation.StatusRepasse;
import br.com.delegacia.sgidp.model.procedure.Procedimento;
import br.com.delegacia.sgidp.model.user.Usuario;

import java.time.LocalDateTime;

@Entity
@Table(name = "repasse", check
        = @CheckConstraint(name = "ck_repasse_solicitante_destinatario",
        constraint = "solicitante_id <> destinatario_id"))
@Getter
@Setter
@SequenceGenerator(name = "seq_repasse", sequenceName = "seq_repasse", allocationSize = 1, initialValue = 1)
public class Repasse {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_repasse")
    private Long id;

    @NotNull(message = "O status do repasse deve ser informado")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusRepasse statusRepasse = StatusRepasse.PENDENTE; //status repasse inicia como pendente

    @Column(name = "justificativa_recusa", length = 500)
    private String justificativaRecusa;

    @CreationTimestamp
    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    // Nula enquanto o repasse estiver PENDENTE; preenchida em aceitar()/recusar()
    @Column(name = "data_resposta")
    private LocalDateTime dataResposta;

    @NotNull(message = "O repasse deve ter um procedimento vinculado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_repasse_procedimento"))
    private Procedimento procedimento;

    @NotNull(message = "O repasse do procedimento deve ter um usuário solicitante")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_repasse_solicitante"))
    private Usuario solicitante;

    @NotNull(message = "O repaase do procedimento deve ter um usuário destinatário")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_repasse_destinatario"))
    private Usuario destinatario;

    public void aceitar(){
        this.statusRepasse = StatusRepasse.ACEITO;
        this.dataResposta = LocalDateTime.now();
    }

    public void recusar(String justificativa){
        this.statusRepasse = StatusRepasse.RECUSADO;
        this.justificativaRecusa = justificativa;
        this.dataResposta = LocalDateTime.now();
    }


}
