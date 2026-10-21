package br.com.delegacia.sgidp.model.hearing;

import br.com.delegacia.sgidp.model.party.Pessoa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import br.com.delegacia.sgidp.enums.party.TipoParte;

@Entity
@Table(name = "oitiva_parte")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//faz o lombok gerar p construtor vazio como protected em vez de public
@SequenceGenerator(name = "seq_oitiva_parte", sequenceName = "seq_oitiva_parte", allocationSize = 1, initialValue = 1)
public class OitivaParte {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_oitiva_parte")
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O tipo de parte da oitiva deve ser informado")
    @Column(name = "tipo_parte", nullable = false)
    private TipoParte tipoParte;

    @NotBlank(message = "O nome da parte a ser ouvida deve ser informado, não pode ser nulo ou vazio")
    @Column(name = "nome_parte", nullable = false)
    private String nomeParte; //questionando a necessidade deste atributo

    @NotNull(message = "A oitiva vinculada deve ser informada no cadastro da parte")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oitiva_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_oitiva_parte_oitiva"))
    private Oitiva oitiva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id",
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_oitiva_parte_pessoa"))
    private Pessoa pessoa;

    public OitivaParte(TipoParte tipoParte, String nomeParte) {
        this.tipoParte = tipoParte;
        this.nomeParte = nomeParte;
    }

    // Novo: parte já ligada a uma pessoa cadastrada (pessoa pode ser nula)
    public OitivaParte(TipoParte tipoParte, String nomeParte, Pessoa pessoa) {
        this.tipoParte = tipoParte;
        this.nomeParte = nomeParte;
        this.pessoa = pessoa;
    }
}
