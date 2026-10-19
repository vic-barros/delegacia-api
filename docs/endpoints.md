# Documentação Técnica da API Backend SGI-DP

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
- 403 Forbidden: login permitido, mas sem permissão
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

Retorna os dados básicos do usuário autenticado, extraídos do token JWT.

### Autenticação:

Requer JWT

### Parâmetros de Entrada

Nenhum

### Respostas

#### Sucesso (200 OK)

```json
{
  "login": "admin",
  "nome": "Administrador",
  "role": "ROLE_ADMIN"
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
    "matricula": "2025",
    "login": "joao",
    "status": "APROVADO",
    "role": "ROLE_DELEGADO",
    "motivoRejeicao": null
  }
}
```

#### Erros comuns

- 400 Bad Request: payload inválido
- 409 Conflict: procedimento duplicado (tipo + número + ano)
- 401 Unauthorized: token inválido

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
      "nomeParte": "João da Silva"
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
      "tipoParte": "INVESTIGADO",
      "nomeParte": "João da Silva"
    },
    {
      "tipoParte": "TESTEMUNHA",
      "nomeParte": "Maria Souza"
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
      "nomeParte": "Ana Pereira"
    }
  ],
  "confirmarConflito": false
}
```

Campos:

- `responsavelId`: number, obrigatório
- `partes`: lista obrigatória com ao menos 1 item
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

Requer JWT

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

Requer JWT

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

Requer JWT

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
