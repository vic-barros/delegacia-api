package br.com.delegacia.sgidp.model;

import br.com.delegacia.sgidp.enums.StatusOitiva;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "oitiva")
@Getter
@Setter
@SequenceGenerator(name= "seq_oitiva", sequenceName = "seq_oitiva", allocationSize = 1, initialValue = 1)
public class Oitiva {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_oitiva")
    private Long id;

    @NotNull(message = "A data e hora da oitiva deve ser informada")
    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O campo do status da oitiva não pode ser nulo")
    @Column(name = "status", nullable = false)
    private StatusOitiva statusOitiva;

    @Column(name = "motivo_cancelamento", nullable = true)
    private String motivoCancelamento;

    @NotNull(message = "A data do cadastro da oitiva deve ser informada")
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @NotNull(message = "A oitiva deve possuir um procedimento vinculado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_oitiva_procedimento_"))
    private Procedimento procedimento;

    @NotNull(message = "A oitiva deve possuir um policial/delegado como responsável cadastrado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_oitiva_usuario_responsavel"))
    private Usuario usuarioResponsavel;

    @NotNull(message = "A oitiva deve possuir uma parte cadastrada para ser ouvida")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cadastrado_por_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_oitiva_usuario_cadastrado"))
    private Usuario usuarioCadastrado;


}
