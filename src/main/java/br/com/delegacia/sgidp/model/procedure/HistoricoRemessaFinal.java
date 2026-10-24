package br.com.delegacia.sgidp.model.procedure;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import br.com.delegacia.sgidp.model.user.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Immutable // Hibernate ignora qualquer UPDATE após o INSERT
@Table(name = "historico_remessa_final")
@SequenceGenerator(
        name = "seq_historico_remessa_final",
        sequenceName = "seq_historico_remessa_final",
        allocationSize = 1,
        initialValue = 1
)
@Getter // Só getter: histórico não se altera
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoricoRemessaFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_historico_remessa_final")
    private Long id;

    /**
     * Dados anteriores à sobrescrita. Nulos no primeiro registro da remessa.
     */
    @Column(name = "data_remessa_anterior")
    private LocalDate dataRemessaAnterior;

    @Column(name = "protocolo_anterior", length = 255)
    private String protocoloAnterior;

    /**
     * Dados recém-registrados.
     */
    @NotNull(message = "A data da remessa final deve ser informada")
    @Column(name = "data_remessa_nova", nullable = false)
    private LocalDate dataRemessaNova;

    @NotBlank(message = "O protocolo da remessa final deve ser informado")
    @Column(name = "protocolo_novo", nullable = false, length = 255)
    private String protocoloNovo;

    @CreationTimestamp
    @Column(name = "data_registro", nullable = false, updatable = false)
    private LocalDateTime dataRegistro;

    @NotNull(message = "O histórico deve estar vinculado a um procedimento")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "procedimento_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_historico_remessa_final_procedimento")
    )
    private Procedimento procedimento;

    @NotNull(message = "O histórico deve registrar o responsável pelo registro")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "responsavel_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_historico_remessa_final_responsavel")
    )
    private Usuario responsavel;

    public HistoricoRemessaFinal(
            Procedimento procedimento,
            LocalDate dataRemessaAnterior,
            String protocoloAnterior,
            LocalDate dataRemessaNova,
            String protocoloNovo,
            Usuario responsavel
    ) {
        this.procedimento = procedimento;
        this.dataRemessaAnterior = dataRemessaAnterior;
        this.protocoloAnterior = protocoloAnterior;
        this.dataRemessaNova = dataRemessaNova;
        this.protocoloNovo = protocoloNovo;
        this.responsavel = responsavel;
    }
}