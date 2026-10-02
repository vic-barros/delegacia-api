package br.com.delegacia.sgidp.model.user;

import br.com.delegacia.sgidp.enums.user.StatusUsuario;
import br.com.delegacia.sgidp.model.role.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario", uniqueConstraints = {
        @UniqueConstraint(name = "unique_matricula", columnNames = { "matricula" }),
        @UniqueConstraint(name = "unique_login", columnNames = { "login" }) })
@SequenceGenerator(name = "seq_usuario", sequenceName = "seq_usuario", allocationSize = 1, initialValue = 1)
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
    @Column(nullable = false)
    private String matricula;

    @NotBlank(message = "O campo login não pode ser nulo ou vazio")
    @Column(nullable = false)
    private String login;

    @NotBlank(message = "O campo senha_hash não pode ser nulo ou vazio")
    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O campo status não pode ser nulo")
    @Column(name = "status", nullable = false)
    private StatusUsuario statusUsuario;

    @NotNull(message = "O usuário deve possuir um papel - role")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_usuario_role"))
    private Role role;

    // Novo atributo para rejeição de cadastro de usuário, permitindo a notificação na tela
    // Como existe a unique de login e matricula, esse usuário não vai conseguir solicitar cadastro dps
    // Situação para resolver dps, mas com a notificação basta para o projeto
    @Column(name = "motivo_rejeicao", length = 500)
    private String motivoRejeicao;
}
