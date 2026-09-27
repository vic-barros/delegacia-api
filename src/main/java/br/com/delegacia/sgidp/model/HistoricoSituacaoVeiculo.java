package br.com.delegacia.sgidp.model;

import br.com.delegacia.sgidp.enums.SituacaoVeiculo;
import br.com.delegacia.sgidp.enums.StatusProcedimento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable //O hibernate ignora qualquer alteração dps do insert, o update nunca vai para o banco
@Table(name = "historico_situacao_veiculo")
@SequenceGenerator(name = "seq_historico_situacao_veiculo", sequenceName = "seq_historico_situacao_veiculo", allocationSize = 1, initialValue = 1)
@Getter //Só getter: histórico não se altera
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoricoSituacaoVeiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_historico_situacao_veiculo")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_anterior", nullable = true)
    private SituacaoVeiculo situacaoAnterior;

    @NotNull(message = "A situação nova do veículo deve ser informada")
    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_nova", nullable = false)
    private SituacaoVeiculo situacaoNova;

    @NotBlank(message = "O motivo da mudança de situação do veículo deve ser informado")
    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @CreationTimestamp
    @Column(name = "data_transicao", nullable = false, updatable = false)
    private LocalDateTime dataTransicao;

    @NotNull(message = "O veículo deve ser informado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_historico_situacao_veiculo_veiculo"))
    private Veiculo veiculo;

    @NotNull(message = "O histórico deve registrar o responsável pela mudança")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_historico_situacao_veiculo_responsavel"))
    private Usuario responsavel;

    public HistoricoSituacaoVeiculo(SituacaoVeiculo situacaoAnterior, SituacaoVeiculo situacaoNova,
    String motivo, Veiculo veiculo, Usuario responsavel) {
        this.situacaoAnterior = situacaoAnterior;
        this.situacaoNova = situacaoNova;
        this.motivo = motivo;
        this.veiculo = veiculo;
        this.responsavel = responsavel;
    }
}
