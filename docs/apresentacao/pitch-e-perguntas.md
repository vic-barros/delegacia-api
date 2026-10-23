# Pitch do SISGEP (5 minutos) e perguntas prováveis

## Roteiro do pitch

A fala completa de cada slide está nas notas do apresentador. Somando os tempos sugeridos, dá cerca de 4 minutos:
sobra 1 minuto de folga para respirar, para uma pausa ou para mostrar a etiqueta impressa.

| # | Slide | Tempo | Mensagem principal |
|---|---|---|---|
| 1 | Capa | 0:15 | Quem somos e o que é o SISGEP |
| 2 | O problema | 0:25 | O controle interno da delegacia está em livros e planilhas e se perde |
| 3 | A solução | 0:20 | Um sistema só, com histórico de cada mudança |
| 4 | Painel | 0:20 | O dia do servidor em uma tela |
| 5 | Minha mesa | 0:25 | Cada procedimento diz o próximo passo; nada fica esquecido |
| 6 | Repasse | 0:20 | A custódia só muda de mãos com aceite |
| 7 | Veículo | 0:25 | Lacre, etiqueta com QR e relatório de carga conferido |
| 8 | Sigilo e LGPD | 0:25 | O dado de investigação não sai da delegacia |
| 9 | Implantação | 0:25 | Um computador, um comando; pronto para crescer |
| 10 | Próximos passos | 0:25 | Toda a custódia da delegacia em um só lugar |
| 11 | Obrigado | 0:10 | Fechamento e equipe |

**Dicas para o treino**
- Cronometrar pelo menos 3 vezes, em pé, com os slides. Se passar de 4min30, encurtar as falas dos slides 4 a 7 (as telas falam por si).
- Definir quem fala cada bloco. Sugestão: abertura (1 a 3), telas (4 a 7), confiança e futuro (8 a 11). Trocar de pessoa no início de cada bloco, sem "agora o fulano vai falar".
- Uma frase de efeito para abrir e outra para fechar, decoradas. O resto pode ser dito com as próprias palavras.
- Levar uma **etiqueta impressa** do veículo de demonstração e escanear o QR com o celular durante o slide 7, se houver tempo.

## Demonstração (se pedirem) e plano B

- Banco de demonstração: rodar `docs/demo/dados-demonstracao.sql` **na manhã da apresentação** (as datas são relativas ao dia). Login `vbarros` / `Demo@2026`.
- Roteiro curto: login → painel → minha mesa (aguardando aceite, oitiva hoje, parado há 63 dias) → repasses recebidos → ficha do Gol `LAC-AAAA-000321` e etiqueta com QR → relatório de carga em PDF.
- Plano B: vídeo de 2 minutos gravado com esse roteiro, no notebook **e** num pendrive, mais o PDF da documentação.
- Na busca de pessoas, digitar "Jo" ou "João" (a busca ainda diferencia acentos).

## Perguntas prováveis

### Sobre o problema e o uso
**Quem usaria o sistema?** Os servidores da delegacia: Delegado, Policial (agente e escrivão) e Estagiário, cada um com o seu perfil. O Admin aprova os cadastros e gerencia os usuários.

**Por que não usar o sistema que a Polícia Civil já tem?** O sistema institucional controla o procedimento policial e os prazos. O SISGEP cuida do controle **interno** que hoje está em papel: quem está com cada procedimento, oitivas, veículos e carga da delegacia. Ele não substitui o sistema oficial, complementa.

**Vocês controlam prazos?** Não, por decisão: o prazo processual já é controlado pelo sistema institucional, e duplicar esse controle criaria duas fontes de verdade.

**Mas as telas mostram prazo ("Vence em 2 dias", modelo "Prazos").** O protótipo foi desenhado antes dessa decisão e ainda não foi ajustado. A API não tem prazo: esses elementos saem do front. Melhor dizer isso antes de perguntarem, ao mostrar a primeira tela.

**O que muda em relação à versão anterior (v1)?** A v1 era Java puro com JDBC. A v2 tem arquitetura em camadas, segurança com JWT e perfis, repasse com aceite (na v1 era de um lado só), veículos, pessoas, painel e relatórios.

### Sobre segurança, sigilo e LGPD
**Como vocês protegem os dados?**
- Instalação local, na rede interna de cada delegacia.
- Login com token (JWT) e senhas com hash BCrypt.
- Cada rota liberada só para os perfis certos.
- CPF sempre mascarado na API; telefone e endereço só aparecem na ficha da pessoa.
- Históricos que não podem ser alterados, com registro de quem cadastrou e alterou.

**Por que instalação local e não na nuvem?**
- **Sigilo:** os dados não saem da delegacia.
- **Isolamento:** sem banco compartilhado, não há risco de uma delegacia ver dados de outra.
- **Disponibilidade:** funciona sem internet.
- **Custo:** zero de nuvem.

A evolução para um sistema centralizado está prevista na documentação (seção 1.8).

**E se a Polícia Civil quiser centralizar?** A API já aceita tokens de um provedor externo, então daria para usar o login da intranet. Os dados seriam separados por delegacia com um recurso do próprio Hibernate, e a numeração passaria a ser por unidade. Não é preciso reescrever o sistema.

**E a IA? Os dados vão para o Google?** A IA está em desenvolvimento e é opcional. As regras de sigilo já estão definidas:
- só o detentor do procedimento pede o resumo;
- os dados vão minimizados, com as partes identificadas só pelo papel ("Testemunha 1");
- a chave fica só no servidor;
- a resposta é marcada como gerada por IA e conferida por uma pessoa.

Na demo, só dados fictícios. Em produção, um contrato sem retenção dos dados ou um modelo rodando localmente.

**Um usuário desativado continua acessando?** O token vale até 8 horas. Hoje, a desativação vale a partir do próximo login. A revogação imediata está mapeada como melhoria.

### Sobre a parte técnica
**Quais tecnologias?** Java 21 com Spring Boot, Spring Security e JPA; PostgreSQL; React no front; PDFBox, ZXing (QR), Apache POI e OpenPDF para os arquivos.

**Como o sistema sabe que um procedimento está parado?** Ele pega a data mais recente de qualquer movimentação: posse, mudança de status, oitiva, repasse ou veículo. Se passou de 60 dias, o procedimento aparece como parado.

**Isso aguenta muitos dados?** A Minha mesa faz uma consulta por tabela para todos os procedimentos de uma vez (com GROUP BY e MAX), e não uma por procedimento: são cerca de 8 consultas, seja qual for o tamanho da mesa.

**Como funciona o lacre e o QR?** O lacre é gerado pelo sistema a partir de uma sequência do banco (LAC-ano-número) e é único. O QR leva só o endereço da ficha no sistema, que exige login: quem escanear sem acesso não vê nada.

**O que acontece se dois usuários fizerem algo ao mesmo tempo?** Para o lacre, é usada uma sequence do banco, que nunca repete número. O repasse só aceita um pendente por procedimento. Nos ofícios (próxima entrega), a numeração do ano é travada no banco enquanto o número é gerado.

**Como vocês testaram?** Cada funcionalidade foi testada pela API (Postman e roteiros automatizados), com um banco novo, cobrindo o caminho certo, cada erro de regra de negócio e as permissões de cada perfil. Os testes automatizados no código são uma melhoria prevista.

### Sobre o projeto e a equipe
**O que é inovador?**
- A etiqueta com QR para o veículo apreendido.
- O relatório de carga pronto para a conferência física.
- A Minha mesa com o próximo passo de cada caso.
- O repasse com aceite.
- O sigilo pensado desde o desenho.

**O que falta?**
- Ofícios (já especificado).
- Objetos apreendidos e patrimônio.
- Fotos do veículo.
- Importação do acervo, para a carga completa sair com um clique.
- Vínculos e IA (em desenvolvimento).

**Quanto custa para uma delegacia?** O software usa tecnologias livres. A delegacia precisa só de um computador para servir o sistema na rede interna e de uma mídia para o backup.

**O que foi feito pela equipe e o que foi IA?** Responder com tranquilidade e honestidade:
- **A equipe** definiu as regras de negócio com base na rotina real da delegacia, escreveu e testou o código e tomou as decisões de arquitetura.
- **A IA** foi usada como apoio, para revisar código, sugerir soluções e ajudar na documentação, como qualquer equipe de desenvolvimento faz hoje.
