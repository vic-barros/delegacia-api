# SISGEP — Sistema Interno de Gestão Policial (v2)

API REST em Spring Boot para controle de oitivas, custódia de procedimentos policiais e veículos apreendidos.
Projeto acadêmico/portfólio (IFS, Inovathon). Reescrita da v1 (Java puro + JDBC) com arquitetura em camadas.
Nome anterior: SGI-DP. O pacote Java (`br.com.delegacia.sgidp`), a classe `SgiDpApplication` e o `artifactId` mantêm
o nome antigo de propósito: renomear mexe em todos os arquivos e conflita com as branches abertas (fazer, se for o
caso, depois da apresentação, com tudo mergeado, num PR único via refactor da IDE). Logo: `docs/img/logo-sisgep.jpg`.

- Documentação completa (visão, RFs, RNFs, casos de uso, diagramas): `docs/SISGEP_Documentacao.docx`
- DER e diagrama de classes: `docs/diagramas/` (PDF e PNG)
- Contrato das rotas para o front (JSON de entrada/saída e erros): `docs/endpoints.md`
- Protótipo das telas do front (dados fictícios; seção 9 do docx): `docs/img/telas/`

## Stack

- Java 21, Spring Boot 4, Spring Data JPA, Spring Security (JWT, stateless), PostgreSQL, Lombok
- Arquivos: Apache PDFBox + ZXing `core` (etiqueta com QR), Apache POI `poi-ooxml` (XLSX) e OpenPDF 3 (PDF com
  tabela; pacote `org.openpdf`, não o antigo `com.lowagie`) nas exportações
- Fuso: `TimeZone.setDefault(America/Sao_Paulo)` no início do `main()` (antes disso o `@CreationTimestamp` saía em UTC)
- Frontend separado em React (não está neste repositório; dev server Vite em `http://localhost:5173`)
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
  (oitiva), `party`, `vehicle`, `examination` (perícia), `notification`, `export` (peças comuns das exportações:
  `enums/export/FormatoExportacao`, `dto/export/ArquivoExportadoResponseDto`). Ex.: `service/user/UsuarioService`,
  `model/delegation/Repasse`. Fora das camadas: `security` (SecurityConfig, TokenService), `exception` (exceções
  genéricas `RegraNegocioException` 400, `RecursoNaoEncontradoException` 404, `RecursoDuplicadoException` 409 +
  `GlobalExceptionHandler`), `config` (DadosIniciais). `AuthService` fica em `service/auth`.
- Injeção por construtor com `@RequiredArgsConstructor` (campos `final`); não usar `@Autowired`. `@Value` do Spring
  (`org.springframework.beans.factory.annotation.Value`, não o do Lombok) só para configuração.
- Erros da API: lançar exceção de negócio e tratá-la no `GlobalExceptionHandler` (`@RestControllerAdvice`), que
  devolve `ErroResponseDto(status, mensagem, detalhes, dataHora)` (fica no pacote `exception`, junto do handler). Validação de DTO (`@Valid`) vira 400 com a lista de
  campos em `detalhes`.
- Textos de UI/mensagens e documentação em português.
- Rotas no plural (`/procedimentos`, `/repasses`, `/oitivas`, `/veiculos`, `/usuarios`, `/notificacoes`).
- Regras do `SecurityConfig`: o Spring usa a **primeira** que combina com a rota, então a específica vem antes da
  geral (ex.: `GET /veiculos/exportar` antes de `GET /veiculos/**`). Rota literal (`/exportar`) convive com `/{id}`.

## Modelo de domínio

As entidades JPA estão em `model/` (11 na `main`; `Pessoa` na PR `feature/pessoas`) (nomes abaixo = atributos Java reais; colunas em snake_case).

| Entidade | Pontos-chave |
|---|---|
| `Role` | `acesso` (UNIQUE, não é enum, padrão `ROLE_ADMIN`/`ROLE_DELEGADO`/`ROLE_POLICIAL`/`ROLE_ESTAGIARIO`), `descricao`. Implementa `GrantedAuthority` (`getAuthority()` devolve `acesso`) |
| `Usuario` | `nome`, `matricula` UK, `login` UK, `senhaHash`, `statusUsuario: StatusUsuario` (coluna `status`), FK `role` (N:1), `motivoRejeicao` (500, nullable; preenchido ao rejeitar). **Sem e-mail** |
| `Procedimento` | `tipoProcedimento` (coluna `tipo`), `numeroProcedimento: Long` (coluna `numero`, bigint; só dígitos, formatação na UI), `anoProcedimento: Integer` (coluna `ano`), `crime`, `dataAbertura`/`dataRemessaFinal` (`LocalDate`), `protocoloRemessaFinal`, `statusProcedimento` (coluna `status`), FK `detentorAtual` (`detentor_atual_id`). **UNIQUE (tipo, numero, ano)**. Busca derivada: `findByTipoProcedimentoAndNumeroProcedimentoAndAnoProcedimento` |
| `HistoricoStatusProcedimento` | `@Immutable`. `statusAnterior` (nullable: registro da criação), `statusNovo`, `motivo` (500), `dataTransicao`, FKs `procedimento`, `responsavel` |
| `Repasse` | `statusRepasse` (coluna `status`, inicia PENDENTE), `justificativaRecusa` (500), `dataSolicitacao` (`@CreationTimestamp`), `dataResposta` (null até responder), FKs `procedimento`, `solicitante`, `destinatario`. CHECK `solicitante_id <> destinatario_id` via `@Table(check = @CheckConstraint)`. Métodos `aceitar()` / `recusar(justificativa)` preenchem status e `dataResposta` |
| `HistoricoCustodia` | `dataInicio` (`@CreationTimestamp`), `dataFim` (null = custódia atual; única coluna atualizável), `origem: OrigemHistoricoCustodia`, FKs `procedimento`, `usuario`, `repasseOrigem` (`@OneToOne` LAZY, nullable). Construtor `(procedimento, usuario, origem, repasseOrigem)`; `encerrar()` preenche `dataFim` (erro se já encerrada). **Não é `@Immutable`** |
| `Oitiva` | `dataHora` (atualizável: remarcação), `statusOitiva` (coluna `status`, inicia AGENDADA), `motivoCancelamento`, `dataCadastro` (`@CreationTimestamp`), FKs `procedimento`, `responsavel`, `cadastradoPor` (`updatable = false`), `alteradoPor` (nullable: último que editou/remarcou/desmarcou/concluiu), `partes: List<OitivaParte>` (composição). `motivoCancelamento` com 500 |
| `OitivaParte` | `tipoParte: TipoParte`, `nomeParte` (coluna `nome_parte`, retrato do nome na data da oitiva), FK `oitiva`, FK `pessoa` (opcional, `fk_oitiva_parte_pessoa`). Construtores `(tipoParte, nomeParte)` e `(tipoParte, nomeParte, pessoa)` + vazio `protected`. `OitivaParteRepository` só para consulta (`buscarParticipacoes`) |
| `Pessoa` | pacote `party`. `nome` (150), `dataNascimento`, `cpf` (11 dígitos, UNIQUE `unique_cpf`, nullable), `alcunha` (100), `telefone` (20), `endereco` (300, texto livre), `dataCadastro`, FK `cadastradoPor` (`updatable = false`). LGPD: só esses dados; CPF mascarado na resposta |
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
- `TipoNotificacao`: OITIVA_PROXIMA / OITIVA_SEM_BAIXA / OITIVA_ATRIBUIDA (-> oitiva), REPASSE_RECEBIDO /
  REPASSE_ACEITO / REPASSE_RECUSADO (-> repasse), CADASTRO_PENDENTE (-> usuario). Entre parênteses: tabela para onde aponta `referenciaId`

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
  Cadastro em `POST /procedimentos` (Delegado/Policial).
  Arquivar exige que não haja repasse PENDENTE nem oitiva AGENDADA (400 listando as datas: "conclua as já
  realizadas e desmarque as demais"; consulta pronta em `OitivaRepository.findByProcedimentoIdAndStatusOitivaOrderByDataHoraAsc`);
  procedimento arquivado não aceita repasse nem oitiva; reabertura volta para EM_ANDAMENTO com motivo livre e
  registro em histórico.
  **Sem controle de prazo** (decisão: o sistema institucional da Polícia Civil já controla; o SISGEP é controle
  interno) e **sem status REMETIDO**: "Remetidos" na listagem = filtro derivado (remessa final registrada).
  Remetido que volta para diligências = reabrir. Listagem (RF36, colega): abas em andamento/arquivados/remetidos/
  todos com contagem, filtros tipo (vários), ano, detentor e texto (número/crime), paginada, linha indica repasse
  pendente.
- **Repasse**: fluxo pendente -> aceito/recusado; só no aceite a custódia muda. No aceite, no mesmo
  `@Transactional`: `repasse.aceitar()`, `custodiaAtual.encerrar()`, novo `HistoricoCustodia(..., REPASSE, repasse)`
  e `procedimento.setDetentorAtual(destinatario)`.
  Solicitação pronta (UC08): `POST /repasses` `{idDestinatario, idProcedimento}` (Delegado/Policial/Estagiário),
  só o detentor atual solicita; destinatário Delegado/Policial/Estagiário APROVADO e diferente do solicitante;
  procedimento ARQUIVADO ou com repasse PENDENTE recusa (400); ids inexistentes 404. Notifica o destinatário
  (REPASSE_RECEBIDO, "procedimento IP 123/2026 de Fulano"). `saveAndFlush` para a `dataSolicitacao` vir preenchida.
  Pendentes (colega): aceitar/recusar (UC09) com REPASSE_ACEITO/RECUSADO e listagem (recebidos, enviados,
  histórico). A implementar: `observacao` opcional (500) no `POST /repasses` e na resposta (tela de repasses).
- **Oitiva**: Delegado, Policial e Estagiário agendam, editam, remarcam, desmarcam e concluem (o Estagiário conduz
  oitivas sob supervisão e agenda para os policiais); Admin não acessa. Responsável: Delegado, Policial ou
  Estagiário APROVADO. Procedimento ARQUIVADO não aceita oitiva (400 "Procedimento arquivado. É necessário
  desarquivar para novas diligências."). Data/hora no passado é bloqueada no agendamento e na remarcação (`@Future`).
  Conflito de horário por responsável (outra AGENDADA até 59 min antes/depois, ignorando a própria) **não bloqueia**:
  409 `confirmarConflito` (agendar, remarcar, editar trocando o responsável). Oitiva CONCLUIDA/DESMARCADA não é
  editável. Desmarcar exige motivo (`motivoCancelamento`). Remarcar só altera `dataHora`. Concluir é permitido a
  qualquer momento; se for antes da data marcada (oitiva antecipada), `dataHora` vira o momento da conclusão.
  "Atrasada" (AGENDADA com data passada, sem baixa) não é status salvo: é calculada na resposta (`atrasada`) e
  filtrável (`?atrasadas=true`). `dataCadastro` via `@CreationTimestamp`; `cadastradoPor` e `alteradoPor` vêm do
  usuário autenticado (JWT), nunca do formulário.
  Rotas: `POST /oitivas`, `GET /oitivas[?dataInicio&dataFim&responsavelId&status&procedimentoId&minhas&atrasadas]`
  (datas `yyyy-MM-dd`), `GET /oitivas/{id}`, `PUT /oitivas/{id}` (responsável + partes; a lista substitui a
  anterior), `PATCH /oitivas/{id}/remarcar`, `PATCH /oitivas/{id}/desmarcar`, `PATCH /oitivas/{id}/concluir`.
  `Oitiva` é dona das partes (composição): `@OneToMany(mappedBy = "oitiva", cascade = ALL, orphanRemoval = true)`,
  `@Size(min = 1)`, lista sem setter; usar sempre `adicionarParte()`/`removerParte()`. `dataHora` precisa ser
  atualizável (remarcação); `dataCadastro` e `cadastradoPor` são `updatable = false`.
- **Notificação**: `referenciaId` só é preenchido depois de salvar o registro de origem (antes o id é nulo).
  Avisos de oitiva (`service/hearing/OitivaNotificacaoService`), sempre para o responsável:
  OITIVA_ATRIBUIDA na hora (agendar ou trocar o responsável, se quem fez não é o próprio responsável);
  tarefa agendada (`@Scheduled`, cron em `NOTIFICACAO_CRON`, padrão `0 */10 * * * *`) cria OITIVA_PROXIMA para
  AGENDADA nas próximas 24h e OITIVA_SEM_BAIXA para AGENDADA com data passada há mais de 2h; checa
  `existsByTipoNotificacaoAndReferenciaId` antes de criar (um aviso de cada tipo por oitiva). Remarcar ou trocar
  o responsável apaga PROXIMA/SEM_BAIXA da oitiva (a tarefa gera de novo); concluir/desmarcar marca os avisos da
  oitiva como lidos. Novo valor em `TipoNotificacao` exige recriar o banco de desenvolvimento: o Hibernate cria
  um CHECK com os valores do enum na coluna `tipo` e o `ddl-auto=update` não o atualiza (insert dá 500). Só o próprio destinatário pode marcar a notificação como lida (consultas filtradas por
  `UsuarioDestinatarioLogin`; de outra pessoa = 404). Rotas: `GET /notificacoes[?lida=]`,
  `GET /notificacoes/nao-lidas/contagem`, `PATCH /notificacoes/{id}/lida`, `PATCH /notificacoes/lidas`.
  Assunto resolvido baixa o aviso de todos: aprovar/rejeitar cadastro chama
  `notificacaoService.marcarComoLidas(CADASTRO_PENDENTE, usuarioId)` na mesma transação.
- **Lacre do veículo**: gerado no service, formato `LAC-AAAA-NNNNNN` (`String.format("LAC-%d-%06d", ano, n)`).
  O número vem da sequence `seq_lacre_veiculo`, criada em `src/main/resources/schema.sql` (o Hibernate não cria
  sequence que não é de id; exige `spring.sql.init.mode=always`). Leitura via SQL nativo justificado (RNF06):
  `@Query(value = "SELECT nextval('seq_lacre_veiculo')", nativeQuery = true)` no `VeiculoRepository`. A numeração
  não reinicia por ano e pode ter lacunas. Não usar `@PrePersist` com o id (ainda é nulo nesse momento).
  Comentários em `schema.sql` usam `--` (`#` quebra a inicialização). Ao criar `data.sql` (seed de roles/admin),
  acrescentar `spring.jpa.defer-datasource-initialization=true`.
- **Etiqueta do veículo**: `GET /veiculos/{id}/etiqueta` devolve PDF 100 x 50 mm (`inline`) gerado em
  `service/vehicle/EtiquetaVeiculoService` com ZXing `core` (QR, correção de erro Q, desenhado em vetor) e Apache
  PDFBox. QR = `ETIQUETA_URL_BASE` + `/veiculos/lacre/{lacre}` (tela do front, exige login; o QR não carrega dados).
  Conteúdo: unidade (`ETIQUETA_UNIDADE`, opcional), lacre, tipo/marca/modelo, cor, placa (se houver),
  procedimento, data de entrada (primeira linha do histórico) e data/hora da impressão; sem nomes de pessoas.
  Qualquer logado pode gerar (regra de GET de `/veiculos/**`). Acentos no `.env` em `\uXXXX` (o arquivo é lido
  como `.properties`).
- **Situação do veículo**: começa em NA_DEPOL. EM_PATIO = em pátio, que pode ser de outro órgão ou de uso da
  delegacia (ex.: pátio da prefeitura); por isso a carga continua NA_DEPOL + EM_PATIO e quem quiser só a DEPOL filtra. Transições **não são bloqueadas** por decisão de projeto
  (DEVOLVIDO/DESCARTADO normalmente são finais, mas a reversão é permitida com confirmação).
  Toda mudança exige motivo e gera `HistoricoSituacaoVeiculo`; o cadastro também gera (anterior `null` → NA_DEPOL).
  Nova situação igual à atual é rejeitada.
- **Veículos (contrato)**: rotas `POST /veiculos`, `GET /veiculos[?lacre&placa&chassi&motor&situacao&procedimentoId]`,
  `GET /veiculos/{id}`, `GET /veiculos/{id}/historico`, `PUT /veiculos/{id}`, `PATCH /veiculos/{id}/situacao`.
  GET para qualquer logado (inclusive Estagiário); escrita só Delegado/Policial. **Procedimento ARQUIVADO aceita
  veículo** (diferente de oitiva: o arquivamento encerra as diligências, não a destinação do veículo).
  Confirmações via flag no corpo + 409 (`ConfirmacaoNecessariaException`, nome da flag em `detalhes`):
  `confirmarDuplicidade` (placa, chassi ou motor de veículo ativo NA_DEPOL/EM_PATIO; os três normalizados trim +
  maiúsculas; na edição só o que mudou é verificado) e `confirmarReversao` (sair de DEVOLVIDO/DESCARTADO). O `PUT` edita só dados descritivos e perícia (RF26): não altera lacre, situação nem o
  procedimento vinculado; essas edições não são rastreadas no MVP (ver "Melhorias futuras").
- **Exportação (RF20/UC16)**: `GET /procedimentos/exportar` (filtros `dataInicio`/`dataFim` da abertura, `tipo`,
  `status`) e `GET /veiculos/exportar` (`situacao` repetível, `statusPericia`, `procedimentoId`, `dataInicio`/`dataFim`
  da entrada = primeira linha do histórico), ambas com `formato=XLSX|PDF` obrigatório e só Delegado/Policial.
  Resposta `attachment` com nome `procedimentos-|veiculos-|carga-veiculos-AAAAMMDD-HHMM.ext`; o CORS expõe
  `Content-Disposition` para o front ler o nome. Lista vazia = 404 com mensagem (UC16, nunca arquivo vazio); período
  invertido 400; parâmetro obrigatório ausente vira `ErroResponseDto` 400. Estrutura: service de regras
  (`Exportacao*Service`) + um gerador por formato (`*XlsxService`, `*PdfService`, PDF em A4 paisagem com cabeçalho
  repetido por página). Planilha com datas/números como valor (não texto), filtro e cabeçalho fixo.
  **Relatório de carga**: filtro exatamente NA_DEPOL + EM_PATIO (comparado como `EnumSet`, em qualquer ordem);
  totais por situação, ordem por situação (ordem do enum, feita em Java: no banco o enum é texto) e lacre; PDF com
  coluna "Conferido" em branco e linha de assinatura para a conferência física. Data de entrada de todos os veículos
  numa consulta só (`HistoricoSituacaoVeiculoRepository.buscarDatasEntrada`, projeção por interface); fim do período
  = meia-noite do dia seguinte com `<` (a data tem hora).
- **Importação de acervo (RF21, fase posterior)**: sem rota própria; alternativa simples já testada = chamar
  `POST /procedimentos` por linha (Postman Collection Runner com CSV ou script Python que lê CSV/XLSX e grava o log
  por linha). Quem importa vira o detentor; procedimentos já arquivados exigem arquivar depois.
- **Ofícios expedidos (documentado no docx: RF30-RF35, UC24-UC29; código a implementar)**: substitui o livro
  cartorário de ofícios; só expedidos. Campos: número/ano gerados pelo sistema (reinicia a cada ano, UNIQUE (numero,
  ano)), data de expedição (não futura; define o ano), órgão destinatário, assunto, procedimento vinculado (opcional,
  FK) e referência externa (opcional, texto: processo judicial etc.), status EXPEDIDO/CANCELADO, cadastradoPor,
  alteradoPor, canceladoPor. Numeração: tabela `numeracao_oficio` (ano, ultimo_numero) com a linha do ano travada
  (`@Lock(PESSIMISTIC_WRITE)`) ao gerar o próximo número. Cancelar exige motivo; número nunca é reaproveitado e o
  cancelado continua na consulta e no livro (como a linha riscada). Corrigir só destinatário/assunto/procedimento/
  referência e só EXPEDIDO; número e data imutáveis. Procedimento ARQUIVADO aceita ofício. Delegado, Policial e
  Estagiário em tudo (Admin não acessa); numeração inicial do ano (continuidade com o livro de papel) só Delegado e
  só sem ofício do ano no sistema. Rotas propostas: `POST/GET /oficios`, `GET/PUT /oficios/{id}`,
  `PATCH /oficios/{id}/cancelar`, `GET /oficios/exportar` (livro em PDF/XLSX). Pacote `letter`.
- **Perfis**: Admin (usuários/cadastros); Delegado e Policial (mesmas permissões, herdam de "Servidor");
  Estagiário: agenda/edita/remarca/desmarca/conclui oitivas e pode ser responsável por elas; só lê veículos
  (lista, ficha e etiqueta); participa de posse/repasse; não cadastra nem altera procedimentos, não arquiva nem
  registra remessa e não exporta.
- **Painel e Minha mesa (RF37/RF38, PR #24 `feature/painel-minhamesa`)**: só com os dados que já existem, nada salvo.
  Pacote `dashboard`; rotas `GET /painel` e `GET /painel/minha-mesa` (Delegado/Policial/Estagiário).
  `GET /painel`: oitivas de hoje do usuário (sem DESMARCADA), repasses pendentes (recebidos e enviados), card
  "Veículos nesta delegacia" = só NA_DEPOL (`total`, `semPericia`, `periciaEmAndamento`), procedimentos parados.
  Minha mesa só EM_ANDAMENTO; enum `ProximoPasso` (AGUARDANDO_ACEITE, OITIVA_ATRASADA, OITIVA_AGENDADA, PARADO, NENHUM)
  + texto pronto; ordem: passo, oitiva mais próxima (`nullsLast`), movimentação mais antiga. Uma consulta por fonte
  (GROUP BY + MAX, projeção `DataPorProcedimento`) juntada em mapas com `for`. Minha mesa: procedimentos com detentor = usuário logado,
  "na posse há N dias" (`dataInicio` da custódia aberta), próxima oitiva, repasse em andamento, contagem dos chips e
  "próximo passo" por prioridade: repasse pendente ("Aguardando aceite") → oitiva agendada ("Oitiva hoje, 16:30")
  → parado ("Parado há N dias"). Parado = sem movimentação há mais de 60 dias (última data entre custódia,
  histórico de status, oitiva cadastrada/concluída, repasse e histórico de veículo). "Repassar selecionados": o
  front chama `POST /repasses` uma vez por procedimento e mostra o resultado de cada um.
- **Pessoas (RF43, UC36, PR `feature/pessoas`)**: `POST/GET /pessoas[?nome=&cpf=]`, `GET/PUT /pessoas/{id}`,
  `GET /pessoas/{id}/participacoes` (Delegado/Policial/Estagiário). `@CPF` (Hibernate Validator) + normalização
  só dígitos; CPF repetido 409; construtor compacto do record transforma "" em nulo. Busca devolve
  `PessoaResumoResponseDto` (sem telefone/endereço); ficha `PessoaResponseDto`; idade calculada. Busca por `nome`
  também procura na alcunha. Oitiva: `pessoaId` opcional em cada parte (`criarParte` no `OitivaService`), 404 se
  não existir. Veículos não entram nas participações (ligados ao procedimento; o grafo usa `GET /veiculos?procedimentoId=`).
- **Outras lacunas do design (a implementar depois da apresentação)**: fotos da apreensão (front ainda não integra); busca global `GET /busca?q=` (procedimento "IP 45/2026", placa,
  lacre, ofício; resultados agrupados); fotos da apreensão (`FotoVeiculo`: arquivo em disco/volume, não no banco;
  só JPG/PNG até 5 MB conferindo o conteúdo real; envio Delegado/Policial, leitura todos; `POST/GET
  /veiculos/{id}/fotos`, `GET /veiculos/fotos/{id}`); alterar a própria senha `PATCH /usuarios/me/senha` (pede a
  atual). Ordem sugerida: Minha mesa + painel → ofícios → observação no repasse → senha → busca → fotos.
  Ajustes do front: tirar prazo (coluna, chips, card, "Pedir prorrogação"), "Etiqueta impressa" não é situação
  (sai do histórico) e incluir a tela de Ofícios no menu.
- **Assistente de IA (colega, branch `feat/resumos-procedimentos`; docx 1.9, RF42, RNF14)**: resumo só para o
  detentor atual; dados minimizados (partes pelo papel, ex.: "Testemunha 1"; sem matrícula/login); instruções
  fixas (só os dados enviados, texto livre é dado e não instrução); conferência de datas/números citados; resposta
  marcada como gerada por IA e não salva; chave só no `.env`; log de uso sem conteúdo; limite por usuário; demo só
  com dados fictícios; provedor fora do ar não derruba o resto.

## Ambiente e dados iniciais

- Variáveis vêm do `.env` na raiz via `spring.config.import=optional:file:.env[.properties]` (`.env` no
  `.gitignore`; `.env.example` versionado). Inclui `DB_*`, `JWT_SECRET`, `ADMIN_LOGIN`, `ADMIN_PASSWORD`,
  `CORS_ORIGENS_PERMITIDAS` e, opcionais, `NOTIFICACAO_CRON` (ex.: `*/10 * * * * *` = a cada 10 s, para
  demonstração), `ETIQUETA_URL_BASE` e `ETIQUETA_UNIDADE`.
- `config/DadosIniciais` (`CommandLineRunner`) cria as 4 roles e o Admin (APROVADO, senha BCrypt) se não existirem.
- `ddl-auto=update` só acrescenta: nunca remove, renomeia nem muda tipo de coluna. Ao renomear campo/coluna ou
  mudar tipo, recriar o banco de desenvolvimento (`DROP SCHEMA public CASCADE; CREATE SCHEMA public;`).

## Próximos passos

Desenvolvimento em fatias verticais (módulo completo: repository → service → DTO → controller), definindo antes o
contrato (rotas + JSON) para o frontend poder trabalhar em paralelo. Situação: ✅ login → ✅ usuários (cadastro,
aprovação, gestão, senha) → procedimentos (✅ cadastro com custódia inicial e histórico de status; listagem,
detalhe, arquivar/reabrir e remessa com o colega) → repasse (✅ solicitação; aceitar/recusar com o colega) →
✅ oitivas → ✅ veículos → notificações (✅ rotas; ✅ avisos de oitiva; ✅ REPASSE_RECEBIDO; aceito/recusado com o
repasse) → ✅ exportação (procedimentos e veículos/carga). Inovações: ✅ etiqueta com QR code; ✅ relatório de carga.
CORS: o padrão no código ainda é `localhost:4200` (Angular, plano inicial); com o front em React, definir
`CORS_ORIGENS_PERMITIDAS=http://localhost:5173` (Vite) no `.env`; outras origens, separadas por vírgula.

Em paralelo ao colega: documentação (docx, DER/diagramas, coleção Postman), OpenAPI/Swagger (gera o contrato das
rotas automaticamente; decidido manter `docs/endpoints.md` à mão, documento do colega, já com repasse, etiqueta,
exportações e painel),
dados de demonstração, Docker (PR `chore/docker`, RNF11) e testes automatizados (hoje só `contextLoads`).
Docker (docx 1.8 e Figura 10): `docker-compose.yml` com front (Nginx + React compilado, HTTPS, repassa `/api`), api
e banco (porta 5432 não publicada), `restart: unless-stopped`, volumes de dados e de fotos, backup diário com
`pg_dump`; conferir se a PR `chore/docker` cobre isso.

## Melhorias futuras (fora do MVP)

- Revogação imediata de acesso: validar o status do usuário a cada requisição (ex.: no conversor JWT do
  `SecurityConfig`, buscando o usuário e recusando se não estiver APROVADO) ou manter lista de tokens revogados.
- Padronizar as respostas 401/403 do Spring Security no formato `ErroResponseDto`.
- Rastreabilidade completa do veículo: troca de procedimento vinculado com motivo e histórico próprio, e registro
  das edições de dados (placa, chassi, perícia...).
- Gestão patrimonial da delegacia: bens com número de tombamento, categoria, setor/localização, servidor
  responsável, movimentações com histórico e baixa (armamento merece análise própria: cautela).
- Anexo do PDF do ofício ao registro.
- Assistente virtual com IA em produção: provedor sem retenção dos dados ou modelo local (LGPD, sigilo).
- Módulo de vínculos (colega, em desenvolvimento): grafo ligando pessoas, procedimentos, veículos e oitivas. A tela
  pede entidade `Pessoa` (nome, CPF, nascimento) ligada ao procedimento com o papel; `OitivaParte.nomeParte` deveria
  passar a apontar para `Pessoa` (senão a mesma pessoa vira vários registros). CPF mascarado na resposta da API.
- Implantação centralizada multi-tenant (docx 1.8): login pelo provedor de identidade da Polícia Civil (OIDC/LDAP;
  a API já é Resource Server, trocaria HS256 por RS256/JWKS), lotação (`Delegacia`) no SISGEP, `delegacia_id` com
  `@TenantId` do Hibernate (ou schema por delegacia) e unicidades incluindo a delegacia.
- Auditoria técnica com Hibernate Envers (`@Audited` + tabelas `_aud`/`revinfo`, com listener gravando o login do
  token): versões de todas as alterações, complementando os históricos de negócio (que guardam o motivo).

## Decisões registradas

- Implantação: instância local por delegacia com Docker Compose (sigilo/LGPD, soberania, funciona sem internet,
  custo e escopo), com evolução prevista para multi-tenant centralizado — seção 1.8 da documentação (RNF11, RNF13).

## Diagramas

Gerados por código em `docs/diagramas/fonte/` (Python gera HTML/SVG; Playwright/Chromium renderiza PNG e PDF).
Para alterar uma tabela/classe, editar `spec.py` (DER e classes), `uc.py` (casos de uso, inclusive ofícios), `arq.py` (arquitetura) ou `impl.py`
(implantação) e rodar, dentro dessa pasta: `python3 spec.py && python3 uc.py && python3 arq.py && python3 impl.py && node render.js` (com `playwright` disponível no `NODE_PATH`).
Os arquivos gerados (`*.html`, `*.json`, `*.png`, `*.pdf`) saem na própria pasta; copiar os finais para
`docs/diagramas/`.
