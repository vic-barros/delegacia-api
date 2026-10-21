package br.com.delegacia.sgidp.model.party;

import br.com.delegacia.sgidp.model.user.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Pessoa envolvida em procedimentos (vítima, testemunha, investigado). Base do módulo de vínculos.
@Entity
@Table(name = "pessoa", uniqueConstraints = {
        @UniqueConstraint(name = "unique_cpf", columnNames = {"cpf"})
})
@SequenceGenerator(name = "seq_pessoa", sequenceName = "seq_pessoa", allocationSize = 1, initialValue = 1)
@Getter
@Setter
@NoArgsConstructor
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pessoa")
    private Long id;

    @NotBlank(message = "O nome a pessoa deve ser informado")
    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 11)
    private String cpf;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "nome_mae", length = 150)
    private String nomeMae;

    @Column(length = 100)
    private String alcunha;

    @Column(length = 20)
    private String telefone;

    // Texto livre (rua, número, bairro, cidade): não há busca por endereço no MVP
    @Column(length = 300)
    private String endereco;

    @CreationTimestamp
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    // Quem cadastrou (vem do token): rastreabilidade de quem incluiu dados pessoais
    @NotNull(message = "O usuário que cadastrou a pessoa deve ser informado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cadastrado_por_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_pessoa_cadastrado_por"))
    private Usuario cadastradoPor;
}
