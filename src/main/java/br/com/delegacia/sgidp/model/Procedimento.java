package br.com.delegacia.sgidp.model;

import br.com.delegacia.sgidp.enums.StatusProcedimento;
import br.com.delegacia.sgidp.enums.TipoProcedimento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table (name = "procedimento")
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
    @Column(name = "numero", nullable = false)
    private String numeroProcedimento;

    @NotNull(message = "O campo do ano do procedimento não pode ser nulo")
    @Column(name = "ano", nullable = false)
    private int anoProcedimento;

    @NotNull(message = "O campo do crime do procedimento não pode ser nulo")
    @Column(name = "crime", nullable = false)
    private String crime;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_remessa_final", nullable = true)
    private LocalDateTime dataRemessaFinal;

    @Column(name = "protocolo_remessa_final", nullable = true)
    private String protocoloRemessaFinal;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O campo do status do procedimento não pode ser nulo")
    @Column(name = "status", nullable = false)
    private StatusProcedimento statusProcedimento;

    @NotNull(message = "O procedimento deve possuir um usuário detentor")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detentor_atual", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_procedimento_usuario"))
    private Usuario detentorAtual;


}
