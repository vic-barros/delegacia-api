package br.com.delegacia.sgidp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;

@Entity
@Table(name = "role", uniqueConstraints = {
        @UniqueConstraint(name = "unique_acesso", columnNames = "acesso"),
})
@SequenceGenerator(name = "seq_role", sequenceName = "seq_role", allocationSize = 1, initialValue = 1)
@Getter
@Setter
public class Role implements GrantedAuthority {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_role")
    private Long id;

    @NotBlank(message = "O campo acesso não pode ser nulo ou vazio")
    @Column(nullable = false, unique = true)
    private String acesso; //seria o acesso

    @Column
    private String descricao;

    @Override
    public String getAuthority() {
        return this.acesso;
    }


}
