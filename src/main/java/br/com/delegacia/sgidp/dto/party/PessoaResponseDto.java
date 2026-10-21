package br.com.delegacia.sgidp.dto.party;

import br.com.delegacia.sgidp.model.party.Pessoa;

import java.time.LocalDate;
import java.time.Period;

// Ficha completa (cadastro, edição e GET /pessoas/{id}). CPF sempre mascarado (LGPD)
public record PessoaResponseDto(
        Long id,
        String nome,
        LocalDate dataNascimento,
        Integer idade,
        String cpf,
        String alcunha,
        String telefone,
        String endereco
) {

    public static PessoaResponseDto de(Pessoa pessoa) {
        return new PessoaResponseDto(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getDataNascimento(),
                calcularIdade(pessoa.getDataNascimento()),
                mascararCpf(pessoa.getCpf()),
                pessoa.getAlcunha(),
                pessoa.getTelefone(),
                pessoa.getEndereco()
        );
    }

    static Integer calcularIdade(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            return null;
        }
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    // "12345678909" -> "***.456.789-**"
    static String mascararCpf(String cpf) {
        if (cpf == null) {
            return null;
        }
        return "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }
}
