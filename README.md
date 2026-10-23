<p align="center">
  <img src="docs/img/logo-sisgep.jpg" alt="SISGEP" width="200">
</p>

<h1 align="center">SISGEP: Sistema Interno de Gestão Policial</h1>

<p align="center">
  Projeto desenvolvido por alunos do <strong>IFS</strong> e submetido ao <strong>Inovathon</strong> pela equipe <strong>Delegacia 4.0</strong>.<br>
  Front-end (React): <a href="https://github.com/jaopzj/sisgep-frontend">jaopzj/sisgep-frontend</a>
</p>

---

## Sobre o projeto

<p align="justify">&emsp;&emsp;API REST para a gestão e o controle interno de delegacias policiais, com o registro, o acompanhamento e a
rastreabilidade da posse e do repasse de procedimentos entre policiais, das oitivas e dos veículos apreendidos. Cada
movimentação fica no histórico, com o responsável identificado.</p>

<p align="justify">&emsp;&emsp;Hoje <strong>todo o controle interno da delegacia é feito em livros, planilhas e papel</strong>: não existe
sistema para saber quem está com cada procedimento, quando a oitiva foi marcada ou onde está o veículo apreendido. O
SISGEP sistematiza esse controle pela primeira vez e devolve tempo à atividade-fim da Polícia Civil: a investigação
criminal e o atendimento ao público. Os prazos processuais continuam no sistema institucional, que já os controla.</p>

## Funcionalidades

| Módulo | O que faz |
|---|---|
| **Usuários e acesso** | Cadastro com aprovação pelo Admin, perfis (Admin, Delegado, Policial, Estagiário) e login com JWT |
| **Procedimentos e custódia** | Cadastro, posse com histórico, **repasse com aceite** e arquivamento |
| **Oitivas** | Agenda com alerta de conflito, remarcação, desmarcação com motivo e avisos automáticos |
| **Pessoas** | Vítimas, testemunhas e investigados ligados às oitivas, com CPF mascarado |
| **Veículos apreendidos** | **Lacre gerado pelo sistema**, **etiqueta com QR code**, situação e perícia com histórico |
| **Painel e Minha mesa** | O dia do servidor e o **próximo passo** de cada procedimento |
| **Relatórios** | Procedimentos e veículos em XLSX e PDF, incluindo o **relatório de carga** para conferência física |

## Tecnologias

Java 21, Spring Boot 4, Spring Security (JWT), Spring Data JPA, PostgreSQL, Apache PDFBox, ZXing, Apache POI e
OpenPDF. Front-end em React + Vite. Implantação local em cada delegacia com Docker Compose (sigilo e LGPD).

## Como executar

1. Crie um banco vazio no PostgreSQL e copie `.env.example` para `.env`, preenchendo banco, `JWT_SECRET` (mínimo de
   32 caracteres) e o Admin inicial. Para o front em React: `CORS_ORIGENS_PERMITIDAS=http://localhost:5173`.
2. Suba a API com `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`). Ela fica em `http://localhost:8080`.
3. Opcional: rode [`docs/demo/dados-demonstracao.sql`](docs/demo/dados-demonstracao.sql) num banco **só de
   demonstração** e entre com `vbarros` / `Demo@2026`.

## Documentação

- [Documentação do projeto](docs/SISGEP_Documentacao.docx): requisitos, casos de uso, modelagem e arquitetura
- [Contrato das rotas](docs/endpoints.md) e [diagramas](docs/diagramas/)

## Equipe

Alunos do IFS, equipe <strong>Delegacia 4.0</strong>: **Laíssa Victória Barros** (líder), Elton Ramon Alves Santos,
Erick Marques Santos Bomfim, Hebert Ruan do Nascimento Feitosa e João Pedro da Silva Carvalho.

> Todos os dados usados em testes, demonstrações e telas são fictícios.
