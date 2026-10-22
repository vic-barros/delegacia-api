package br.com.delegacia.sgidp.dto.party;

import br.com.delegacia.sgidp.model.party.Pessoa;

// Resultado da busca (autocompletar e lista): só o que identifica a pessoa.
// Telefone e endereço ficam de fora (LGPD: minimização); aparecem só na ficha
public record PessoaResumoResponseDto(
        Long id,
        String nome,
        String alcunha,
        Integer idade,
        String cpf
) {

    public static PessoaResumoResponseDto de(Pessoa pessoa) {
        return new PessoaResumoResponseDto(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getAlcunha(),
                PessoaResponseDto.calcularIdade(pessoa.getDataNascimento()),
                PessoaResponseDto.mascararCpf(pessoa.getCpf())
        );
    }
}
