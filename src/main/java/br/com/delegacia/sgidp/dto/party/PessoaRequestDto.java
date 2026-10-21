package br.com.delegacia.sgidp.dto.party;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

// Usado no cadastro e na edição (no PUT, enviar todos os campos: o que vier nulo fica nulo).
// CPF com ou sem pontuação; @CPF confere os dígitos verificadores
public record PessoaRequestDto(

        @NotBlank(message = "O nome deve ser informado")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @Past(message = "A data de nascimento deve estar no passado")
        LocalDate dataNascimento,

        @CPF(message = "CPF inválido")
        String cpf,

        @Size(max = 100, message = "A alcunha deve ter no máximo 100 caracteres")
        String alcunha,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        @Pattern(regexp = "^[0-9()+\\-\\s]*$", message = "O telefone deve conter apenas números, espaços, parênteses, + e -")
        String telefone,

        @Size(max = 300, message = "O endereço deve ter no máximo 300 caracteres")
        String endereco

) {

    // Construtor compacto: roda antes da validação. Campo opcional vazio ("") vira nulo,
    // senão o @CPF recusaria o CPF em branco que o formulário envia quando ninguém preenche
    public PessoaRequestDto {
        cpf = vazioParaNulo(cpf);
        alcunha = vazioParaNulo(alcunha);
        telefone = vazioParaNulo(telefone);
        endereco = vazioParaNulo(endereco);
    }

    private static String vazioParaNulo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
