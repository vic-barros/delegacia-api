package br.com.delegacia.sgidp.repository.party;

import br.com.delegacia.sgidp.model.party.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.LongSummaryStatistics;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    boolean existsByCpf(String cpf);

    // Na edição: outro cadastro (id diferente) já usa esse CPF?
    boolean existsByCpfAndIdNot(String cpf, Long id);

    // Busca com filtros opcionais (nulo = ignora): "nome" procura parte do texto no nome ou na alcunha,
    // sem diferenciar maiúsculas; CPF exato
    @Query("""
            SELECT p FROM Pessoa p
            WHERE (:nome IS NULL
                   OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))
                   OR LOWER(p.alcunha) LIKE LOWER(CONCAT('%', :nome, '%')))
              AND (:cpf IS NULL OR p.cpf = :cpf)
            ORDER BY p.nome
            """)
    List<Pessoa> buscar(String nome, String cpf);

}
