package br.com.delegacia.sgidp.model;

import br.com.delegacia.sgidp.enums.SituacaoVeiculo;
import br.com.delegacia.sgidp.enums.StatusPericia;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "veiculo", uniqueConstraints = {
@UniqueConstraint(name = "unique_lacre", columnNames = { "lacre" })})
@SequenceGenerator(name = "seq_veiculo", sequenceName = "seq_veiculo", allocationSize = 1, initialValue = 1)
@Getter
@Setter
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_veiculo")
    private Long id;

    @NotBlank(message = "O tipo do veículo deve ser informado")
    @Column(name = "tipo_veiculo", nullable = false)
    private String tipoVeiculo;

    @NotBlank(message = "O lacre do veículo deve ser gerado antes de salvar")
    @Setter(AccessLevel.NONE) //Ninguém altera por setter
    @Column(name = "lacre", nullable = false, updatable = false, length = 15)
    private String lacre;

    @NotBlank(message = "O campo marca deve ser informado")
    @Column(name = "marca", nullable = false)
    private String marca;

    @NotBlank(message = "O campo modelo deve ser informado")
    @Column(name = "modelo", nullable = false)
    private String modelo;

    @NotBlank(message = "O campo cor deve ser informado")
    @Column(name = "cor", nullable = false)
    private String cor;

    @Column(name = "placa", nullable = true)
    private String placa;

    @Column(name = "chassi", nullable = true)
    private String chassi;

    @Column(name = "motor", nullable = true)
    private String motor;

    @Column(name = "caracteristicas_visuais", columnDefinition = "TEXT")
    private String caracteristicasVisuais;

    @NotNull(message = "O status da perícia deve ser informado")
    @Enumerated(EnumType.STRING)
    @Column(name = "pericia", nullable = false)
    private StatusPericia statusPericia;

    @NotNull(message = "A situação do veículo deve ser informada")
    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false)
    private SituacaoVeiculo situacaoVeiculo = SituacaoVeiculo.NA_DEPOL;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @NotNull(message = "O veículo deve ter um procedimento vinculado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_veiculo_procedimento"))
    private Procedimento procedimento;









    public void definirLacre(String lacre){
        if(this.lacre != null){
            throw new IllegalStateException("O lacre do veículo ja foi definido");
        }
        this.lacre = lacre;
    }
}
