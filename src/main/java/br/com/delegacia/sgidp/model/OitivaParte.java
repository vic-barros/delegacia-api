package br.com.delegacia.sgidp.model;

import br.com.delegacia.sgidp.enums.TipoParte;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "oitiva_parte")
@Getter
@Setter
@SequenceGenerator(name= "seq_oitiva_parte", sequenceName = "seq_oitiva_parte", allocationSize = 1, initialValue = 1)
public class OitivaParte {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_oitiva_parte")
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O tipo de parte da oitiva deve ser informado")
    @Column(name = "tipo_parte", nullable = false)
    private TipoParte tipoParte;

    @Column(name = "identificacao_parte")
    private String identificacaoParte; //questionando a necessidade deste atributo

    @NotNull(message = "A oitiva vinculada deve ser informada no cadastro da parte")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oitiva_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_oitiva_parte_oitiva"))
    private Oitiva oitiva;
}
