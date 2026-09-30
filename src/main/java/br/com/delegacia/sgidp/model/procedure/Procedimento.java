package br.com.delegacia.sgidp.model.procedure;

import br.com.delegacia.sgidp.enums.procedure.StatusProcedimento;
import br.com.delegacia.sgidp.enums.procedure.TipoProcedimento;
import br.com.delegacia.sgidp.model.user.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table (name = "procedimento", uniqueConstraints
        = {
                @UniqueConstraint(name =
                "unique_tipo_numero_ano", columnNames = {
                        "tipo", "numero", "ano"})})
@SequenceGenerator(name= "seq_procedimento", sequenceName = "seq_procedimento", allocationSize = 1, initialValue = 1)
@Getter
@Setter
public class Procedimento {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_procedimento")
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O campo do tipo de procedimento não pode ser nulo")
    @Column(name = "tipo", nullable = false)
    private TipoProcedimento tipoProcedimento;

    @NotNull(message = "O campo do número procedimento não pode ser nulo")
    @Positive(message = "O número do procedimento deve ser positivo")
    @Column(name = "numero", nullable = false)
    private Long numeroProcedimento;

    @NotNull(message = "O campo do ano do procedimento não pode ser nulo")
    @Positive(message = "O ano do procedimento deve ser positivo")
    @Column(name = "ano", nullable = false)
    private Integer anoProcedimento;

    @NotBlank(message = "O campo do crime do procedimento não pode ser nulo ou vazio")
    @Column(name = "crime", nullable = false)
    private String crime;

    @NotNull(message = "A data de abertura do procedimento não pode ser nula")
    @Column(name = "data_abertura", nullable = false)
    private LocalDate dataAbertura;

    @Column(name = "data_remessa_final", nullable = true)
    private LocalDate dataRemessaFinal;

    @Column(name = "protocolo_remessa_final", nullable = true)
    private String protocoloRemessaFinal;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O campo do status do procedimento não pode ser nulo")
    @Column(name = "status", nullable = false)
    private StatusProcedimento statusProcedimento;

    @NotNull(message = "O procedimento deve possuir um usuário detentor")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detentor_atual_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_procedimento_usuario"))
    private Usuario detentorAtual;


}
