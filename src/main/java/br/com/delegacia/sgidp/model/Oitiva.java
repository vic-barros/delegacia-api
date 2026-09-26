package br.com.delegacia.sgidp.model;

import br.com.delegacia.sgidp.enums.StatusOitiva;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O campo do status da oitiva não pode ser nulo")
    @Column(name = "status", nullable = false)
    private StatusOitiva statusOitiva = StatusOitiva.AGENDADA; //A oitiva inicia como agendada no cadastro

    @Column(name = "motivo_cancelamento", nullable = true)
    private String motivoCancelamento;

    @CreationTimestamp
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @NotNull(message = "A oitiva deve possuir um procedimento vinculado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_oitiva_procedimento"))
    private Procedimento procedimento;

    @NotNull(message = "A oitiva deve possuir uma parte intimada cadastrada")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_oitiva_parte_intimada"))
    private Usuario parteIntimada;

    @NotNull(message = "A oitiva deve registrar o usuário que a cadastrou")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cadastrado_por_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_oitiva_usuario_cadastrado"))
    private Usuario cadastradoPor;

    @Size(min = 1, message = "A oitiva deve possuir ao menos uma parte")
    @OneToMany(mappedBy = "oitiva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OitivaParte> partes = new ArrayList<>();

    public void adicionarParte(OitivaParte parte){
        parte.setOitiva(this);
        this.partes.add(parte);
    }

    public void removerOitiva(OitivaParte parte) {
        this.partes.remove(parte);
        parte.setOitiva(null);
    }


}
