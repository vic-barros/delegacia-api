# SGI-DP — Sistema de Gestão Interna da Delegacia de Polícia (v2)

API REST em Spring Boot para controle de oitivas, custódia de procedimentos policiais e veículos apreendidos.
Projeto acadêmico/portfólio (IFS, Inovathon). Reescrita da v1 (Java puro + JDBC) com arquitetura em camadas.

- Documentação completa (visão, RFs, RNFs, casos de uso, diagramas): `docs/Sistema_Delegacia_v2_Documentacao_SGI-DP.docx`
- DER e diagrama de classes: `docs/diagramas/` (PDF e PNG)

## Stack

- Java 21, Spring Boot 4, Spring Data JPA, Spring Security (JWT, stateless), PostgreSQL, Lombok
- Frontend separado em Angular (não está neste repositório)
- Pacote base: `br.com.delegacia.sgidp`
- Variáveis de ambiente do banco e `JWT_SECRET`: ver `.env.example`
- Build/teste: `./mvnw test`

## Convenções de código (obrigatórias)

- Camadas: Controller -> Service -> Repository. Nada de regra de negócio no Controller.
- Entidades JPA: `@Getter`/`@Setter` do Lombok. **Nunca `@Data` em entidade** (risco de StackOverflowError em
  equals/hashCode/toString com relacionamentos). `@Data` só em DTOs.
- `@SequenceGenerator` explícito por entidade; `@JoinColumn` + `@ForeignKey` com nome explícito em toda FK.
- Enums com `@Enumerated(EnumType.STRING)`.
- Relacionamentos: preferir `@ManyToOne(fetch = FetchType.LAZY)` unidirecional; `@OneToMany` só quando necessário.
- Validação de entrada com Bean Validation nos DTOs; senhas com BCrypt.
- Tabelas de histórico (`historico_custodia`, `historico_status_procedimento`, `historico_situacao_veiculo`)
  são imutáveis: só inserção (`@Immutable` ou `updatable = false`). Nunca sobrescrever registro anterior.
- Nomes no banco: sequence `seq_<tabela>`; FK `fk_<tabela>_<referência>` (ex.: `fk_usuario_role`);
  unique `unique_<coluna>` declarada só na `@Table` (sem `unique = true` duplicado na `@Column`).
- Atributos Java em camelCase com `@Column(name = "snake_case")` quando o nome da coluna diferir
  (ex.: `senhaHash` -> `senha_hash`). Campo de relacionamento guarda o objeto: `role`, não `roleId`.
- Bean Validation: `@NotBlank` só em `String`; enums e relacionamentos usam `@NotNull`
  (com `spring-boot-starter-validation`, o Hibernate valida no persist e `@NotBlank` em não-String lança
  `UnexpectedTypeException`).
- Segurança: authorities com prefixo `ROLE_`; na configuração usar `hasRole("ADMIN")` (o Spring adiciona o
  prefixo) ou `hasAuthority("ROLE_ADMIN")`. O claim de papel no JWT guarda o valor completo (`ROLE_...`).
- JWT enxuto (decisão para o hackathon): usar o suporte nativo do Spring Security (OAuth2 Resource Server,
  HS256 com `JWT_SECRET`), sem filtro JWT manual. Só `POST /auth/login` emitindo access token (claims: login e
  papel; validade ~8h). Sem refresh token, sem blacklist/logout no servidor (logout = front descarta o token).
  Limitação aceita: usuário desativado mantém o token válido até expirar.
- Textos de UI/mensagens e documentação em português.

## Modelo de domínio

| Entidade | Pontos-chave |
|---|---|
| `Role` | catálogo extensível; `acesso` varchar UNIQUE (não é enum), no padrão Spring Security (`ROLE_ADMIN`, `ROLE_DELEGADO`, `ROLE_POLICIAL`, `ROLE_ESTAGIARIO`); `descricao`. Implementa `GrantedAuthority` (`getAuthority()` devolve `acesso`) |
| `Usuario` | `nome`, `matricula` UK, `login` UK, `senhaHash`, `status: StatusUsuario`, FK `role` (N:1). **Sem e-mail** |
| `Procedimento` | `tipo: TipoProcedimento`, `numero: Long` (bigint; só dígitos, sem zeros à esquerda — formatação fica na UI), `ano: Integer`, `crime`, `dataAbertura`, `dataRemessaFinal`, `protocoloRemessaFinal`, `status`, FK `detentorAtual` -> usuario. **UNIQUE (tipo, numero, ano)** |
| `HistoricoStatusProcedimento` | `statusAnterior`, `statusNovo`, `motivo` (texto livre, ex. cota judicial/ministerial), `dataTransicao`, FKs `procedimento`, `responsavel` |
| `Repasse` | `status: StatusRepasse`, `justificativaRecusa`, `dataSolicitacao`, `dataResposta`, FKs `procedimento`, `solicitante`, `destinatario` (solicitante != destinatario) |
| `HistoricoCustodia` | `dataInicio`, `dataFim` (null = custódia atual), `origem: OrigemCustodia`, FKs `procedimento`, `usuario`, `repasseOrigem` (nullable) |
| `Oitiva` | `dataHora`, `status: StatusOitiva`, `motivoCancelamento`, `dataCadastro` (automático), FKs `procedimento`, `responsavel`, `cadastradoPor` |
| `OitivaParte` | `tipoParte: TipoParte`, `identificacaoParte`, FK `oitiva` |
| `Notificacao` | `tipo: TipoNotificacao`, `mensagem`, `lida`, `dataCriacao`, FK `usuarioDestinatario` |
| `Veiculo` | `tipoVeiculo`, `lacre` UK (gerado, imutável), `marca`, `modelo`, `cor`, `placa`/`chassi` (nullable), `motor`, `caracteristicasVisuais`, `pericia: Pericia`, `situacao: SituacaoVeiculo` (inicial `NA_DEPOL`), `localizacaoPatio` (nullable), `observacoes`, FK `procedimento` |
| `HistoricoSituacaoVeiculo` | `situacaoAnterior`, `situacaoNova`, `motivo`, `dataTransicao`, FKs `veiculo`, `responsavel` |

### Enums

- `StatusUsuario`: PENDENTE, APROVADO, REJEITADO, DESATIVADO
- `TipoProcedimento`: BO, IP, TCO, APF, AIAI, AAFAI, ROP (com `descricao`, ex.: TCO = Termo Circunstanciado de Ocorrência)
- `StatusProcedimento`: EM_ANDAMENTO, ARQUIVADO
- `StatusOitiva`: AGENDADA, CONCLUIDA, CANCELADA
- `StatusRepasse`: PENDENTE, ACEITO, RECUSADO
- `OrigemCustodia`: CADASTRO_INICIAL, REPASSE
- `TipoParte`: INVESTIGADO, TESTEMUNHA, VITIMA
- `SituacaoVeiculo`: NA_DEPOL, EM_PATIO, DEVOLVIDO, DESCARTADO
- `Pericia`: SEM_PERICIA, PERICIA_EM_ANDAMENTO, PERICIA_CONCLUIDA, NAO_PRECISA_PERICIA
- `TipoNotificacao`: valores a definir (oitiva próxima, repasse recebido, repasse respondido)

## Regras de negócio decididas

- **Login** apenas por `login` + senha (sem e-mail, simplificação para o hackathon). Usuário PENDENTE, REJEITADO
  ou DESATIVADO não autentica.
- **Senha**: não há recuperação self-service; o Admin redefine com senha provisória (RF05/UC14).
- **Procedimento**: numeração reinicia por tipo e ano, por isso a unicidade é (tipo, numero, ano).
  Arquivar exige que não haja repasse PENDENTE; procedimento arquivado não aceita repasse; reabertura volta
  para EM_ANDAMENTO com motivo livre e registro em histórico.
- **Repasse**: fluxo pendente -> aceito/recusado; só no aceite a custódia muda (fecha `dataFim` do histórico
  atual e abre um novo com origem REPASSE).
- **Oitiva**: conflito de horário é verificado por responsável; oitiva CONCLUIDA/CANCELADA não é editável.
- **Lacre do veículo**: gerado na camada de service, formato `LAC-AAAA-NNNNNN` (ano corrente + sequencial de
  6 dígitos de uma sequence dedicada do banco, separada da sequence do id). Não usar `@PrePersist` com o id
  (o id ainda é nulo nesse momento).
- **Situação do veículo**: começa em NA_DEPOL. Transições **não são bloqueadas** por decisão de projeto
  (DEVOLVIDO/DESCARTADO normalmente são finais, mas a reversão é permitida com confirmação na UI).
  Toda mudança exige motivo e gera `HistoricoSituacaoVeiculo`. Ir para EM_PATIO exige `localizacaoPatio`.
  Nova situação igual à atual é rejeitada.
- **Perfis**: Admin (usuários/cadastros); Delegado e Policial (mesmas permissões, herdam de "Servidor");
  Estagiário só lê oitivas e veículos, participa de posse/repasse, não arquiva nem registra remessa.

## Decisões em aberto

- Modelo de implantação (instância local por delegacia vs. SaaS multi-tenant) — ver seção 1.8 da documentação.

## Diagramas

Gerados por código em `docs/diagramas/fonte/` (Python gera HTML/SVG; Playwright/Chromium renderiza PNG e PDF).
Para alterar uma tabela/classe, editar `spec.py` (DER e classes) ou `uc.py` (casos de uso) e rodar, dentro
dessa pasta: `python3 spec.py && python3 uc.py && node render.js` (com `playwright` disponível no `NODE_PATH`).
Os arquivos gerados (`*.html`, `*.json`, `*.png`, `*.pdf`) saem na própria pasta; copiar os finais para
`docs/diagramas/`.
