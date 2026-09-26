package br.com.delegacia.sgidp.model;

import br.com.delegacia.sgidp.enums.StatusUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario", uniqueConstraints = {
        @UniqueConstraint(name = "unique_matricula", columnNames = { "matricula" }),
        @UniqueConstraint(name = "unique_login", columnNames = { "login" }) })
@SequenceGenerator(name = "seq_usuario", sequenceName = "usuario_seq", allocationSize = 1, initialValue = 1)
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_usuario")
    private Long id;

    @NotBlank(message = "O campo nome não pode ser nulo ou vazio")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "O campo matricula não pode ser nulo ou vazio")
    @Column(nullable = false, unique = true)
    private String matricula;

    @NotBlank(message = "O campo login não pode ser nulo ou vazio")
    @Column(nullable = false, unique = true)
    private String login;

    @NotBlank(message = "O campo senha_hash não pode ser nulo ou vazio")
    @Column(nullable = false)
    private String senha_hash;

    @Enumerated(EnumType.STRING)
    @NotBlank(message = "O campo status não pode ser nulo ou vazio")
    @Column(nullable = false)
    private StatusUsuario statusUsuario;

    @NotBlank(message = "O usuário não pode ter o campo roleId nulo ou vazio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "role_id_fk"))
    private Role roleId;
}
