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
  são imutáveis: só inserção. Padrão (ver `HistoricoStatusProcedimento`): `@Immutable` (Hibernate) + só `@Getter`
  (sem `@Setter`, exceção justificada à regra geral) + construtor com todos os dados + `@NoArgsConstructor(access =
  PROTECTED)`; data via `@CreationTimestamp`; responsável vem do usuário autenticado. Gravar a mudança de estado da
  entidade e o histórico no mesmo método `@Transactional`. Sem `@OneToMany` na entidade de origem: consultar via
  repository (ex.: `findByProcedimentoIdOrderByDataTransicaoAsc`).
  Exceção: `historico_custodia` precisa atualizar `dataFim` ao fechar a custódia, então não pode ser `@Immutable`
  inteira — usar `updatable = false` nas demais colunas.
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
  Limitação aceita (MVP): usuário desativado mantém o token válido até expirar (até 8h) e pode agir nesse
  intervalo, inclusive reativar a si mesmo. Melhoria futura: conferir o status a cada requisição (ver "Melhorias futuras").
  Implementação: `SecurityConfig` (BCrypt, `SecurityFilterChain` stateless, `/auth/login` público, resto autenticado,
  `JwtEncoder`/`JwtDecoder` HS256 a partir de `JWT_SECRET` — mínimo 32 caracteres —, conversor que lê o claim `role`
  sem prefixo; CORS via `.cors(Customizer.withDefaults())` + bean `CorsConfigurationSource` com origens de
  `CORS_ORIGENS_PERMITIDAS`, separadas por vírgula, padrão `http://localhost:4200`; cabeçalhos `Authorization` e
  `Content-Type`). `TokenService.gerar(usuario)`: claims `sub` (login), `nome`, `role`, validade 8h.
  `AuthService.login` (opção A, sem `UserDetailsService`): `findByLogin` → `passwordEncoder.matches` → status
  APROVADO, senão `CredenciaisInvalidasException` (401, mensagem genérica) ou `CadastroNaoAprovadoException` (403).
  Rotas: `POST /auth/login`, `GET /auth/usuario-logado` (lê do token via `@AuthenticationPrincipal Jwt`).
  Evolução futura possível: `UserDetailsService` + `AuthenticationManager` trocando só o miolo do `AuthService`.
- Pacotes: camadas `model`, `enums`, `repository` (interfaces estendendo `JpaRepository` direto, sem interface base),
  `dto` (records com sufixo `RequestDto`/`ResponseDto`), `service`, `controller`, cada uma dividida em subpacotes
  por domínio (nomes em inglês): `auth`, `user`, `role`, `procedure`, `custody`, `delegation` (repasse), `hearing`
  (oitiva), `party`, `vehicle`, `examination` (perícia), `notification`. Ex.: `service/user/UsuarioService`,
  `model/delegation/Repasse`. Fora das camadas: `security` (SecurityConfig, TokenService), `exception` (exceções
  genéricas `RegraNegocioException` 400, `RecursoNaoEncontradoException` 404, `RecursoDuplicadoException` 409 +
  `GlobalExceptionHandler`), `config` (DadosIniciais). `AuthService` fica em `service/auth`.
- Injeção por construtor com `@RequiredArgsConstructor` (campos `final`); não usar `@Autowired`. `@Value` do Spring
  (`org.springframework.beans.factory.annotation.Value`, não o do Lombok) só para configuração.
- Erros da API: lançar exceção de negócio e tratá-la no `GlobalExceptionHandler` (`@RestControllerAdvice`), que
  devolve `ErroResponseDto(status, mensagem, detalhes, dataHora)` (fica no pacote `exception`, junto do handler). Validação de DTO (`@Valid`) vira 400 com a lista de
  campos em `detalhes`.
- Textos de UI/mensagens e documentação em português.

## Modelo de domínio

As 11 entidades JPA estão implementadas em `model/` (nomes abaixo = atributos Java reais; colunas em snake_case).

| Entidade | Pontos-chave |
|---|---|
| `Role` | `acesso` (UNIQUE, não é enum, padrão `ROLE_ADMIN`/`ROLE_DELEGADO`/`ROLE_POLICIAL`/`ROLE_ESTAGIARIO`), `descricao`. Implementa `GrantedAuthority` (`getAuthority()` devolve `acesso`) |
| `Usuario` | `nome`, `matricula` UK, `login` UK, `senhaHash`, `statusUsuario: StatusUsuario` (coluna `status`), FK `role` (N:1), `motivoRejeicao` (500, nullable; preenchido ao rejeitar). **Sem e-mail** |
| `Procedimento` | `tipoProcedimento` (coluna `tipo`), `numeroProcedimento: Long` (coluna `numero`, bigint; só dígitos, formatação na UI), `anoProcedimento: Integer` (coluna `ano`), `crime`, `dataAbertura`/`dataRemessaFinal` (`LocalDate`), `protocoloRemessaFinal`, `statusProcedimento` (coluna `status`), FK `detentorAtual` (`detentor_atual_id`). **UNIQUE (tipo, numero, ano)**. Busca derivada: `findByTipoProcedimentoAndNumeroProcedimentoAndAnoProcedimento` |
| `HistoricoStatusProcedimento` | `@Immutable`. `statusAnterior` (nullable: registro da criação), `statusNovo`, `motivo` (500), `dataTransicao`, FKs `procedimento`, `responsavel` |
| `Repasse` | `statusRepasse` (coluna `status`, inicia PENDENTE), `justificativaRecusa` (500), `dataSolicitacao` (`@CreationTimestamp`), `dataResposta` (null até responder), FKs `procedimento`, `solicitante`, `destinatario`. CHECK `solicitante_id <> destinatario_id` via `@Table(check = @CheckConstraint)`. Métodos `aceitar()` / `recusar(justificativa)` preenchem status e `dataResposta` |
| `HistoricoCustodia` | `dataInicio` (`@CreationTimestamp`), `dataFim` (null = custódia atual; única coluna atualizável), `origem: OrigemHistoricoCustodia`, FKs `procedimento`, `usuario`, `repasseOrigem` (`@OneToOne` LAZY, nullable). Construtor `(procedimento, usuario, origem, repasseOrigem)`; `encerrar()` preenche `dataFim` (erro se já encerrada). **Não é `@Immutable`** |
| `Oitiva` | `dataHora` (atualizável: remarcação), `statusOitiva` (coluna `status`, inicia AGENDADA), `motivoCancelamento`, `dataCadastro` (`@CreationTimestamp`), FKs `procedimento`, `responsavel`, `cadastradoPor` (`updatable = false`), `partes: List<OitivaParte>` (composição) |
| `OitivaParte` | `tipoParte: TipoParte`, `nomeParte` (coluna `nome_parte`), FK `oitiva`. Construtor `(tipoParte, nomeParte)` + construtor vazio `protected` |
| `Notificacao` | `tipoNotificacao` (coluna `tipo`), `mensagem`, `lida: Boolean` (inicia `false`), `dataCriacao` (`@CreationTimestamp`), `referenciaId` (Long, sem FK: id do registro de origem, tabela definida pelo tipo), FK `usuarioDestinatario`. Índice `(usuario_destinatario_id, lida)` |
| `Veiculo` | `tipoVeiculo`, `lacre` (UK, `length = 15`, `updatable = false`, sem setter; `definirLacre()`), `marca`, `modelo`, `cor`, `placa`/`chassi`/`motor` (nullable), `caracteristicasVisuais`/`observacoes` (`TEXT`), `statusPericia: StatusPericia` (coluna `pericia`), `situacaoVeiculo` (coluna `situacao`, inicia `NA_DEPOL`), FK `procedimento`. **Sem `localizacaoPatio`** (a situação já indica onde o veículo está) |
| `HistoricoSituacaoVeiculo` | `@Immutable`. `situacaoAnterior` (nullable), `situacaoNova`, `motivo` (500), `dataTransicao`, FKs `veiculo`, `responsavel` |

### Enums

- `StatusUsuario`: PENDENTE, APROVADO, REJEITADO, DESATIVADO
- `TipoProcedimento`: BO, IP, TCO, APF, AIAI, AAFAI, ROP (com `descricao`, ex.: TCO = Termo Circunstanciado de Ocorrência)
- `StatusProcedimento`: EM_ANDAMENTO, ARQUIVADO
- `StatusOitiva`: AGENDADA, CONCLUIDA, DESMARCADA (remarcar = editar data/hora mantendo AGENDADA; não há status REMARCADA)
- `StatusRepasse`: PENDENTE, ACEITO, RECUSADO
- `OrigemHistoricoCustodia`: CADASTRO_INICIAL, REPASSE
- `TipoParte`: INVESTIGADO, TESTEMUNHA, VITIMA
- `SituacaoVeiculo`: NA_DEPOL, EM_PATIO, DEVOLVIDO, DESCARTADO
- `StatusPericia`: SEM_PERICIA, PERICIA_EM_ANDAMENTO, PERICIA_CONCLUIDA, NAO_PRECISA_PERICIA
- `TipoNotificacao`: OITIVA_PROXIMA (-> oitiva), REPASSE_RECEBIDO / REPASSE_ACEITO / REPASSE_RECUSADO (-> repasse),
  CADASTRO_PENDENTE (-> usuario). Entre parênteses: tabela para onde aponta `referenciaId`

## Regras de negócio decididas

- **Login** apenas por `login` + senha (sem e-mail, simplificação para o hackathon). Usuário PENDENTE, REJEITADO
  ou DESATIVADO não autentica (403 com mensagem por status; o REJEITADO vê o motivo da rejeição).
- **Cadastro/aprovação (UC02/UC03)**: cadastro público nasce PENDENTE (nunca como ROLE_ADMIN) e notifica os Admins
  (CADASTRO_PENDENTE). Aprovar/rejeitar só a partir de PENDENTE; aprovar pode trocar o papel; rejeitar exige
  motivo, que o usuário vê ao tentar logar (sem notificação: ele não acessa o sistema). Login e matrícula de um
  rejeitado continuam reservados.
- **Gestão (UC13/UC14)**: editar (nome, matrícula, papel; login não muda) só APROVADO ou DESATIVADO; na gestão o
  papel ADMIN pode ser atribuído (promoção). O Admin não pode remover o próprio papel de Admin nem desativar a si
  mesmo (evita lockout; login do Admin logado via `@AuthenticationPrincipal Jwt` → `jwt.getSubject()`, passado ao
  service como `String`). Desativar só APROVADO; reativar só DESATIVADO. Redefinir senha (204) vale para qualquer status.
- **Senha**: não há recuperação self-service; o Admin redefine com senha provisória (RF05/UC14).
- **Procedimento**: numeração reinicia por tipo e ano, por isso a unicidade é (tipo, numero, ano).
  Arquivar exige que não haja repasse PENDENTE; procedimento arquivado não aceita repasse; reabertura volta
  para EM_ANDAMENTO com motivo livre e registro em histórico.
- **Repasse**: fluxo pendente -> aceito/recusado; só no aceite a custódia muda. No aceite, no mesmo
  `@Transactional`: `repasse.aceitar()`, `custodiaAtual.encerrar()`, novo `HistoricoCustodia(..., REPASSE, repasse)`
  e `procedimento.setDetentorAtual(destinatario)`.
- **Oitiva**: conflito de horário é verificado por responsável; oitiva CONCLUIDA/DESMARCADA não é editável.
  Desmarcar exige motivo (`motivoCancelamento`). Remarcar só altera `dataHora`. `dataCadastro` via
  `@CreationTimestamp`; `cadastradoPor` vem do usuário autenticado (JWT), nunca do formulário.
  `Oitiva` é dona das partes (composição): `@OneToMany(mappedBy = "oitiva", cascade = ALL, orphanRemoval = true)`,
  `@Size(min = 1)`, lista sem setter; usar sempre `adicionarParte()`/`removerParte()`. `dataHora` precisa ser
  atualizável (remarcação); `dataCadastro` e `cadastradoPor` são `updatable = false`.
- **Notificação**: `referenciaId` só é preenchido depois de salvar o registro de origem (antes o id é nulo).
  A tarefa agendada de OITIVA_PROXIMA checa `existsByTipoNotificacaoAndReferenciaId` antes de criar, para não
  repetir. Só o próprio destinatário pode marcar a notificação como lida.
- **Lacre do veículo**: gerado no service, formato `LAC-AAAA-NNNNNN` (`String.format("LAC-%d-%06d", ano, n)`).
  O número vem da sequence `seq_lacre_veiculo`, criada em `src/main/resources/schema.sql` (o Hibernate não cria
  sequence que não é de id; exige `spring.sql.init.mode=always`). Leitura via SQL nativo justificado (RNF06):
  `@Query(value = "SELECT nextval('seq_lacre_veiculo')", nativeQuery = true)` no `VeiculoRepository`. A numeração
  não reinicia por ano e pode ter lacunas. Não usar `@PrePersist` com o id (ainda é nulo nesse momento).
  Comentários em `schema.sql` usam `--` (`#` quebra a inicialização). Ao criar `data.sql` (seed de roles/admin),
  acrescentar `spring.jpa.defer-datasource-initialization=true`.
- **Situação do veículo**: começa em NA_DEPOL. Transições **não são bloqueadas** por decisão de projeto
  (DEVOLVIDO/DESCARTADO normalmente são finais, mas a reversão é permitida com confirmação na UI).
  Toda mudança exige motivo e gera `HistoricoSituacaoVeiculo`.
  Nova situação igual à atual é rejeitada.
- **Perfis**: Admin (usuários/cadastros); Delegado e Policial (mesmas permissões, herdam de "Servidor");
  Estagiário só lê oitivas e veículos, participa de posse/repasse, não arquiva nem registra remessa.

## Ambiente e dados iniciais

- Variáveis vêm do `.env` na raiz via `spring.config.import=optional:file:.env[.properties]` (`.env` no
  `.gitignore`; `.env.example` versionado). Inclui `DB_*`, `JWT_SECRET`, `ADMIN_LOGIN`, `ADMIN_PASSWORD`,
  `CORS_ORIGENS_PERMITIDAS`.
- `config/DadosIniciais` (`CommandLineRunner`) cria as 4 roles e o Admin (APROVADO, senha BCrypt) se não existirem.
- `ddl-auto=update` só acrescenta: nunca remove, renomeia nem muda tipo de coluna. Ao renomear campo/coluna ou
  mudar tipo, recriar o banco de desenvolvimento (`DROP SCHEMA public CASCADE; CREATE SCHEMA public;`).

## Próximos passos

Desenvolvimento em fatias verticais (módulo completo: repository → service → DTO → controller), definindo antes o
contrato (rotas + JSON) para o frontend poder trabalhar em paralelo. Ordem: ✅ login → usuários (✅ UC02) (cadastro,
aprovação, gestão) → procedimentos (custódia inicial, histórico, arquivar/reabrir) → repasse → oitivas → veículos →
notificações (criadas dentro dos módulos; rota de listagem/lida e tarefa agendada no fim) → exportação.
CORS configurado para o Angular (`localhost:4200`); para outra origem, acrescentar em `CORS_ORIGENS_PERMITIDAS`.

## Melhorias futuras (fora do MVP)

- Revogação imediata de acesso: validar o status do usuário a cada requisição (ex.: no conversor JWT do
  `SecurityConfig`, buscando o usuário e recusando se não estiver APROVADO) ou manter lista de tokens revogados.
- Padronizar as respostas 401/403 do Spring Security no formato `ErroResponseDto`.

## Decisões em aberto

- Modelo de implantação (instância local por delegacia vs. SaaS multi-tenant) — ver seção 1.8 da documentação.

## Diagramas

Gerados por código em `docs/diagramas/fonte/` (Python gera HTML/SVG; Playwright/Chromium renderiza PNG e PDF).
Para alterar uma tabela/classe, editar `spec.py` (DER e classes) ou `uc.py` (casos de uso) e rodar, dentro
dessa pasta: `python3 spec.py && python3 uc.py && node render.js` (com `playwright` disponível no `NODE_PATH`).
Os arquivos gerados (`*.html`, `*.json`, `*.png`, `*.pdf`) saem na própria pasta; copiar os finais para
`docs/diagramas/`.
