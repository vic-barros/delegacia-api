# Documentação Técnica da API Backend SISGEP

Esta documentação foi estruturada com base nos endpoints atualmente já implementados no backend e no contrato dos DTOs. Ela serve como referência oficial para o frontend consumir a API.

---

## Formato padrão de erro

A API usa `ErroResponseDto` em respostas de erro:

```json
{
  "status": 400,
  "mensagem": "Dados inválidos",
  "detalhes": [
    "nome: O nome deve ser informado"
  ],
  "dataHora": "2026-10-14T10:45:16.483Z"
}
```

### Códigos comuns

- 400 Bad Request: validação, payload inválido, parâmetro inválido
- 401 Unauthorized: credenciais inválidas / token ausente ou inválido
- 403 Forbidden: login permitido, mas sem permissão: o papel não acessa a rota (sem corpo, resposta do Spring Security) ou o usuário não é o dono do registro, por exemplo não é o detentor nem Delegado ao arquivar, ou não é o destinatário ao responder um repasse (com `ErroResponseDto` e `mensagem`)
- 404 Not Found: recurso inexistente
- 409 Conflict: conflito de regra de negócio ou confirmação necessária
- 415 Unsupported Media Type: body enviado em tipo não suportado (ex.: JSON ausente ou Content-Type incorreto)

---

# 1) Autenticação

## Endpoint: Login

### Método HTTP:

POST

### Rota:

`/auth/login`

### Descrição:

Autentica um usuário e retorna um JWT para uso nas rotas protegidas.

### Autenticação:

Público

### Parâmetros de Entrada (Request)

#### Body (Payload)

```json
{
  "login": "login",
  "senha": "senha"
}
```

Campos:

- `login`: string, obrigatório, deve ser informado
- `senha`: string, obrigatório, deve ser informada

#### Validações

- `login`: não pode estar em branco
- `senha`: não pode estar em branco

### Respostas

#### Sucesso (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9....",
  "tipo": "Bearer"
}
```

#### Erros comuns

- 401 Unauthorized: credenciais inválidas
- 400 Bad Request: payload inválido

---

## Endpoint: Usuário logado

### Método HTTP:

GET

### Rota:

`/auth/usuario-logado`

### Descrição:

Retorna os dados do usuário autenticado: o login vem do token e o restante (id, nome, matrícula e papel) do banco.

### Autenticação:

Requer JWT

### Parâmetros de Entrada

Nenhum

### Respostas

#### Sucesso (200 OK)

```json
{
  "id": 2,
  "login": "vbarros",
  "nome": "Victória Barros",
  "matricula": "2001",
  "role": "ROLE_DELEGADO"
}
```

#### Erros comuns

- 401 Unauthorized: token ausente ou inválido

---

# 2) Roles / Papéis

## Endpoint: Listar papéis disponíveis para cadastro

### Método HTTP:

GET

### Rota:

`/roles/cadastro`

### Descrição:

Lista os papéis permitidos para o cadastro público de usuários, excluindo o perfil admin.

### Autenticação:

Público

### Parâmetros de Entrada

Nenhum

### Respostas

#### Sucesso (200 OK)

```json
[
  {
    "id": 2,
    "acesso": "ROLE_DELEGADO",
    "descricao": "Delegado"
  },
  {
    "id": 3,
    "acesso": "ROLE_POLICIAL",
    "descricao": "Policial"
  },
  {
    "id": 4,
    "acesso": "ROLE_ESTAGIARIO",
    "descricao": "Estagiário"
  }
]
```

#### Erros comuns

- 500 Internal Server Error: falha interna

---

# 3) Usuários

## Endpoint: Cadastro público de usuário

### Método HTTP:

POST

### Rota:

`/usuarios/cadastro`

### Descrição:

Cria uma solicitação de cadastro com status `PENDENTE` e dispara notificação para os admins aprovadores.

### Autenticação:

Público

### Parâmetros de Entrada

#### Body (Payload)

```json
{
  "nome": "nome",
  "matricula": "12345",
  "login": "login",
  "senha": "12345678",
  "roleId": 2
}
```

Campos:

- `nome`: string, obrigatório, máximo 150 caracteres
- `matricula`: string, obrigatório, máximo 50 caracteres
- `login`: string, obrigatório, mínimo 3 e máximo 50 caracteres
- `senha`: string, obrigatório, mínimo 8 e máximo 72 caracteres
- `roleId`: number, obrigatório, ID do papel no banco

#### Validações

- nome obrigatório
- matrícula obrigatória
- login obrigatório
- senha mínima de 8 caracteres
- `roleId` obrigatório

### Respostas

#### Sucesso (201 Created)

```json
{
  "id": 8,
  "nome": "nome",
  "matricula": "12345",
  "login": "nome",
  "status": "PENDENTE",
  "role": "ROLE_DELEGADO",
  "motivoRejeicao": null
}
```

#### Erros comuns

- 400 Bad Request: dados inválidos
- 409 Conflict: login ou matrícula já cadastrados

---

## Endpoint: Listar usuários

### Método HTTP:

GET

### Rota:

`/usuarios`

### Descrição:

Lista usuários, com filtro opcional por status.

### Autenticação:

Requer JWT. Papel obrigatório: `ROLE_ADMIN`.

### Parâmetros de Entrada

#### Query Parameters

- `status` (opcional): enum `StatusUsuario`
  - `PENDENTE`
  - `APROVADO`
  - `REJEITADO`
  - `DESATIVADO`

Exemplo:

```http
GET /usuarios?status=PENDENTE
```

### Respostas

#### Sucesso (200 OK)

```json
[
  {
    "id": 5,
    "nome": "Maria",
    "matricula": "2024",
    "login": "maria",
    "status": "PENDENTE",
    "role": "ROLE_POLICIAL",
    "motivoRejeicao": null
  }
]
```

#### Erros comuns

- 401 Unauthorized: token inválido
- 403 Forbidden: usuário não é admin
- 400 Bad Request: `status` inválido

---

## Endpoint: Buscar usuário por ID

### Método HTTP:

GET

### Rota:

`/usuarios/{id}`

### Descrição:

Busca um usuário específico por id.

### Autenticação:

Requer JWT. Papel obrigatório: `ROLE_ADMIN`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

```json
{
  "id": 5,
  "nome": "nome",
  "matricula": "2024",
  "login": "login",
  "status": "PENDENTE",
  "role": "ROLE_POLICIAL",
  "motivoRejeicao": null
}
```

#### Erros comuns

- 404 Not Found: usuário não existe
- 401 Unauthorized: token inválido
- 403 Forbidden: sem permissão

---

## Endpoint: Aprovar usuário

### Método HTTP:

PATCH

### Rota:

`/usuarios/{id}/aprovar`

### Descrição:

Aprova um cadastro pendente e opcionalmente troca o papel do usuário.

### Autenticação:

Requer JWT. Papel obrigatório: `ROLE_ADMIN`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

Opcional:

```json
{
  "roleId": 3
}
```

Campos:

- `roleId`: number, opcional

> Se `roleId` vier nulo, o papel original do cadastro permanece.

### Respostas

#### Sucesso (200 OK)

```json
{
  "id": 5,
  "nome": "nome",
  "matricula": "2024",
  "login": "login",
  "status": "APROVADO",
  "role": "ROLE_POLICIAL",
  "motivoRejeicao": null
}
```

#### Erros comuns

- 404 Not Found: usuário inexistente
- 400 Bad Request: não pode aprovar um usuário fora do estado pendente
- 409 Conflict: confirmação necessária em regra específica

---

## Endpoint: Rejeitar usuário

### Método HTTP:

PATCH

### Rota:

`/usuarios/{id}/rejeitar`

### Descrição:

Rejeita um cadastro pendente e exige motivo.

### Autenticação:

Requer JWT. Papel obrigatório: `ROLE_ADMIN`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

```json
{
  "motivo": "Dados inconsistentes na matrícula"
}
```

Campos:

- `motivo`: string, obrigatório, máximo 500 caracteres

### Respostas

#### Sucesso (200 OK)

```json
{
  "id": 5,
  "nome": "nome",
  "matricula": "2024",
  "login": "login",
  "status": "REJEITADO",
  "role": "ROLE_POLICIAL",
  "motivoRejeicao": "Dados inconsistentes na matrícula"
}
```

#### Erros comuns

- 400 Bad Request: motivo inválido
- 404 Not Found: usuário inexistente

---

## Endpoint: Editar usuário

### Método HTTP:

PUT

### Rota:

`/usuarios/{id}`

### Descrição:

Edita nome, matrícula e papel do usuário. O login não é alterado.

### Autenticação:

Requer JWT. Papel obrigatório: `ROLE_ADMIN`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

```json
{
  "nome": "Maria Souza",
  "matricula": "2025",
  "roleId": 3
}
```

Campos:

- `nome`: string, obrigatório, máximo 150 caracteres
- `matricula`: string, obrigatório, máximo 50 caracteres
- `roleId`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

```json
{
  "id": 5,
  "nome": "Maria Souza",
  "matricula": "2025",
  "login": "maria",
  "status": "PENDENTE",
  "role": "ROLE_POLICIAL",
  "motivoRejeicao": null
}
```

#### Erros comuns

- 400 Bad Request: payload inválido
- 404 Not Found: usuário inexistente
- 401 Unauthorized: token inválido

---

## Endpoint: Desativar usuário

### Método HTTP:

PATCH

### Rota:

`/usuarios/{id}/desativar`

### Descrição:

Desativa um usuário autenticado ou um usuário específico, conforme a regra de negócio.

### Autenticação:

Requer JWT. Papel obrigatório: `ROLE_ADMIN`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

```json
{
  "id": 5,
  "nome": "nome",
  "matricula": "2024",
  "login": "login",
  "status": "DESATIVADO",
  "role": "ROLE_POLICIAL",
  "motivoRejeicao": null
}
```

#### Erros comuns

- 404 Not Found: usuário inexistente
- 401 Unauthorized: token inválido
- 403 Forbidden: sem permissão

---

## Endpoint: Reativar usuário

### Método HTTP:

PATCH

### Rota:

`/usuarios/{id}/reativar`

### Descrição:

Reativa um usuário que estava desativado.

### Autenticação:

Requer JWT. Papel obrigatório: `ROLE_ADMIN`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

```json
{
  "id": 5,
  "nome": "nome",
  "matricula": "2024",
  "login": "login",
  "status": "APROVADO",
  "role": "ROLE_POLICIAL",
  "motivoRejeicao": null
}
```

#### Erros comuns

- 404 Not Found: usuário inexistente
- 403 Forbidden: sem permissão

---

## Endpoint: Redefinir senha

### Método HTTP:

PATCH

### Rota:

`/usuarios/{id}/senha`

### Descrição:

Define uma nova senha para o usuário. A API retorna 204 No Content.

### Autenticação:

Requer JWT. Papel obrigatório: `ROLE_ADMIN`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

```json
{
  "novaSenha": "novasenha123"
}
```

Campos:

- `novaSenha`: string, obrigatório, mínimo 8 e máximo 72 caracteres

### Respostas

#### Sucesso (204 No Content)

Sem body

#### Erros comuns

- 400 Bad Request: payload inválido
- 404 Not Found: usuário inexistente
- 401 Unauthorized: token inválido

---

## Endpoint: Listar servidores operacionais

### Método HTTP:

GET

### Rota:

`/usuarios/operacionais`

### Descrição:

Lista os Delegados, Policiais e Estagiários com cadastro aprovado, só com `id`, `nome` e `role` (sem login, matrícula nem status). Usada nos seletores do front: destinatário do repasse, responsável da oitiva e filtro de detentor.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`. O Admin usa `GET /usuarios`.

### Parâmetros de Entrada

Nenhum

### Respostas

#### Sucesso (200 OK)

```json
[
  {
    "id": 5,
    "nome": "Ana Lima",
    "role": "ROLE_POLICIAL"
  },
  {
    "id": 3,
    "nome": "Carlos Souza",
    "role": "ROLE_POLICIAL"
  },
  {
    "id": 6,
    "nome": "Carolina Silva",
    "role": "ROLE_POLICIAL"
  }
]
```

#### Erros comuns

- 401 Unauthorized: token ausente ou inválido
- 403 Forbidden: Admin

---

## Endpoint: Alterar a própria senha

### Método HTTP:

PATCH

### Rota:

`/usuarios/me/senha`

### Descrição:

O usuário logado troca a própria senha, informando a atual. O usuário vem do token (não há id na rota). A senha nunca volta na resposta.

### Autenticação:

Requer JWT. Qualquer usuário autenticado, inclusive o Admin.

### Parâmetros de Entrada

#### Body (Payload)

```json
{
  "senhaAtual": "Demo@2026",
  "novaSenha": "NovaSenha@1"
}
```

#### Validações

- `senhaAtual`: obrigatória
- `novaSenha`: obrigatória, de 8 a 72 caracteres, diferente da atual

### Respostas

#### Sucesso (204 No Content)

Sem corpo.

#### Erros comuns

- 400 Bad Request: campos ausentes ou senha curta (`detalhes`), "A senha atual está incorreta" ou "A nova senha deve ser diferente da atual"
- 401 Unauthorized: token ausente ou inválido

Observação: trocar a senha não invalida os tokens já emitidos (eles valem até expirar, 8h).

---

# 4) Notificações

## Endpoint: Listar notificações

### Método HTTP:

GET

### Rota:

`/notificacoes`

### Descrição:

Lista as notificações do usuário logado, com filtro opcional por status lida/não lida.

### Autenticação:

Requer JWT

### Parâmetros de Entrada

#### Query Parameters

- `lida` (opcional): boolean
  - `true` para lidas
  - `false` para não lidas

Exemplo:

```http
GET /notificacoes?lida=false
```

### Respostas

#### Sucesso (200 OK)

```json
[
  {
    "id": 10,
    "tipo": "CADASTRO_PENDENTE",
    "mensagem": "Novo cadastro pendente de aprovação: Maria",
    "lida": false,
    "dataCriacao": "2026-10-14T10:00:00",
    "referenciaId": 5
  }
]
```

#### Erros comuns

- 401 Unauthorized: token inválido

---

## Endpoint: Contar notificações não lidas

### Método HTTP:

GET

### Rota:

`/notificacoes/nao-lidas/contagem`

### Descrição:

Retorna a quantidade de notificações não lidas do usuário autenticado.

### Autenticação:

Requer JWT

### Respostas

#### Sucesso (200 OK)

```json
{
  "quantidade": 3
}
```

#### Erros comuns

- 401 Unauthorized: token inválido

---

## Endpoint: Marcar notificação como lida

### Método HTTP:

PATCH

### Rota:

`/notificacoes/{id}/lida`

### Descrição:

Marca uma notificação específica como lida.

### Autenticação:

Requer JWT

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (204 No Content)

Sem body

#### Erros comuns

- 404 Not Found: notificação inexistente
- 401 Unauthorized: token inválido

---

## Endpoint: Marcar todas as notificações como lidas

### Método HTTP:

PATCH

### Rota:

`/notificacoes/lidas`

### Descrição:

Marca todas as notificações do usuário logado como lidas.

### Autenticação:

Requer JWT

### Respostas

#### Sucesso (204 No Content)

Sem body

#### Erros comuns

- 401 Unauthorized: token inválido

---

# 5) Procedimentos

## Endpoint: Cadastrar procedimento

### Método HTTP:

POST

### Rota:

`/procedimentos`

### Descrição:

Cria um novo procedimento, define status inicial `EM_ANDAMENTO` e registra o usuário detentor atual.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO` ou `ROLE_POLICIAL`.

### Parâmetros de Entrada

#### Body (Payload)

```json
{
  "tipoProcedimento": "BO",
  "numeroProcedimento": 22,
  "anoProcedimento": 2026,
  "crime": "Furto de carro",
  "dataAbertura": "2026-10-03"
}
```

Campos:

- `tipoProcedimento`: enum `TipoProcedimento`
  - `BO`
  - `IP`
  - `TCO`
  - `APF`
  - `AIAI`
  - `AAFAI`
  - `ROP`
- `numeroProcedimento`: number, obrigatório, positivo
- `anoProcedimento`: number, obrigatório, mínimo 1900
- `crime`: string, obrigatório, 3 a 150 caracteres
- `dataAbertura`: string `YYYY-MM-DD`, obrigatório

### Respostas

#### Sucesso (201 Created)

```json
{
  "id": 6,
  "tipoProcedimento": "BO",
  "numeroProcedimento": 22,
  "anoProcedimento": 2026,
  "crime": "Furto de carro",
  "dataAbertura": "2026-10-03",
  "dataRemessaFinal": null,
  "protocoloRemessaFinal": null,
  "statusProcedimento": "EM_ANDAMENTO",
  "detentorAtual": {
    "id": 2,
    "nome": "Delegado",
    "role": "ROLE_DELEGADO"
  }
}
```

#### Erros comuns

- 400 Bad Request: payload inválido
- 409 Conflict: procedimento duplicado (tipo + número + ano)
- 401 Unauthorized: token inválido
- 403 Forbidden: Estagiário ou Admin

---

## Endpoint: Listar procedimentos

### Método HTTP:

GET

### Rota:

`/procedimentos`

### Descrição:

Acervo de procedimentos com filtros, ordenação e paginação. A resposta traz as contagens de cada aba (com os mesmos filtros, menos a aba), se o procedimento tem repasse pendente e desde quando está com o detentor atual (`naPosseDesde`, início da custódia aberta).

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Query Parameters

- `aba`: `EM_ANDAMENTO` (padrão), `ARQUIVADOS`, `REMETIDOS` (remessa final registrada) ou `TODOS`
- `tipo`: enum `TipoProcedimento`, opcional e repetível (`?tipo=IP&tipo=APF`); sem nenhum = todos
- `ano`: number, opcional
- `detentorId`: number, opcional
- `texto`: opcional; só dígitos = número do procedimento, senão parte do crime
- `dataInicio` / `dataFim`: `YYYY-MM-DD`, opcionais (data de abertura, inclusive; os mesmos da exportação)
- `ordem`: `-abertura` (padrão, mais recente primeiro), `abertura`, `procedimento` (ano, tipo e número) ou `-procedimento`
- `pagina`: number, padrão 0
- `tamanho`: number, padrão 20, máximo 100

Exemplo: `/procedimentos?aba=EM_ANDAMENTO&tipo=IP&dataInicio=2026-01-01&ordem=procedimento&pagina=0&tamanho=20`

### Respostas

#### Sucesso (200 OK)

```json
{
  "itens": [
    {
      "id": 6,
      "tipoProcedimento": "IP",
      "numeroProcedimento": 68,
      "anoProcedimento": 2026,
      "crime": "Roubo de veículo",
      "dataAbertura": "2026-10-22",
      "statusProcedimento": "EM_ANDAMENTO",
      "remetido": false,
      "detentorAtual": {
        "id": 2,
        "nome": "Victória Barros",
        "role": "ROLE_DELEGADO"
      },
      "repassePendente": false,
      "naPosseDesde": "2026-10-22T10:00:00"
    },
    {
      "id": 7,
      "tipoProcedimento": "IP",
      "numeroProcedimento": 77,
      "anoProcedimento": 2026,
      "crime": "Roubo",
      "dataAbertura": "2026-10-16",
      "statusProcedimento": "EM_ANDAMENTO",
      "remetido": false,
      "detentorAtual": {
        "id": 4,
        "nome": "Marcos Andrade",
        "role": "ROLE_POLICIAL"
      },
      "repassePendente": true,
      "naPosseDesde": "2026-10-16T10:00:00"
    }
  ],
  "pagina": 0,
  "tamanho": 2,
  "totalItens": 6,
  "totalPaginas": 3,
  "contagens": {
    "emAndamento": 6,
    "arquivados": 2,
    "remetidos": 1,
    "todos": 8
  }
}
```

#### Erros comuns

- 400 Bad Request: página/tamanho fora do limite, "A data inicial não pode ser posterior à data final.", "Ordem inválida. Use abertura, -abertura, procedimento ou -procedimento" ou enum/data em formato inválido
- 403 Forbidden: Admin

---

## Endpoint: Detalhe do procedimento

### Método HTTP:

GET

### Rota:

`/procedimentos/{id}`

### Descrição:

Dados do procedimento e o repasse pendente, se houver (`null` quando não há).

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

```json
{
  "procedimento": {
    "id": 3,
    "tipoProcedimento": "IP",
    "numeroProcedimento": 45,
    "anoProcedimento": 2026,
    "crime": "Roubo majorado",
    "dataAbertura": "2026-09-28",
    "dataRemessaFinal": null,
    "protocoloRemessaFinal": null,
    "statusProcedimento": "EM_ANDAMENTO",
    "detentorAtual": {
      "id": 2,
      "nome": "Victória Barros",
      "role": "ROLE_DELEGADO"
    }
  },
  "repassePendente": null
}
```

Com repasse pendente:

```json
"repassePendente": {
  "id": 4,
  "destinatario": { "id": 2, "nome": "Victória Barros", "role": "ROLE_DELEGADO" },
  "dataSolicitacao": "2026-10-28T11:22:25"
}
```

#### Erros comuns

- 404 Not Found: "Procedimento não encontrado"
- 403 Forbidden: Admin

---

## Endpoint: Histórico de custódia

### Método HTTP:

GET

### Rota:

`/procedimentos/{id}/historico-custodia`

### Descrição:

Todas as posses do procedimento, da mais antiga para a atual. `dataFim` nula = posse atual; `origem` é `CADASTRO_INICIAL` ou `REPASSE` (com o id do repasse aceito).

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

```json
[
  {
    "id": 3,
    "usuario": {
      "id": 3,
      "nome": "Carlos Souza",
      "role": "ROLE_POLICIAL"
    },
    "origem": "CADASTRO_INICIAL",
    "repasseOrigemId": null,
    "dataInicio": "2026-09-28T10:00:00",
    "dataFim": "2026-10-08T11:00:00"
  },
  {
    "id": 14,
    "usuario": {
      "id": 2,
      "nome": "Victória Barros",
      "role": "ROLE_DELEGADO"
    },
    "origem": "REPASSE",
    "repasseOrigemId": 1,
    "dataInicio": "2026-10-08T11:00:00",
    "dataFim": null
  }
]
```

#### Erros comuns

- 404 Not Found: "Procedimento não encontrado"
- 403 Forbidden: Admin

---

## Endpoint: Histórico de status

### Método HTTP:

GET

### Rota:

`/procedimentos/{id}/historico-status`

### Descrição:

Transições de status do procedimento (cadastro, arquivamento e reabertura), com motivo e responsável. `statusAnterior` nulo = registro do cadastro.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

```json
[
  {
    "id": 3,
    "statusAnterior": null,
    "statusNovo": "EM_ANDAMENTO",
    "motivo": "Cadastro do procedimento",
    "responsavel": {
      "id": 3,
      "nome": "Carlos Souza",
      "role": "ROLE_POLICIAL"
    },
    "dataTransicao": "2026-09-28T10:00:00"
  }
]
```

#### Erros comuns

- 404 Not Found: "Procedimento não encontrado"
- 403 Forbidden: Admin

---

## Endpoint: Arquivar procedimento

### Método HTTP:

PATCH

### Rota:

`/procedimentos/{id}/arquivar`

### Descrição:

Arquiva o procedimento com motivo e registra a transição no histórico de status. O Delegado arquiva qualquer procedimento; o Policial, só o que detém.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO` ou `ROLE_POLICIAL`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

```json
{
  "motivo": "Relatório final remetido ao Judiciário"
}
```

- `motivo`: obrigatório, de 3 a 500 caracteres

### Respostas

#### Sucesso (200 OK)

O procedimento atualizado, no mesmo formato de "Cadastrar procedimento":

```json
{
  "id": 5,
  "tipoProcedimento": "IP",
  "numeroProcedimento": 30,
  "anoProcedimento": 2026,
  "crime": "Ameaça",
  "dataAbertura": "2026-07-30",
  "dataRemessaFinal": "2026-10-28",
  "protocoloRemessaFinal": "PROT-123/2026",
  "statusProcedimento": "ARQUIVADO",
  "detentorAtual": {
    "id": 2,
    "nome": "Victória Barros",
    "role": "ROLE_DELEGADO"
  }
}
```

#### Erros comuns

- 400 Bad Request: motivo inválido, "O procedimento já está arquivado", "Não é possível arquivar o procedimento enquanto houver repasses pendentes" ou "Não é possível arquivar o procedimento enquanto houver oitivas agendadas (datas). Conclua as já realizadas e desmarque as demais"
- 403 Forbidden: "Apenas o detentor atual do procedimento ou um Delegado podem arquivar esse procedimento"; Estagiário ou Admin
- 404 Not Found: "Procedimento não encontrado"

---

## Endpoint: Reabrir procedimento

### Método HTTP:

PATCH

### Rota:

`/procedimentos/{id}/reabrir`

### Descrição:

Volta um procedimento arquivado para `EM_ANDAMENTO`, com motivo (ex.: cota judicial ou ministerial), e registra no histórico de status. Delegado ou detentor atual.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO` ou `ROLE_POLICIAL`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

```json
{
  "motivo": "Cota ministerial: novas diligências"
}
```

- `motivo`: obrigatório, de 3 a 500 caracteres

### Respostas

#### Sucesso (200 OK)

O procedimento atualizado, no mesmo formato de "Cadastrar procedimento":

```json
{
  "id": 5,
  "tipoProcedimento": "IP",
  "numeroProcedimento": 30,
  "anoProcedimento": 2026,
  "crime": "Ameaça",
  "dataAbertura": "2026-07-30",
  "dataRemessaFinal": "2026-10-28",
  "protocoloRemessaFinal": "PROT-123/2026",
  "statusProcedimento": "EM_ANDAMENTO",
  "detentorAtual": {
    "id": 2,
    "nome": "Victória Barros",
    "role": "ROLE_DELEGADO"
  }
}
```

#### Erros comuns

- 400 Bad Request: motivo inválido ou "Não é possível reabrir um procedimento que não está arquivado"
- 403 Forbidden: "Apenas o detentor atual do procedimento ou um Delegado podem reabrir esse procedimento"; Estagiário ou Admin
- 404 Not Found: "Procedimento não encontrado"

---

## Endpoint: Registrar remessa final

### Método HTTP:

PATCH

### Rota:

`/procedimentos/{id}/registrar-remessa`

### Descrição:

Registra a data e o protocolo da remessa final (o procedimento passa a aparecer na aba Remetidos). Se já houver remessa registrada, pede confirmação para substituir (`409` com `confirmarSobrescrita`). Toda mudança fica no histórico de remessa. Delegado ou detentor atual.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO` ou `ROLE_POLICIAL`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

```json
{
  "data": "2026-10-28",
  "numeroProtocolo": "PROT-123/2026",
  "confirmarSobrescrita": false
}
```

- `data`: obrigatória, `YYYY-MM-DD`, não pode ser futura nem anterior à abertura
- `numeroProtocolo`: obrigatório, de 3 a 255 caracteres
- `confirmarSobrescrita`: opcional; `true` para substituir uma remessa já registrada

### Respostas

#### Sucesso (200 OK)

O procedimento atualizado, no mesmo formato de "Cadastrar procedimento":

```json
{
  "id": 5,
  "tipoProcedimento": "IP",
  "numeroProcedimento": 30,
  "anoProcedimento": 2026,
  "crime": "Ameaça",
  "dataAbertura": "2026-07-30",
  "dataRemessaFinal": "2026-10-28",
  "protocoloRemessaFinal": "PROT-123/2026",
  "statusProcedimento": "EM_ANDAMENTO",
  "detentorAtual": {
    "id": 2,
    "nome": "Victória Barros",
    "role": "ROLE_DELEGADO"
  }
}
```

#### Erros comuns

- 400 Bad Request: validação, "Não é possível definir uma data de remessa anterior à data de abertura do procedimento." ou "Não é possível registrar uma remessa para um procedimento arquivado."
- 403 Forbidden: "Apenas o detentor atual do procedimento ou um Delegado podem registrar a remessa final desse procedimento"; Estagiário ou Admin
- 404 Not Found: "Procedimento não encontrado"
- 409 Conflict: confirmação necessária (reenviar com `confirmarSobrescrita: true`):

```json
{
  "status": 409,
  "mensagem": "Já existe uma remessa registrada. Confirme que deseja sobrescrever antes de prosseguir.",
  "detalhes": [
    "confirmarSobrescrita"
  ],
  "dataHora": "2026-10-28T13:21:53.25137377"
}
```

- 409 Conflict: "Já existe um registro de remessa com a mesma data e número de protocolo."

---

## Endpoint: Histórico de remessa

### Método HTTP:

GET

### Rota:

`/procedimentos/{id}/historico-remessa`

### Descrição:

Registros e substituições da remessa final, com os valores anteriores e novos, data do registro e responsável.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

```json
[
  {
    "id": 1,
    "dataRemessaAnterior": null,
    "protocoloAnterior": null,
    "dataRemessaNova": "2026-10-28",
    "protocoloNovo": "PROT-123/2026",
    "dataRegistro": "2026-10-28T13:22:26.507383",
    "responsavel": {
      "id": 2,
      "nome": "Victória Barros",
      "role": "ROLE_DELEGADO"
    }
  },
  {
    "id": 2,
    "dataRemessaAnterior": "2026-10-28",
    "protocoloAnterior": "PROT-123/2026",
    "dataRemessaNova": "2026-10-28",
    "protocoloNovo": "PROT-124/2026",
    "dataRegistro": "2026-10-28T13:22:26.556082",
    "responsavel": {
      "id": 2,
      "nome": "Victória Barros",
      "role": "ROLE_DELEGADO"
    }
  }
]
```

#### Erros comuns

- 404 Not Found: "Procedimento não encontrado"
- 403 Forbidden: Admin

---

## Endpoint: Exportar procedimentos

### Método HTTP:

GET

### Rota:

`/procedimentos/exportar`

### Descrição:

Gera o relatório dos procedimentos cadastrados no sistema em XLSX (planilha com filtro e cabeçalho fixo) ou PDF (A4 paisagem, com os filtros usados, quem gerou e quando). A resposta é o arquivo, não JSON.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO` ou `ROLE_POLICIAL`.

### Parâmetros de Entrada

#### Query Parameters

- `formato`: enum `FormatoExportacao` (`XLSX` ou `PDF`), obrigatório
- `dataInicio`: string `YYYY-MM-DD`, opcional (data de abertura a partir de)
- `dataFim`: string `YYYY-MM-DD`, opcional (data de abertura até, inclusive)
- `tipo`: enum `TipoProcedimento`, opcional e repetível (`?tipo=IP&tipo=APF`); sem nenhum = todos
- `status`: enum `StatusProcedimento` (`EM_ANDAMENTO` ou `ARQUIVADO`), opcional

Exemplo: `/procedimentos/exportar?formato=PDF&tipo=IP&tipo=APF&status=EM_ANDAMENTO`

Os filtros `dataInicio`, `dataFim` e `tipo` são os mesmos da listagem (`GET /procedimentos`): com os mesmos valores, o arquivo traz o mesmo conjunto.

### Respostas

#### Sucesso (200 OK)

Corpo binário (o arquivo). Cabeçalhos:

```http
Content-Type: application/pdf
Content-Disposition: attachment; filename="procedimentos-20261020-1430.pdf"
```

- XLSX: `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`, arquivo `.xlsx`
- O CORS expõe `Content-Disposition`, então o front consegue ler o nome do arquivo (no `fetch`, usar `response.blob()` e o nome do cabeçalho para disparar o download)

#### Erros comuns

- 400 Bad Request: `formato` ausente ("Parâmetro obrigatório ausente: 'formato'"), valor de enum inválido ou data inicial posterior à final
- 404 Not Found: "Nenhum procedimento encontrado com os filtros informados." (lista vazia não gera arquivo)
- 403 Forbidden: Estagiário ou Admin

---

# 5.1) Repasses

## Endpoint: Solicitar repasse de procedimento

### Método HTTP:

POST

### Rota:

`/repasses`

### Descrição:

O detentor atual de um procedimento solicita o repasse da custódia para outro servidor. O repasse nasce `PENDENTE` e a custódia só muda quando o destinatário aceitar. O destinatário recebe a notificação `REPASSE_RECEBIDO`.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Body (Payload)

```json
{
  "idDestinatario": 3,
  "idProcedimento": 5,
  "observacao": "Falta ouvir a testemunha, segue para você conduzir."
}
```

Campos:

- `idDestinatario`: number, obrigatório (Delegado, Policial ou Estagiário com cadastro aprovado)
- `idProcedimento`: number, obrigatório
- `observacao`: string, opcional, até 500 caracteres (recado para o destinatário; em branco vira `null`)

### Respostas

#### Sucesso (201 Created)

```json
{
  "id": 6,
  "statusRepasse": "PENDENTE",
  "observacao": "Falta ouvir a testemunha, segue para você conduzir.",
  "justificativaRecusa": null,
  "dataSolicitacao": "2026-10-28T13:21:53.478668",
  "dataResposta": null,
  "procedimento": {
    "id": 5,
    "tipoProcedimento": "IP",
    "numeroProcedimento": 30,
    "anoProcedimento": 2026,
    "crime": "Ameaça",
    "dataAbertura": "2026-07-30",
    "dataRemessaFinal": "2026-10-28",
    "protocoloRemessaFinal": "PROT-123/2026",
    "statusProcedimento": "EM_ANDAMENTO",
    "detentorAtual": {
      "id": 2,
      "nome": "Victória Barros",
      "role": "ROLE_DELEGADO"
    }
  },
  "solicitante": {
    "id": 2,
    "nome": "Victória Barros",
    "matricula": "2001",
    "login": "vbarros",
    "status": "APROVADO",
    "role": "ROLE_DELEGADO",
    "motivoRejeicao": null
  },
  "destinatario": {
    "id": 3,
    "nome": "Carlos Souza",
    "matricula": "2002",
    "login": "csouza",
    "status": "APROVADO",
    "role": "ROLE_POLICIAL",
    "motivoRejeicao": null
  }
}
```

#### Erros comuns

- 400 Bad Request:
  - campos ausentes
  - observação com mais de 500 caracteres
  - "Não é possível repassar um procedimento para si próprio."
  - "O destinatário deve ser Delegado, Policial ou Estagiário com cadastro aprovado"
  - "Não é possível solicitar um repasse para um procedimento arquivado."
  - "Já existe um repasse pendente para esse procedimento."
- 404 Not Found: "Destinatário não encontrado." ou "Procedimento não encontrado."
- 403 Forbidden: "O procedimento informado não pertence ao solicitante." (só o detentor atual pede o repasse); Admin

---

## Endpoint: Listar repasses

### Método HTTP:

GET

### Rota:

`/repasses`

### Descrição:

Tela de repasses: só os repasses em que o usuário logado é destinatário ou solicitante (vem do token). A resposta traz os números das abas "Recebidos" e "Enviados" (pendentes).

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Query Parameters

- `aba`: `RECEBIDOS` (padrão; pendentes em que sou o destinatário), `ENVIADOS` (pendentes que eu pedi) ou `HISTORICO` (aceitos e recusados, enviados e recebidos)
- `texto`: opcional; só dígitos = número do procedimento, senão parte do crime ou do nome de um dos servidores
- `pagina`: number, padrão 0
- `tamanho`: number, padrão 20, máximo 100

Ordem: mais recentes primeiro (`dataSolicitacao`; no histórico, `dataResposta`).

### Respostas

#### Sucesso (200 OK)

```json
{
  "itens": [
    {
      "id": 4,
      "statusRepasse": "PENDENTE",
      "procedimentoId": 7,
      "tipoProcedimento": "IP",
      "numeroProcedimento": 77,
      "anoProcedimento": 2026,
      "crime": "Roubo",
      "solicitante": {
        "id": 4,
        "nome": "Marcos Andrade",
        "role": "ROLE_POLICIAL"
      },
      "destinatario": {
        "id": 2,
        "nome": "Victória Barros",
        "role": "ROLE_DELEGADO"
      },
      "observacao": "Falta ouvir a testemunha, segue para você conduzir.",
      "justificativaRecusa": null,
      "dataSolicitacao": "2026-10-28T11:21:51.133623",
      "dataResposta": null
    }
  ],
  "pagina": 0,
  "tamanho": 1,
  "totalItens": 2,
  "totalPaginas": 2,
  "recebidosPendentes": 2,
  "enviadosPendentes": 1
}
```

#### Erros comuns

- 400 Bad Request: página/tamanho fora do limite ou aba inválida
- 403 Forbidden: Admin

---

## Endpoint: Aceitar repasse

### Método HTTP:

PATCH

### Rota:

`/repasses/{id}/aceitar`

### Descrição:

O destinatário aceita o repasse. Na mesma transação: o repasse vira `ACEITO`, a custódia anterior é encerrada, abre-se a nova custódia (origem `REPASSE`), o destinatário vira o detentor, o aviso `REPASSE_RECEBIDO` é marcado como lido e o solicitante recebe `REPASSE_ACEITO`.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório (id do repasse)

Sem body.

### Respostas

#### Sucesso (200 OK)

Mesmo formato da resposta de "Solicitar repasse", com o status, a `dataResposta` e o procedimento já atualizados:

```json
{
  "id": 5,
  "statusRepasse": "ACEITO",
  "observacao": null,
  "justificativaRecusa": null,
  "dataSolicitacao": "2026-10-27T13:21:51.133623",
  "dataResposta": "2026-10-28T13:21:52.844750003",
  "procedimento": {
    "id": 8,
    "tipoProcedimento": "BO",
    "numeroProcedimento": 43,
    "anoProcedimento": 2026,
    "crime": "Furto",
    "dataAbertura": "2026-10-13",
    "dataRemessaFinal": null,
    "protocoloRemessaFinal": null,
    "statusProcedimento": "EM_ANDAMENTO",
    "detentorAtual": {
      "id": 2,
      "nome": "Victória Barros",
      "role": "ROLE_DELEGADO"
    }
  },
  "solicitante": {
    "id": 6,
    "nome": "Carolina Silva",
    "matricula": "2005",
    "login": "csilva",
    "status": "APROVADO",
    "role": "ROLE_POLICIAL",
    "motivoRejeicao": null
  },
  "destinatario": {
    "id": 2,
    "nome": "Victória Barros",
    "matricula": "2001",
    "login": "vbarros",
    "status": "APROVADO",
    "role": "ROLE_DELEGADO",
    "motivoRejeicao": null
  }
}
```

#### Erros comuns

- 403 Forbidden: "Você não é o destinatário dessa solicitação de repasse."; Admin
- 404 Not Found: "Solicitação de repasse não encontrada."
- 409 Conflict: "O repasse já foi respondido. Status atual: ACEITO" (ou `RECUSADO`)

---

## Endpoint: Recusar repasse

### Método HTTP:

PATCH

### Rota:

`/repasses/{id}/recusar`

### Descrição:

O destinatário recusa o repasse com justificativa. A custódia continua com o solicitante, que recebe `REPASSE_RECUSADO` com o motivo.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório (id do repasse)

#### Body (Payload)

```json
{
  "justificativa": "Estou de plantão esta semana"
}
```

- `justificativa`: obrigatória, de 3 a 500 caracteres

### Respostas

#### Sucesso (200 OK)

Mesmo formato da resposta de "Solicitar repasse", com o status, a `dataResposta` e o procedimento já atualizados:

```json
{
  "id": 4,
  "statusRepasse": "RECUSADO",
  "observacao": "Falta ouvir a testemunha, segue para você conduzir.",
  "justificativaRecusa": "Estou de plantão esta semana",
  "dataSolicitacao": "2026-10-28T11:21:51.133623",
  "dataResposta": "2026-10-28T13:21:53.042323826",
  "procedimento": {
    "id": 7,
    "tipoProcedimento": "IP",
    "numeroProcedimento": 77,
    "anoProcedimento": 2026,
    "crime": "Roubo",
    "dataAbertura": "2026-10-16",
    "dataRemessaFinal": null,
    "protocoloRemessaFinal": null,
    "statusProcedimento": "EM_ANDAMENTO",
    "detentorAtual": {
      "id": 4,
      "nome": "Marcos Andrade",
      "role": "ROLE_POLICIAL"
    }
  },
  "solicitante": {
    "id": 4,
    "nome": "Marcos Andrade",
    "matricula": "2003",
    "login": "mandrade",
    "status": "APROVADO",
    "role": "ROLE_POLICIAL",
    "motivoRejeicao": null
  },
  "destinatario": {
    "id": 2,
    "nome": "Victória Barros",
    "matricula": "2001",
    "login": "vbarros",
    "status": "APROVADO",
    "role": "ROLE_DELEGADO",
    "motivoRejeicao": null
  }
}
```

#### Erros comuns

- 400 Bad Request: justificativa ausente ou fora do tamanho
- 403 Forbidden: "Você não é o destinatário dessa solicitação de repasse."; Admin
- 404 Not Found: "Solicitação de repasse não encontrada."
- 409 Conflict: "O repasse já foi respondido. Status atual: ..."

---

# 5.5) Oitivas

## Endpoint: Agendar oitiva

### Método HTTP:

POST

### Rota:

`/oitivas`

### Descrição:

Agenda uma nova oitiva para um procedimento, definindo o responsável e as partes envolvidas.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` e `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Body (Payload)

```json
{
  "procedimentoId": 6,
  "responsavelId": 2,
  "dataHora": "2026-10-21T14:30:00",
  "partes": [
    {
      "tipoParte": "INVESTIGADO",
      "nomeParte": "João da Silva",
      "pessoaId": 1
    },
    {
      "tipoParte": "TESTEMUNHA",
      "nomeParte": "Maria Souza"
    }
  ],
  "confirmarConflito": false
}
```

Campos:

- `procedimentoId`: number, obrigatório
- `responsavelId`: number, obrigatório
- `dataHora`: string `yyyy-MM-ddTHH:mm:ss`, obrigatório, deve estar no futuro
- `partes`: lista obrigatória com ao menos 1 item
  - `tipoParte`: enum `TipoParte`
    - `INVESTIGADO`
    - `TESTEMUNHA`
    - `VITIMA`
  - `nomeParte`: string, obrigatório, até 150 caracteres
  - `pessoaId`: number, opcional. Id de uma pessoa já cadastrada (`GET /pessoas?nome=`); liga a parte à pessoa
    (vínculos). Sem ele, a parte fica só com o nome, como antes. Pessoa inexistente: 404 "Pessoa não encontrada"
- `confirmarConflito`: boolean, opcional, usado quando já existe outra oitiva do mesmo responsável no mesmo horário

### Respostas

#### Sucesso (201 Created)

```json
{
  "id": 12,
  "dataHora": "2026-10-21T14:30:00",
  "status": "AGENDADA",
  "atrasada": false,
  "procedimentoId": 6,
  "procedimento": "BO 22/2026",
  "responsavelId": 2,
  "responsavel": "Delegado",
  "partes": [
    {
      "id": 20,
      "tipoParte": "INVESTIGADO",
      "nomeParte": "João da Silva",
      "pessoaId": 1
    },
    {
      "id": 21,
      "tipoParte": "TESTEMUNHA",
      "nomeParte": "Maria Souza",
      "pessoaId": null
    }
  ],
  "motivoCancelamento": null,
  "cadastradoPor": "Administrador",
  "alteradoPor": null,
  "dataCadastro": "2026-10-15T09:00:00"
}
```

#### Erros comuns

- 400 Bad Request: payload inválido
- 401 Unauthorized: token ausente ou inválido
- 403 Forbidden: usuário sem papel permitido para `/oitivas/**`
- 409 Conflict: conflito de horário com outra oitiva do mesmo responsável e sem confirmação de conflito

---

## Endpoint: Listar oitivas

### Método HTTP:

GET

### Rota:

`/oitivas`

### Descrição:

Lista oitivas com filtros opcionais por período, responsável, procedimento, status e filtro de minhas oitivas/atrasadas.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` e `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Query Parameters

- `dataInicio`: `yyyy-MM-dd`, opcional
- `dataFim`: `yyyy-MM-dd`, opcional
- `responsavelId`: number, opcional
- `status`: enum `StatusOitiva`, opcional
  - `AGENDADA`
  - `CONCLUIDA`
  - `DESMARCADA`
- `procedimentoId`: number, opcional
- `minhas`: boolean, opcional, padrão `false`
- `atrasadas`: boolean, opcional, padrão `false`

Exemplo:

```http
GET /oitivas?status=AGENDADA&procedimentoId=6&minhas=true
```

### Respostas

#### Sucesso (200 OK)

```json
[
  {
    "id": 12,
    "dataHora": "2026-10-21T14:30:00",
    "status": "AGENDADA",
    "atrasada": false,
    "procedimentoId": 6,
    "procedimento": "BO 22/2026",
    "responsavelId": 2,
    "responsavel": "Delegado",
    "partes": [
      {
        "tipoParte": "INVESTIGADO",
        "nomeParte": "João da Silva"
      }
    ],
    "motivoCancelamento": null,
    "cadastradoPor": "Administrador",
    "alteradoPor": null,
    "dataCadastro": "2026-10-15T09:00:00"
  }
]
```

#### Erros comuns

- 401 Unauthorized: token ausente ou inválido
- 403 Forbidden: sem permissão

---

## Endpoint: Buscar oitiva por ID

### Método HTTP:

GET

### Rota:

`/oitivas/{id}`

### Descrição:

Busca uma oitiva específica pelo identificador.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` e `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

```json
{
  "id": 12,
  "dataHora": "2026-10-21T14:30:00",
  "status": "AGENDADA",
  "atrasada": false,
  "procedimentoId": 6,
  "procedimento": "BO 22/2026",
  "responsavelId": 2,
  "responsavel": "Delegado",
  "partes": [
    {
      "tipoParte": "INVESTIGADO",
      "nomeParte": "João da Silva"
    }
  ],
  "motivoCancelamento": null,
  "cadastradoPor": "Administrador",
  "alteradoPor": null,
  "dataCadastro": "2026-10-15T09:00:00"
}
```

#### Erros comuns

- 404 Not Found: oitiva não encontrada
- 401 Unauthorized: token inválido
- 403 Forbidden: sem permissão

---

## Endpoint: Editar oitiva

### Método HTTP:

PUT

### Rota:

`/oitivas/{id}`

### Descrição:

Edita o responsável e as partes da oitiva. A data/hora da oitiva não é alterada neste endpoint; para isso, use `/remarcar`.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` e `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

```json
{
  "responsavelId": 3,
  "partes": [
    {
      "tipoParte": "VITIMA",
      "nomeParte": "Ana Pereira",
      "pessoaId": 3
    }
  ],
  "confirmarConflito": false
}
```

Campos:

- `responsavelId`: number, obrigatório
- `partes`: lista obrigatória com ao menos 1 item (substitui a lista anterior; cada parte com `tipoParte`, `nomeParte` e `pessoaId` opcional, como no agendamento)
- `confirmarConflito`: boolean, opcional

### Respostas

#### Sucesso (200 OK)

Retorna o objeto da oitiva atualizado.

#### Erros comuns

- 400 Bad Request: payload inválido
- 404 Not Found: oitiva inexistente
- 409 Conflict: conflito de horário com outra oitiva do mesmo responsável
- 401 Unauthorized: token ausente ou inválido
- 403 Forbidden: sem permissão

---

## Endpoint: Remarcar oitiva

### Método HTTP:

PATCH

### Rota:

`/oitivas/{id}/remarcar`

### Descrição:

Altera apenas a data/hora da oitiva, mantendo o status `AGENDADA`.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` e `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

```json
{
  "dataHora": "2026-10-21T16:00:00",
  "confirmarConflito": false
}
```

Campos:

- `dataHora`: string `yyyy-MM-ddTHH:mm:ss`, obrigatório, deve estar no futuro
- `confirmarConflito`: boolean, opcional

### Respostas

#### Sucesso (200 OK)

Retorna a oitiva com nova data/hora.

#### Erros comuns

- 400 Bad Request: payload inválido
- 404 Not Found: oitiva inexistente
- 409 Conflict: conflito de horário e confirmação não informada
- 401 Unauthorized: token inválido
- 403 Forbidden: sem permissão

---

## Endpoint: Desmarcar oitiva

### Método HTTP:

PATCH

### Rota:

`/oitivas/{id}/desmarcar`

### Descrição:

Cancela uma oitiva agendada e exige motivo explícito.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` e `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

```json
{
  "motivo": "Conflito de agenda com a autoridade responsável"
}
```

Campos:

- `motivo`: string, obrigatório, com no máximo 500 caracteres

### Respostas

#### Sucesso (200 OK)

Retorna a oitiva com status `DESMARCADA`.

#### Erros comuns

- 400 Bad Request: motivo ausente ou inválido
- 404 Not Found: oitiva inexistente
- 401 Unauthorized: token inválido
- 403 Forbidden: sem permissão

---

## Endpoint: Concluir oitiva

### Método HTTP:

PATCH

### Rota:

`/oitivas/{id}/concluir`

### Descrição:

Marca a oitiva como concluída. Se a data agendada ainda estiver no futuro, a API ajusta a data para o momento da conclusão.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` e `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

Retorna a oitiva com status `CONCLUIDA`.

#### Erros comuns

- 404 Not Found: oitiva inexistente
- 401 Unauthorized: token inválido
- 403 Forbidden: sem permissão

---

# 5.6) Pessoas

Pessoas envolvidas nos procedimentos (vítima, testemunha, investigado), base do módulo de vínculos. A parte da
oitiva pode ser ligada a uma pessoa pelo `pessoaId` (ver "Agendar oitiva"). LGPD: o CPF nunca sai completo
(`***.982.247-**`); telefone e endereço só aparecem na ficha (`GET /pessoas/{id}`), não na busca.

Autenticação de todas as rotas: requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou
`ROLE_ESTAGIARIO` (Admin recebe 403).

## Endpoint: Cadastrar pessoa

### Método HTTP:

POST

### Rota:

`/pessoas`

### Descrição:

Cadastra uma pessoa. O usuário logado fica registrado como quem cadastrou.

### Parâmetros de Entrada

#### Body (Payload)

```json
{
  "nome": "João Santos",
  "dataNascimento": "1990-05-10",
  "cpf": "529.982.247-25",
  "alcunha": "João Grande",
  "telefone": "(79) 99999-0000",
  "endereco": "Rua Fictícia, 10, Centro, Aracaju/SE"
}
```

Campos:

- `nome`: string, obrigatório, até 150 caracteres
- `dataNascimento`: string `YYYY-MM-DD`, opcional, no passado
- `cpf`: string, opcional, com ou sem pontuação; os dígitos verificadores são conferidos; único entre as pessoas
- `alcunha`: string, opcional, até 100 caracteres (também entra na busca por nome)
- `telefone`: string, opcional, até 20 caracteres (números, espaços, parênteses, `+` e `-`)
- `endereco`: string, opcional, até 300 caracteres

Campos opcionais enviados vazios (`""`) são gravados como nulos.

### Respostas

#### Sucesso (201 Created)

```json
{
  "id": 1,
  "nome": "João Santos",
  "dataNascimento": "1990-05-10",
  "idade": 36,
  "cpf": "***.982.247-**",
  "alcunha": "João Grande",
  "telefone": "(79) 99999-0000",
  "endereco": "Rua Fictícia, 10, Centro, Aracaju/SE"
}
```

`idade` é calculada a partir da data de nascimento (nula se a data não foi informada).

#### Erros comuns

- 400 Bad Request: nome ausente, "CPF inválido", data de nascimento no futuro, telefone inválido
- 409 Conflict: "Já existe uma pessoa cadastrada com este CPF"

---

## Endpoint: Buscar pessoas

### Método HTTP:

GET

### Rota:

`/pessoas`

### Descrição:

Busca para o autocompletar da oitiva e a tela Pessoas, em ordem alfabética. Devolve o resumo da pessoa (sem
telefone e endereço).

### Parâmetros de Entrada

#### Query Parameters

- `nome`: string, opcional. Parte do nome **ou da alcunha**, sem diferenciar maiúsculas (`?nome=gale` acha "Galego")
- `cpf`: string, opcional, com ou sem pontuação (busca exata)

Sem filtros, lista todas.

### Respostas

#### Sucesso (200 OK)

```json
[
  {
    "id": 1,
    "nome": "João Santos",
    "alcunha": "João Grande",
    "idade": 36,
    "cpf": "***.982.247-**"
  }
]
```

---

## Endpoint: Buscar pessoa por ID (ficha)

### Método HTTP:

GET

### Rota:

`/pessoas/{id}`

### Descrição:

Ficha completa da pessoa (mesmo formato da resposta do cadastro, com telefone e endereço).

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

Mesmo formato da resposta de "Cadastrar pessoa".

#### Erros comuns

- 404 Not Found: "Pessoa não encontrada"

---

## Endpoint: Editar pessoa

### Método HTTP:

PUT

### Rota:

`/pessoas/{id}`

### Descrição:

Atualiza os dados da pessoa. **Enviar todos os campos**: o que vier nulo ou vazio fica nulo (o PUT substitui o
cadastro), **menos o CPF**: como a ficha devolve o CPF mascarado, `cpf` ausente ou vazio mantém o CPF gravado. Para
trocar, enviar o CPF novo. A própria pessoa pode manter o CPF dela.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

Mesmo formato e regras de "Cadastrar pessoa".

### Respostas

#### Sucesso (200 OK)

Mesmo formato da resposta de "Cadastrar pessoa".

#### Erros comuns

- 400 Bad Request: mesmas validações do cadastro
- 404 Not Found: "Pessoa não encontrada"
- 409 Conflict: "Já existe uma pessoa cadastrada com este CPF" (CPF de outra pessoa)

---

## Endpoint: Participações da pessoa (ligações)

### Método HTTP:

GET

### Rota:

`/pessoas/{id}/participacoes`

### Descrição:

Em quais procedimentos e oitivas a pessoa aparece, com o papel. Alimenta o quadro "Ligações" e o primeiro nível
do grafo de vínculos. Os veículos não vêm aqui: eles estão ligados ao procedimento, então o próximo nível do grafo
usa `GET /veiculos?procedimentoId={procedimentoId}`. Ordem: oitiva mais recente primeiro (o mesmo procedimento
pode aparecer mais de uma vez, uma por oitiva).

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

```json
[
  {
    "procedimentoId": 1,
    "procedimento": "IP 45/2026",
    "crime": "Roubo majorado",
    "tipoParte": "TESTEMUNHA",
    "oitivaId": 1,
    "dataHoraOitiva": "2026-10-23T09:00:00",
    "statusOitiva": "AGENDADA"
  }
]
```

#### Erros comuns

- 404 Not Found: "Pessoa não encontrada"

---

# 6) Veículos

## Endpoint: Cadastrar veículo

### Método HTTP:

POST

### Rota:

`/veiculos`

### Descrição:

Cadastra um veículo associado a um procedimento. O lacre é gerado no backend e a situação inicial é `NA_DEPOL`.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO` ou `ROLE_POLICIAL`.

### Parâmetros de Entrada

#### Body (Payload)

```json
{
  "tipoVeiculo": "Carro",
  "marca": "Toyota",
  "modelo": "Corolla",
  "cor": "Prata",
  "placa": "ABC1D23",
  "chassi": "1234567890",
  "motor": "2.0",
  "caracteristicasVisuais": "Vidro fumê",
  "observacoes": "Sem observações",
  "statusPericia": "SEM_PERICIA",
  "procedimentoId": 6,
  "confirmarDuplicidade": true
}
```

Campos:

- `tipoVeiculo`: string, obrigatório, máximo 50 caracteres
- `marca`: string, obrigatório, máximo 50 caracteres
- `modelo`: string, obrigatório, máximo 80 caracteres
- `cor`: string, obrigatório, máximo 30 caracteres
- `placa`: string, opcional, máximo 10 caracteres
- `chassi`: string, opcional, máximo 20 caracteres
- `motor`: string, opcional, máximo 20 caracteres
- `caracteristicasVisuais`: string, opcional, máximo 2000 caracteres
- `observacoes`: string, opcional, máximo 2000 caracteres
- `statusPericia`: enum `StatusPericia`
- `procedimentoId`: number, obrigatório
- `confirmarDuplicidade`: boolean, opcional

#### Enum `StatusPericia`

- `SEM_PERICIA`
- `PERICIA_EM_ANDAMENTO`
- `PERICIA_CONCLUIDA`
- `NAO_PRECISA_PERICIA`

### Respostas

#### Sucesso (201 Created)

```json
{
  "id": 1,
  "lacre": "LAC-2026-000001",
  "tipoVeiculo": "Carro",
  "marca": "Toyota",
  "modelo": "Corolla",
  "cor": "Prata",
  "placa": "ABC1D23",
  "chassi": "1234567890",
  "motor": "2.0",
  "caracteristicasVisuais": "Vidro fumê",
  "observacoes": "Sem observações",
  "statusPericia": "SEM_PERICIA",
  "situacao": "NA_DEPOL",
  "procedimentoId": 6,
  "procedimento": "BO 22/2026"
}
```

#### Erros comuns

- 400 Bad Request: payload inválido
- 404 Not Found: procedimento não encontrado
- 409 Conflict: confirmação de duplicidade necessária

---

## Endpoint: Listar veículos

### Método HTTP:

GET

### Rota:

`/veiculos`

### Descrição:

Busca veículos com filtros opcionais.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO` (Admin recebe 403).

### Parâmetros de Entrada

#### Query Parameters

- `lacre`: string, opcional
- `placa`: string, opcional
- `chassi`: string, opcional
- `motor`: string, opcional
- `situacao`: enum `SituacaoVeiculo`, opcional
- `procedimentoId`: number, opcional

#### Enum `SituacaoVeiculo`

- `NA_DEPOL`
- `EM_PATIO`
- `DEVOLVIDO`
- `DESCARTADO`

Exemplo:

```http
GET /veiculos?situacao=EM_PATIO&procedimentoId=6
```

### Respostas

#### Sucesso (200 OK)

```json
[
  {
    "id": 1,
    "lacre": "LAC-2026-000001",
    "tipoVeiculo": "Carro",
    "marca": "Toyota",
    "modelo": "Corolla",
    "cor": "Prata",
    "placa": "ABC1D23",
    "chassi": "1234567890",
    "motor": "2.0",
    "caracteristicasVisuais": "Vidro fumê",
    "observacoes": "Sem observações",
    "statusPericia": "SEM_PERICIA",
    "situacao": "EM_PATIO",
    "procedimentoId": 6,
    "procedimento": "BO 22/2026"
  }
]
```

#### Erros comuns

- 400 Bad Request: parâmetro inválido
- 401 Unauthorized: token inválido

---

## Endpoint: Buscar veículo por ID

### Método HTTP:

GET

### Rota:

`/veiculos/{id}`

### Descrição:

Busca um veículo específico pelo seu id.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO` (Admin recebe 403).

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

```json
{
  "id": 1,
  "lacre": "LAC-2026-000001",
  "tipoVeiculo": "Carro",
  "marca": "Toyota",
  "modelo": "Corolla",
  "cor": "Prata",
  "placa": "ABC1D23",
  "chassi": "1234567890",
  "motor": "2.0",
  "caracteristicasVisuais": "Vidro fumê",
  "observacoes": "Sem observações",
  "statusPericia": "SEM_PERICIA",
  "situacao": "NA_DEPOL",
  "procedimentoId": 6,
  "procedimento": "BO 22/2026"
}
```

#### Erros comuns

- 404 Not Found: veículo inexistente

---

## Endpoint: Histórico de situação do veículo

### Método HTTP:

GET

### Rota:

`/veiculos/{id}/historico`

### Descrição:

Retorna o histórico de transições de situação do veículo.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO` (Admin recebe 403).

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

```json
[
  {
    "id": 1,
    "situacaoAnterior": "NA_DEPOL",
    "situacaoNova": "EM_PATIO",
    "motivo": "Veículo encaminhado para depósito",
    "dataTransicao": "2026-10-14T10:15:00",
    "responsavel": "Administrador"
  }
]
```

#### Erros comuns

- 404 Not Found: veículo inexistente

---

## Endpoint: Editar veículo

### Método HTTP:

PUT

### Rota:

`/veiculos/{id}`

### Descrição:

Atualiza os dados cadastrais do veículo.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO` ou `ROLE_POLICIAL`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

```json
{
  "tipoVeiculo": "Carro",
  "marca": "Toyota",
  "modelo": "Corolla",
  "cor": "Branca",
  "placa": "ABC1D23",
  "chassi": "1234567890",
  "motor": "2.0",
  "caracteristicasVisuais": "Vidro fumê",
  "observacoes": "Veículo revisado",
  "statusPericia": "PERICIA_CONCLUIDA",
  "confirmarDuplicidade": false
}
```

### Respostas

#### Sucesso (200 OK)

```json
{
  "id": 1,
  "lacre": "LAC-2026-000001",
  "tipoVeiculo": "Carro",
  "marca": "Toyota",
  "modelo": "Corolla",
  "cor": "Branca",
  "placa": "ABC1D23",
  "chassi": "1234567890",
  "motor": "2.0",
  "caracteristicasVisuais": "Vidro fumê",
  "observacoes": "Veículo revisado",
  "statusPericia": "PERICIA_CONCLUIDA",
  "situacao": "NA_DEPOL",
  "procedimentoId": 6,
  "procedimento": "BO 22/2026"
}
```

#### Erros comuns

- 400 Bad Request: payload inválido
- 404 Not Found: veículo inexistente

---

## Endpoint: Alterar situação do veículo

### Método HTTP:

PATCH

### Rota:

`/veiculos/{id}/situacao`

### Descrição:

Altera a situação do veículo e grava um histórico de transição, exigindo motivo e possível confirmação de reversão.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO` ou `ROLE_POLICIAL`.

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

#### Body (Payload)

```json
{
  "situacaoNova": "EM_PATIO",
  "motivo": "Veículo encaminhado para o pátio",
  "confirmarReversao": false
}
```

Campos:

- `situacaoNova`: enum `SituacaoVeiculo`, obrigatório
- `motivo`: string, obrigatório, máximo 500 caracteres
- `confirmarReversao`: boolean, opcional

### Respostas

#### Sucesso (200 OK)

```json
{
  "id": 1,
  "lacre": "LAC-2026-000001",
  "tipoVeiculo": "Carro",
  "marca": "Toyota",
  "modelo": "Corolla",
  "cor": "Prata",
  "placa": "ABC1D23",
  "chassi": "1234567890",
  "motor": "2.0",
  "caracteristicasVisuais": "Vidro fumê",
  "observacoes": "Sem observações",
  "statusPericia": "SEM_PERICIA",
  "situacao": "EM_PATIO",
  "procedimentoId": 6,
  "procedimento": "BO 22/2026"
}
```

#### Erros comuns

- 400 Bad Request: motivo ausente ou situação inválida
- 409 Conflict: reversão não confirmada ou regra específica
- 404 Not Found: veículo inexistente

---

## Endpoint: Etiqueta do veículo (PDF com QR code)

### Método HTTP:

GET

### Rota:

`/veiculos/{id}/etiqueta`

### Descrição:

Gera a etiqueta adesiva do veículo em PDF de 100 x 50 mm: unidade, lacre, tipo/marca/modelo, cor, placa (se houver), procedimento, data de entrada e data/hora da impressão, com um QR code. O QR leva para a tela do front `/veiculos/lacre/{lacre}` (exige login; o QR não carrega dados do veículo). A etiqueta não traz nomes de pessoas.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO` (Admin recebe 403).

### Parâmetros de Entrada

#### Path Parameters

- `id`: number, obrigatório

### Respostas

#### Sucesso (200 OK)

Corpo binário (PDF). Cabeçalhos:

```http
Content-Type: application/pdf
Content-Disposition: inline; filename="etiqueta-veiculo-1.pdf"
```

`inline` faz o navegador abrir o PDF na tela (para imprimir), em vez de baixar.

#### Erros comuns

- 404 Not Found: "Veículo não encontrado"
- 401 Unauthorized: token ausente ou inválido

---

## Endpoint: Exportar veículos / relatório de carga

### Método HTTP:

GET

### Rota:

`/veiculos/exportar`

### Descrição:

Gera o relatório dos veículos apreendidos em XLSX ou PDF (A4 paisagem). O PDF traz a coluna "Conferido" em branco e a linha de assinatura para a conferência física. Quando o filtro de situação é exatamente `NA_DEPOL` + `EM_PATIO` (em qualquer ordem), o arquivo vira o **relatório de carga**: título próprio, totais por situação e nome `carga-veiculos-...`. A resposta é o arquivo, não JSON.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO` ou `ROLE_POLICIAL`.

### Parâmetros de Entrada

#### Query Parameters

- `formato`: enum `FormatoExportacao` (`XLSX` ou `PDF`), obrigatório
- `situacao`: enum `SituacaoVeiculo`, opcional e **repetível** (sem nenhuma = todas)
- `statusPericia`: enum `StatusPericia`, opcional
- `procedimentoId`: number, opcional
- `dataInicio`: string `YYYY-MM-DD`, opcional (data de entrada a partir de)
- `dataFim`: string `YYYY-MM-DD`, opcional (data de entrada até, inclusive)

A data de entrada é a primeira linha do histórico de situação do veículo.

Exemplos:

- Relatório de carga: `/veiculos/exportar?formato=PDF&situacao=NA_DEPOL&situacao=EM_PATIO`
- Só os que estão na DEPOL: `/veiculos/exportar?formato=PDF&situacao=NA_DEPOL`

### Respostas

#### Sucesso (200 OK)

Corpo binário (o arquivo). Cabeçalhos:

```http
Content-Type: application/pdf
Content-Disposition: attachment; filename="carga-veiculos-20261020-1430.pdf"
```

Nome do arquivo: `carga-veiculos-AAAAMMDD-HHMM` no relatório de carga; `veiculos-AAAAMMDD-HHMM` nos demais. XLSX com `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`.

#### Erros comuns

- 400 Bad Request: `formato` ausente, valor de enum inválido ou data inicial posterior à final
- 404 Not Found: "Nenhum veículo encontrado com os filtros informados." (lista vazia não gera arquivo)
- 403 Forbidden: Estagiário ou Admin

---

# 6.5) Painel e Minha mesa

Visões calculadas na hora a partir dos dados já registrados (nada é gravado). Cada usuário vê apenas os próprios
dados: o login vem do token, nenhuma rota recebe id de usuário.

## Endpoint: Minha mesa

### Método HTTP:

GET

### Rota:

`/painel/minha-mesa`

### Descrição:

Lista os procedimentos EM_ANDAMENTO cuja posse é do usuário logado, com tempo de posse, próxima oitiva, repasse em
andamento, última movimentação e um indicativo de próximo passo. Também devolve os números dos chips da tela.

Regras:

- `proximoPasso` segue a prioridade: `AGUARDANDO_ACEITE` (repasse pendente) → `OITIVA_ATRASADA` (oitiva agendada
  com data passada, sem baixa) → `OITIVA_AGENDADA` → `PARADO` → `NENHUM`. `descricaoProximoPasso` traz o texto pronto
  para exibir (ex.: "Oitiva hoje, 16:30", "Aguardando aceite de Ana Lima", "Parado há 63 dias", "Sem pendências").
- `ultimaMovimentacao` = data mais recente entre custódia, histórico de status, oitiva (cadastro ou conclusão),
  repasse (solicitação ou resposta) e histórico de situação dos veículos do procedimento.
- `parado` = mais de 60 dias sem movimentação.
- Ordem da lista: próximo passo (mais urgente primeiro), depois oitiva mais próxima, depois movimentação mais antiga.
- Campos de oitiva e de repasse vêm `null` quando não há oitiva agendada ou repasse pendente.
- "Repassar selecionados": o front chama `POST /repasses` uma vez por procedimento e mostra o resultado de cada um.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

Nenhum.

### Respostas

#### Sucesso (200 OK)

```json
{
  "total": 5,
  "comOitivaMarcada": 3,
  "repasseEmAndamento": 1,
  "parados": 1,
  "procedimentos": [
    {
      "id": 1,
      "procedimento": "IP 901/2026",
      "crime": "Furto",
      "naPosseDesde": "2026-10-20T10:15:02",
      "diasNaPosse": 0,
      "proximaOitivaId": null,
      "proximaOitiva": null,
      "oitivaAtrasada": false,
      "repassePendenteId": 4,
      "repasseDestinatario": "Ana Lima",
      "ultimaMovimentacao": "2026-10-20T10:16:40",
      "diasSemMovimentacao": 0,
      "parado": false,
      "proximoPasso": "AGUARDANDO_ACEITE",
      "descricaoProximoPasso": "Aguardando aceite de Ana Lima"
    },
    {
      "id": 3,
      "procedimento": "IP 903/2026",
      "crime": "Roubo",
      "naPosseDesde": "2026-10-20T10:15:10",
      "diasNaPosse": 0,
      "proximaOitivaId": 7,
      "proximaOitiva": "2026-10-20T18:00:00",
      "oitivaAtrasada": false,
      "repassePendenteId": null,
      "repasseDestinatario": null,
      "ultimaMovimentacao": "2026-10-20T10:18:03",
      "diasSemMovimentacao": 0,
      "parado": false,
      "proximoPasso": "OITIVA_AGENDADA",
      "descricaoProximoPasso": "Oitiva hoje, 18:00"
    }
  ]
}
```

Sem procedimentos na posse: `{"total": 0, "comOitivaMarcada": 0, "repasseEmAndamento": 0, "parados": 0, "procedimentos": []}`.

#### Erros comuns

- 401 Unauthorized: token ausente ou inválido
- 403 Forbidden: Admin (não acessa o painel)

---

## Endpoint: Painel inicial

### Método HTTP:

GET

### Rota:

`/painel`

### Descrição:

Indicadores da tela inicial do usuário logado:

- `oitivasHoje`: oitivas de hoje em que o usuário é o responsável, sem as desmarcadas (mesmo formato de `GET /oitivas`)
- `repassesRecebidos` / `repassesEnviados`: repasses PENDENTES em que o usuário é destinatário / solicitante
- `veiculos`: veículos que estão na DEPOL (situação `NA_DEPOL`); `semPericia` e `periciaEmAndamento` são partes do
  `total` (os veículos no pátio não entram no card)
- `procedimentosParados`: linhas paradas da Minha mesa (mesmo formato de `GET /painel/minha-mesa`)

Não há indicadores de prazo: o controle de prazos processuais é feito pelo sistema institucional da Polícia Civil.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

Nenhum.

### Respostas

#### Sucesso (200 OK)

```json
{
  "oitivasHoje": [
    {
      "id": 7,
      "dataHora": "2026-10-20T18:00:00",
      "status": "AGENDADA",
      "atrasada": false,
      "procedimentoId": 3,
      "procedimento": "IP 903/2026",
      "responsavelId": 2,
      "responsavel": "Carlos Souza",
      "partes": [
        { "tipoParte": "VITIMA", "nomeParte": "Maria Exemplo" }
      ],
      "motivoCancelamento": null,
      "cadastradoPor": "Carlos Souza",
      "alteradoPor": null,
      "dataCadastro": "2026-10-20T10:18:03"
    }
  ],
  "repassesRecebidos": [],
  "repassesEnviados": [
    {
      "id": 4,
      "procedimentoId": 1,
      "procedimento": "IP 901/2026",
      "crime": "Furto",
      "solicitante": "Carlos Souza",
      "destinatario": "Ana Lima",
      "dataSolicitacao": "2026-10-20T10:16:40"
    }
  ],
  "veiculos": {
    "total": 12,
    "semPericia": 4,
    "periciaEmAndamento": 2
  },
  "procedimentosParados": []
}
```

#### Erros comuns

- 401 Unauthorized: token ausente ou inválido
- 403 Forbidden: Admin (não acessa o painel)

---

# 6.6) Busca global

## Endpoint: Busca global

### Método HTTP:

GET

### Rota:

`/busca`

### Descrição:

Um campo só para procurar procedimentos, veículos e pessoas. Reconhece número de procedimento (`IP 45/2026`, `45/2026` ou `45`), placa e lacre, CPF, nome ou alcunha e crime. Até 10 resultados por grupo. CPF sempre mascarado.

### Autenticação:

Requer JWT. Papéis permitidos: `ROLE_DELEGADO`, `ROLE_POLICIAL` ou `ROLE_ESTAGIARIO`.

### Parâmetros de Entrada

#### Query Parameters

- `q`: obrigatório, ao menos 2 caracteres

Exemplos: `/busca?q=IP 45/2026`, `/busca?q=GWE3A22`, `/busca?q=joão`

### Respostas

#### Sucesso (200 OK)

```json
{
  "procedimentos": [
    {
      "id": 3,
      "identificacao": "IP 45/2026",
      "crime": "Roubo majorado",
      "status": "EM_ANDAMENTO",
      "detentor": "Victória Barros"
    }
  ],
  "veiculos": [
    {
      "id": 1,
      "lacre": "LAC-2026-000321",
      "descricao": "Carro Volkswagen Gol - Branco",
      "placa": "GWE3A22",
      "situacao": "NA_DEPOL",
      "procedimento": "IP 68/2026"
    }
  ],
  "pessoas": [
    {
      "id": 1,
      "nome": "João Santos",
      "alcunha": "João Grande",
      "idade": 36,
      "cpf": "***.982.247-**"
    },
    {
      "id": 6,
      "nome": "Pedro Araújo",
      "alcunha": null,
      "idade": 31,
      "cpf": null
    }
  ]
}
```

#### Erros comuns

- 400 Bad Request: "Digite ao menos 2 caracteres para buscar" ou `q` ausente
- 403 Forbidden: Admin

---

# 7) Considerações finais

## Regras práticas de consumo

- Sempre enviar `Content-Type: application/json` em requisições com body
- Para rotas autenticadas, incluir:
  ```http
  Authorization: Bearer <token>
  ```
- Para filtros e listagens, usar query params em vez de body
- Os enums devem ser enviados em texto exatamente como o backend define
- Em erros de validação, o frontend deve ler `detalhes` e mapear os campos do payload
- Rotas que devolvem arquivo (exportações e etiqueta) respondem com corpo binário: no `fetch`, usar `response.blob()`; o nome do arquivo vem no cabeçalho `Content-Disposition` (exposto pelo CORS). Em erro, o corpo continua sendo o JSON de `ErroResponseDto`
