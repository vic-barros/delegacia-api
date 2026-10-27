-- =====================================================================================================
-- SISGEP - dados de DEMONSTRAÇÃO (todos fictícios)
--
-- Os INSERTs nomeiam as colunas: funciona mesmo num banco antigo em que a ordem física das colunas mudou.
--
-- Como usar (num banco SÓ de demonstração):
--   1. Crie o banco vazio e suba a API uma vez (o Hibernate cria as tabelas, as roles e o Admin).
--   2. Rode este script inteiro no pgAdmin / DBeaver (Query Tool), com a API ligada ou desligada.
--   3. Entre com: vbarros / Demo@2026 (Delegada). Outros logins na tabela abaixo, mesma senha.
--
-- As datas são relativas ao momento em que o script roda ("hoje", "há 2 horas", "há 63 dias"):
-- rode de novo no dia da apresentação. O script apaga os dados anteriores (tudo, menos roles e Admin).
--
-- Usuários (senha de todos: Demo@2026):
--   vbarros   Victória Barros   Delegada     (usuária da demo: painel, minha mesa, repasses)
--   csouza    Carlos Souza      Policial
--   mandrade  Marcos Andrade    Policial
--   alima     Ana Lima          Policial
--   csilva    Carolina Silva    Policial
--   palves    Pedro Alves       Estagiário
--   jnovo     João Novo         Policial PENDENTE (para mostrar a aprovação pelo Admin)
-- =====================================================================================================

DO $$
DECLARE
    -- Horário de Brasília, como a API (o banco pode estar em outro fuso, ex.: UTC num servidor)
    agora     timestamp := now() AT TIME ZONE 'America/Sao_Paulo';
    hoje      timestamp := date_trunc('day', now() AT TIME ZONE 'America/Sao_Paulo');   -- hoje, 00:00
    ano       integer   := extract(year from now() AT TIME ZONE 'America/Sao_Paulo')::integer;
    senha     text      := '$2a$10$Jg7I.zvR6B/AM1eFAMlgyuEG0NbrkCcr98xQfoYfmWjmBkTUnpA0.';  -- BCrypt de Demo@2026
    r_del bigint; r_pol bigint; r_est bigint; admin_id bigint;
    u_vb bigint; u_cs bigint; u_ma bigint; u_al bigint; u_ci bigint; u_pa bigint; u_jn bigint;
    p1 bigint; p2 bigint; p3 bigint; p4 bigint; p5 bigint; p6 bigint; p7 bigint; p8 bigint;
    p9 bigint; p10 bigint; p11 bigint; p12 bigint; p13 bigint;
    rp3 bigint; rp4 bigint; rp6 bigint; rp7 bigint; rp8 bigint;
    pe_joao bigint; pe_maria bigint; pe_carlos bigint; pe_rita bigint; pe_laura bigint; pe_pedro bigint;
    o bigint;
    v bigint;
BEGIN
    -- ---------- limpeza (ordem das chaves estrangeiras) ----------
    DELETE FROM notificacao;
    DELETE FROM oitiva_parte;
    DELETE FROM oitiva;
    DELETE FROM historico_situacao_veiculo;
    DELETE FROM veiculo;
    DELETE FROM historico_custodia;
    DELETE FROM repasse;
    DELETE FROM historico_status_procedimento;
    DELETE FROM procedimento;
    DELETE FROM pessoa;
    DELETE FROM usuario WHERE role_id <> (SELECT id FROM role WHERE acesso = 'ROLE_ADMIN');

    SELECT id INTO r_del FROM role WHERE acesso = 'ROLE_DELEGADO';
    SELECT id INTO r_pol FROM role WHERE acesso = 'ROLE_POLICIAL';
    SELECT id INTO r_est FROM role WHERE acesso = 'ROLE_ESTAGIARIO';
    SELECT min(id) INTO admin_id FROM usuario;

    -- ---------- usuários ----------
    u_vb := nextval('seq_usuario'); INSERT INTO usuario (id, login, matricula, motivo_rejeicao, nome, senha_hash, status, role_id) VALUES (u_vb, 'vbarros',  '2001', NULL, 'Victória Barros', senha, 'APROVADO', r_del);
    u_cs := nextval('seq_usuario'); INSERT INTO usuario (id, login, matricula, motivo_rejeicao, nome, senha_hash, status, role_id) VALUES (u_cs, 'csouza',   '2002', NULL, 'Carlos Souza',    senha, 'APROVADO', r_pol);
    u_ma := nextval('seq_usuario'); INSERT INTO usuario (id, login, matricula, motivo_rejeicao, nome, senha_hash, status, role_id) VALUES (u_ma, 'mandrade', '2003', NULL, 'Marcos Andrade',  senha, 'APROVADO', r_pol);
    u_al := nextval('seq_usuario'); INSERT INTO usuario (id, login, matricula, motivo_rejeicao, nome, senha_hash, status, role_id) VALUES (u_al, 'alima',    '2004', NULL, 'Ana Lima',        senha, 'APROVADO', r_pol);
    u_ci := nextval('seq_usuario'); INSERT INTO usuario (id, login, matricula, motivo_rejeicao, nome, senha_hash, status, role_id) VALUES (u_ci, 'csilva',   '2005', NULL, 'Carolina Silva',  senha, 'APROVADO', r_pol);
    u_pa := nextval('seq_usuario'); INSERT INTO usuario (id, login, matricula, motivo_rejeicao, nome, senha_hash, status, role_id) VALUES (u_pa, 'palves',   '2006', NULL, 'Pedro Alves',     senha, 'APROVADO', r_est);
    u_jn := nextval('seq_usuario'); INSERT INTO usuario (id, login, matricula, motivo_rejeicao, nome, senha_hash, status, role_id) VALUES (u_jn, 'jnovo',    '2007', NULL, 'João Novo',       senha, 'PENDENTE', r_pol);

    -- ---------- procedimentos ----------
    -- Mesa da Victória (em andamento)
    p1  := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p1,  ano, 'Furto qualificado',  (hoje - interval '25 days')::date, NULL, 112, NULL, 'EM_ANDAMENTO', 'IP',  u_vb);
    p2  := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p2,  ano, 'Tráfico de drogas',  (hoje - interval '8 days')::date,  NULL, 8,   NULL, 'EM_ANDAMENTO', 'APF', u_vb);
    p3  := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p3,  ano, 'Roubo majorado',     (hoje - interval '30 days')::date, NULL, 45,  NULL, 'EM_ANDAMENTO', 'IP',  u_vb);
    p4  := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p4,  ano, 'Estelionato',        (hoje - interval '5 days')::date,  NULL, 98,  NULL, 'EM_ANDAMENTO', 'BO',  u_vb);
    p5  := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p5,  ano, 'Ameaça',             (hoje - interval '90 days')::date, NULL, 30,  NULL, 'EM_ANDAMENTO', 'IP',  u_vb);
    p6  := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p6,  ano, 'Roubo de veículo',   (hoje - interval '6 days')::date,  NULL, 68,  NULL, 'EM_ANDAMENTO', 'IP',  u_vb);
    -- Com outros servidores
    p7  := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p7,  ano, 'Roubo',              (hoje - interval '12 days')::date, NULL, 77,  NULL, 'EM_ANDAMENTO', 'IP',  u_ma);
    p8  := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p8,  ano, 'Furto',              (hoje - interval '15 days')::date, NULL, 43,  NULL, 'EM_ANDAMENTO', 'BO',  u_ci);
    p9  := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p9,  ano, 'Homicídio',          (hoje - interval '20 days')::date, NULL, 110, NULL, 'EM_ANDAMENTO', 'IP',  u_cs);
    p10 := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p10, ano, 'Ameaça',             (hoje - interval '10 days')::date, NULL, 51,  NULL, 'EM_ANDAMENTO', 'TCO', u_al);
    p13 := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p13, ano, 'Tráfico de drogas',  (hoje - interval '40 days')::date, NULL, 7,   NULL, 'EM_ANDAMENTO', 'APF', u_ma);
    -- Arquivados (um com remessa final registrada = aba "Remetidos")
    p11 := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p11, ano, 'Furto',              (hoje - interval '70 days')::date, (hoje - interval '10 days')::date, 15, 'PROT-' || ano || '-000123', 'ARQUIVADO', 'IP', u_vb);
    p12 := nextval('seq_procedimento'); INSERT INTO procedimento (id, ano, crime, data_abertura, data_remessa_final, numero, protocolo_remessa_final, status, tipo, detentor_atual_id) VALUES (p12, ano, 'Lesão corporal',     (hoje - interval '80 days')::date, NULL, 3,   NULL, 'ARQUIVADO',    'IP',  u_cs);

    -- ---------- histórico de status (cadastro = anterior nulo -> EM_ANDAMENTO) ----------
    INSERT INTO historico_status_procedimento (id, data_transicao, motivo, status_anterior, status_novo, procedimento_id, responsavel_id)
    SELECT nextval('seq_historico_status_procedimento'), p.data_abertura + time '10:00', 'Cadastro do procedimento', NULL, 'EM_ANDAMENTO', p.id,
           CASE WHEN p.id = p3 THEN u_cs ELSE p.detentor_atual_id END
    FROM procedimento p;
    INSERT INTO historico_status_procedimento (id, data_transicao, motivo, status_anterior, status_novo, procedimento_id, responsavel_id) VALUES (nextval('seq_historico_status_procedimento'), hoje - interval '10 days' + time '15:00',
        'Relatado e remetido ao Poder Judiciário', 'EM_ANDAMENTO', 'ARQUIVADO', p11, u_vb);
    INSERT INTO historico_status_procedimento (id, data_transicao, motivo, status_anterior, status_novo, procedimento_id, responsavel_id) VALUES (nextval('seq_historico_status_procedimento'), hoje - interval '40 days' + time '11:00',
        'Arquivamento determinado pelo Ministério Público', 'EM_ANDAMENTO', 'ARQUIVADO', p12, u_cs);

    -- ---------- repasses ----------
    rp3 := nextval('seq_repasse'); INSERT INTO repasse (id, data_resposta, data_solicitacao, justificativa_recusa, status, destinatario_id, procedimento_id, solicitante_id) VALUES (rp3, hoje - interval '20 days' + time '11:00', hoje - interval '20 days' + time '09:30', NULL, 'ACEITO', u_vb, p3, u_cs);
    rp6 := nextval('seq_repasse'); INSERT INTO repasse (id, data_resposta, data_solicitacao, justificativa_recusa, status, destinatario_id, procedimento_id, solicitante_id) VALUES (rp6, hoje - interval '3 days' + time '16:00', hoje - interval '3 days' + time '10:00',
        'Estou de férias a partir da próxima semana', 'RECUSADO', u_ma, p6, u_vb);
    rp4 := nextval('seq_repasse'); INSERT INTO repasse (id, data_resposta, data_solicitacao, justificativa_recusa, status, destinatario_id, procedimento_id, solicitante_id) VALUES (rp4, NULL, agora - interval '30 minutes', NULL, 'PENDENTE', u_al, p4, u_vb);
    rp7 := nextval('seq_repasse'); INSERT INTO repasse (id, data_resposta, data_solicitacao, justificativa_recusa, status, destinatario_id, procedimento_id, solicitante_id, observacao) VALUES (rp7, NULL, agora - interval '2 hours',    NULL, 'PENDENTE', u_vb, p7, u_ma, 'Falta ouvir a testemunha, segue para você conduzir.');
    rp8 := nextval('seq_repasse'); INSERT INTO repasse (id, data_resposta, data_solicitacao, justificativa_recusa, status, destinatario_id, procedimento_id, solicitante_id) VALUES (rp8, NULL, agora - interval '1 day',      NULL, 'PENDENTE', u_vb, p8, u_ci);

    -- ---------- custódia ----------
    INSERT INTO historico_custodia (id, data_fim, data_inicio, origem, procedimento_id, repasse_origem_id, usuario_id)
    SELECT nextval('seq_historico_custodia'), CASE WHEN p.id = p3 THEN hoje - interval '20 days' + time '11:00' END,
           p.data_abertura + time '10:00', 'CADASTRO_INICIAL', p.id, NULL,
           CASE WHEN p.id = p3 THEN u_cs ELSE p.detentor_atual_id END
    FROM procedimento p;
    INSERT INTO historico_custodia (id, data_fim, data_inicio, origem, procedimento_id, repasse_origem_id, usuario_id) VALUES (nextval('seq_historico_custodia'), NULL, hoje - interval '20 days' + time '11:00', 'REPASSE', p3, rp3, u_vb);
    -- IP 30: "parado" desde a posse (63 dias sem nenhuma movimentação)
    UPDATE historico_custodia SET data_inicio = agora - interval '63 days' WHERE procedimento_id = p5;
    UPDATE historico_status_procedimento SET data_transicao = agora - interval '90 days' WHERE procedimento_id = p5;

    -- ---------- pessoas (CPFs fictícios, válidos só no cálculo) ----------
    pe_joao   := nextval('seq_pessoa'); INSERT INTO pessoa (id, alcunha, cpf, data_cadastro, data_nascimento, endereco, nome, telefone, cadastrado_por_id) VALUES (pe_joao,   'João Grande', '52998224725', hoje - interval '25 days', '1990-05-10', 'Rua Fictícia, 10, Centro, Aracaju/SE', 'João Santos', '(79) 99999-0001', u_vb);
    pe_maria  := nextval('seq_pessoa'); INSERT INTO pessoa (id, alcunha, cpf, data_cadastro, data_nascimento, endereco, nome, telefone, cadastrado_por_id) VALUES (pe_maria,  NULL, '11144477735', hoje - interval '25 days', '1985-03-22', 'Av. Exemplo, 200, Jardins, Aracaju/SE', 'Maria Ribeiro', '(79) 99999-0002', u_vb);
    pe_carlos := nextval('seq_pessoa'); INSERT INTO pessoa (id, alcunha, cpf, data_cadastro, data_nascimento, endereco, nome, telefone, cadastrado_por_id) VALUES (pe_carlos, 'Galego', NULL, hoje - interval '8 days', NULL, NULL, 'Carlos Pereira', NULL, u_vb);
    pe_rita   := nextval('seq_pessoa'); INSERT INTO pessoa (id, alcunha, cpf, data_cadastro, data_nascimento, endereco, nome, telefone, cadastrado_por_id) VALUES (pe_rita,   NULL, '52601815906', hoje - interval '10 days', '1978-11-02', NULL, 'Rita Cardoso', '(79) 99999-0004', u_al);
    pe_laura  := nextval('seq_pessoa'); INSERT INTO pessoa (id, alcunha, cpf, data_cadastro, data_nascimento, endereco, nome, telefone, cadastrado_por_id) VALUES (pe_laura,  NULL, '08301661305', hoje - interval '20 days', '2001-07-15', NULL, 'Laura Mendes', NULL, u_cs);
    pe_pedro  := nextval('seq_pessoa'); INSERT INTO pessoa (id, alcunha, cpf, data_cadastro, data_nascimento, endereco, nome, telefone, cadastrado_por_id) VALUES (pe_pedro,  NULL, NULL, hoje - interval '20 days', '1995-01-30', NULL, 'Pedro Araújo', NULL, u_vb);

    -- ---------- oitivas ----------
    -- IP 112: agendada para ontem e ninguém deu baixa ("Oitiva sem baixa")
    o := nextval('seq_oitiva'); INSERT INTO oitiva (id, data_cadastro, data_hora, motivo_cancelamento, status, alterado_por_id, cadastrado_por_id, procedimento_id, responsavel_id) VALUES (o, hoje - interval '10 days', hoje - interval '1 day' + time '10:00', NULL, 'AGENDADA', NULL, u_pa, p1, u_vb);
    INSERT INTO oitiva_parte (id, nome_parte, tipo_parte, oitiva_id, pessoa_id) VALUES (nextval('seq_oitiva_parte'), 'Fulano de Tal', 'INVESTIGADO', o, NULL);
    -- APF 08: hoje, 16:30
    o := nextval('seq_oitiva'); INSERT INTO oitiva (id, data_cadastro, data_hora, motivo_cancelamento, status, alterado_por_id, cadastrado_por_id, procedimento_id, responsavel_id) VALUES (o, hoje - interval '7 days', hoje + time '16:30', NULL, 'AGENDADA', NULL, u_vb, p2, u_vb);
    INSERT INTO oitiva_parte (id, nome_parte, tipo_parte, oitiva_id, pessoa_id) VALUES (nextval('seq_oitiva_parte'), 'Carlos Pereira', 'VITIMA', o, pe_carlos);
    -- IP 45: concluída hoje cedo e outra amanhã 09:00 (João Santos, testemunha)
    o := nextval('seq_oitiva'); INSERT INTO oitiva (id, data_cadastro, data_hora, motivo_cancelamento, status, alterado_por_id, cadastrado_por_id, procedimento_id, responsavel_id) VALUES (o, hoje - interval '15 days', least(hoje + time '09:00', agora - interval '1 hour'), NULL, 'CONCLUIDA', u_vb, u_vb, p3, u_vb);
    INSERT INTO oitiva_parte (id, nome_parte, tipo_parte, oitiva_id, pessoa_id) VALUES (nextval('seq_oitiva_parte'), 'Maria Ribeiro', 'VITIMA', o, pe_maria);
    o := nextval('seq_oitiva'); INSERT INTO oitiva (id, data_cadastro, data_hora, motivo_cancelamento, status, alterado_por_id, cadastrado_por_id, procedimento_id, responsavel_id) VALUES (o, hoje - interval '2 days', hoje + interval '1 day' + time '09:00', NULL, 'AGENDADA', NULL, u_vb, p3, u_vb);
    INSERT INTO oitiva_parte (id, nome_parte, tipo_parte, oitiva_id, pessoa_id) VALUES (nextval('seq_oitiva_parte'), 'João Santos', 'TESTEMUNHA', o, pe_joao);
    INSERT INTO oitiva_parte (id, nome_parte, tipo_parte, oitiva_id, pessoa_id) VALUES (nextval('seq_oitiva_parte'), 'Pedro Araújo', 'TESTEMUNHA', o, pe_pedro);
    -- BO 98: hoje, 14:00 (João Santos de novo: aparece ligado a dois procedimentos no grafo de vínculos)
    o := nextval('seq_oitiva'); INSERT INTO oitiva (id, data_cadastro, data_hora, motivo_cancelamento, status, alterado_por_id, cadastrado_por_id, procedimento_id, responsavel_id) VALUES (o, hoje - interval '4 days', hoje + time '14:00', NULL, 'AGENDADA', NULL, u_vb, p4, u_vb);
    INSERT INTO oitiva_parte (id, nome_parte, tipo_parte, oitiva_id, pessoa_id) VALUES (nextval('seq_oitiva_parte'), 'João Santos', 'TESTEMUNHA', o, pe_joao);
    -- IP 110 (Carlos Souza): daqui a 3 dias, 10:00
    o := nextval('seq_oitiva'); INSERT INTO oitiva (id, data_cadastro, data_hora, motivo_cancelamento, status, alterado_por_id, cadastrado_por_id, procedimento_id, responsavel_id) VALUES (o, hoje - interval '5 days', hoje + interval '3 days' + time '10:00', NULL, 'AGENDADA', NULL, u_cs, p9, u_cs);
    INSERT INTO oitiva_parte (id, nome_parte, tipo_parte, oitiva_id, pessoa_id) VALUES (nextval('seq_oitiva_parte'), 'Laura Mendes', 'TESTEMUNHA', o, pe_laura);
    -- TCO 51 (Ana Lima): daqui a 2 dias, 14:00, agendada pelo estagiário
    o := nextval('seq_oitiva'); INSERT INTO oitiva (id, data_cadastro, data_hora, motivo_cancelamento, status, alterado_por_id, cadastrado_por_id, procedimento_id, responsavel_id) VALUES (o, hoje - interval '3 days', hoje + interval '2 days' + time '14:00', NULL, 'AGENDADA', NULL, u_pa, p10, u_al);
    INSERT INTO oitiva_parte (id, nome_parte, tipo_parte, oitiva_id, pessoa_id) VALUES (nextval('seq_oitiva_parte'), 'Rita Cardoso', 'VITIMA', o, pe_rita);
    -- BO 43 (Carolina Silva): desmarcada ontem, 13:00
    o := nextval('seq_oitiva'); INSERT INTO oitiva (id, data_cadastro, data_hora, motivo_cancelamento, status, alterado_por_id, cadastrado_por_id, procedimento_id, responsavel_id) VALUES (o, hoje - interval '6 days', hoje - interval '1 day' + time '13:00', 'Testemunha internada; nova data a combinar', 'DESMARCADA', u_ci, u_ci, p8, u_ci);
    INSERT INTO oitiva_parte (id, nome_parte, tipo_parte, oitiva_id, pessoa_id) VALUES (nextval('seq_oitiva_parte'), 'Beltrano de Souza', 'TESTEMUNHA', o, NULL);

    -- ---------- veículos ----------
    v := nextval('seq_veiculo'); INSERT INTO veiculo (id, caracteristicas_visuais, chassi, cor, lacre, marca, modelo, motor, observacoes, placa, situacao, pericia, tipo_veiculo, procedimento_id) VALUES (v, 'Retrovisor dianteiro quebrado', '9C2KC1670GR000000', 'Branco', 'LAC-' || ano || '-000321', 'Volkswagen', 'Gol', 'KC16E0000000', NULL, 'GWE3A22', 'NA_DEPOL', 'PERICIA_EM_ANDAMENTO', 'Carro', p6);
    INSERT INTO historico_situacao_veiculo (id, data_transicao, motivo, situacao_anterior, situacao_nova, responsavel_id, veiculo_id) VALUES (nextval('seq_historico_situacao_veiculo'), hoje - interval '6 days' + time '15:30', 'Cadastro do veículo apreendido', NULL, 'NA_DEPOL', u_vb, v);
    v := nextval('seq_veiculo'); INSERT INTO veiculo (id, caracteristicas_visuais, chassi, cor, lacre, marca, modelo, motor, observacoes, placa, situacao, pericia, tipo_veiculo, procedimento_id) VALUES (v, 'Tanque com adesivo', NULL, 'Vermelha', 'LAC-' || ano || '-000320', 'Honda', 'CG 160', NULL, NULL, 'QWE1B23', 'NA_DEPOL', 'SEM_PERICIA', 'Moto', p2);
    INSERT INTO historico_situacao_veiculo (id, data_transicao, motivo, situacao_anterior, situacao_nova, responsavel_id, veiculo_id) VALUES (nextval('seq_historico_situacao_veiculo'), hoje - interval '8 days' + time '11:00', 'Cadastro do veículo apreendido', NULL, 'NA_DEPOL', u_vb, v);
    v := nextval('seq_veiculo'); INSERT INTO veiculo (id, caracteristicas_visuais, chassi, cor, lacre, marca, modelo, motor, observacoes, placa, situacao, pericia, tipo_veiculo, procedimento_id) VALUES (v, NULL, NULL, 'Prata', 'LAC-' || ano || '-000319', 'Fiat', 'Uno', NULL, 'Sem placa dianteira', 'ABC1D23', 'NA_DEPOL', 'SEM_PERICIA', 'Carro', p13);
    INSERT INTO historico_situacao_veiculo (id, data_transicao, motivo, situacao_anterior, situacao_nova, responsavel_id, veiculo_id) VALUES (nextval('seq_historico_situacao_veiculo'), hoje - interval '40 days' + time '09:00', 'Cadastro do veículo apreendido', NULL, 'NA_DEPOL', u_ma, v);
    v := nextval('seq_veiculo'); INSERT INTO veiculo (id, caracteristicas_visuais, chassi, cor, lacre, marca, modelo, motor, observacoes, placa, situacao, pericia, tipo_veiculo, procedimento_id) VALUES (v, NULL, NULL, 'Preto', 'LAC-' || ano || '-000318', 'Toyota', 'Corolla', NULL, NULL, 'RST4E56', 'NA_DEPOL', 'PERICIA_CONCLUIDA', 'Carro', p7);
    INSERT INTO historico_situacao_veiculo (id, data_transicao, motivo, situacao_anterior, situacao_nova, responsavel_id, veiculo_id) VALUES (nextval('seq_historico_situacao_veiculo'), hoje - interval '12 days' + time '14:00', 'Cadastro do veículo apreendido', NULL, 'NA_DEPOL', u_ma, v);
    v := nextval('seq_veiculo'); INSERT INTO veiculo (id, caracteristicas_visuais, chassi, cor, lacre, marca, modelo, motor, observacoes, placa, situacao, pericia, tipo_veiculo, procedimento_id) VALUES (v, NULL, NULL, 'Branca', 'LAC-' || ano || '-000317', 'Volkswagen', 'Saveiro', NULL, NULL, 'XYZ7F89', 'NA_DEPOL', 'NAO_PRECISA_PERICIA', 'Caminhonete', p8);
    INSERT INTO historico_situacao_veiculo (id, data_transicao, motivo, situacao_anterior, situacao_nova, responsavel_id, veiculo_id) VALUES (nextval('seq_historico_situacao_veiculo'), hoje - interval '15 days' + time '16:00', 'Cadastro do veículo apreendido', NULL, 'NA_DEPOL', u_ci, v);
    v := nextval('seq_veiculo'); INSERT INTO veiculo (id, caracteristicas_visuais, chassi, cor, lacre, marca, modelo, motor, observacoes, placa, situacao, pericia, tipo_veiculo, procedimento_id) VALUES (v, 'Para-choque traseiro amassado', NULL, 'Cinza', 'LAC-' || ano || '-000316', 'Chevrolet', 'Onix', NULL, NULL, 'JKL2M34', 'EM_PATIO', 'PERICIA_CONCLUIDA', 'Carro', p3);
    INSERT INTO historico_situacao_veiculo (id, data_transicao, motivo, situacao_anterior, situacao_nova, responsavel_id, veiculo_id) VALUES (nextval('seq_historico_situacao_veiculo'), hoje - interval '29 days' + time '10:00', 'Cadastro do veículo apreendido', NULL, 'NA_DEPOL', u_cs, v);
    INSERT INTO historico_situacao_veiculo (id, data_transicao, motivo, situacao_anterior, situacao_nova, responsavel_id, veiculo_id) VALUES (nextval('seq_historico_situacao_veiculo'), hoje - interval '15 days' + time '09:00', 'Removido para o pátio municipal após a perícia', 'NA_DEPOL', 'EM_PATIO', u_vb, v);
    v := nextval('seq_veiculo'); INSERT INTO veiculo (id, caracteristicas_visuais, chassi, cor, lacre, marca, modelo, motor, observacoes, placa, situacao, pericia, tipo_veiculo, procedimento_id) VALUES (v, NULL, NULL, 'Azul', 'LAC-' || ano || '-000315', 'Yamaha', 'Fazer 250', NULL, NULL, 'MNO5P67', 'EM_PATIO', 'SEM_PERICIA', 'Moto', p13);
    INSERT INTO historico_situacao_veiculo (id, data_transicao, motivo, situacao_anterior, situacao_nova, responsavel_id, veiculo_id) VALUES (nextval('seq_historico_situacao_veiculo'), hoje - interval '40 days' + time '09:30', 'Cadastro do veículo apreendido', NULL, 'NA_DEPOL', u_ma, v);
    INSERT INTO historico_situacao_veiculo (id, data_transicao, motivo, situacao_anterior, situacao_nova, responsavel_id, veiculo_id) VALUES (nextval('seq_historico_situacao_veiculo'), hoje - interval '30 days' + time '14:00', 'Falta de espaço na DEPOL', 'NA_DEPOL', 'EM_PATIO', u_ma, v);
    v := nextval('seq_veiculo'); INSERT INTO veiculo (id, caracteristicas_visuais, chassi, cor, lacre, marca, modelo, motor, observacoes, placa, situacao, pericia, tipo_veiculo, procedimento_id) VALUES (v, NULL, NULL, 'Prata', 'LAC-' || ano || '-000314', 'Ford', 'Ka', NULL, NULL, 'DEF8G90', 'DEVOLVIDO', 'PERICIA_CONCLUIDA', 'Carro', p11);
    INSERT INTO historico_situacao_veiculo (id, data_transicao, motivo, situacao_anterior, situacao_nova, responsavel_id, veiculo_id) VALUES (nextval('seq_historico_situacao_veiculo'), hoje - interval '70 days' + time '10:00', 'Cadastro do veículo apreendido', NULL, 'NA_DEPOL', u_vb, v);
    INSERT INTO historico_situacao_veiculo (id, data_transicao, motivo, situacao_anterior, situacao_nova, responsavel_id, veiculo_id) VALUES (nextval('seq_historico_situacao_veiculo'), hoje - interval '12 days' + time '15:00', 'Devolvido ao proprietário mediante termo de restituição', 'NA_DEPOL', 'DEVOLVIDO', u_vb, v);
    -- o próximo veículo cadastrado pela API continua a numeração do lacre depois destes
    PERFORM setval('seq_lacre_veiculo', greatest((SELECT last_value FROM seq_lacre_veiculo), 321));

    -- ---------- notificações ----------
    INSERT INTO notificacao (id, data_criacao, lida, mensagem, referencia_id, tipo, usuario_destinatario_id) VALUES (nextval('seq_notificacao'), agora - interval '2 hours', false,
        'Você recebeu uma solicitação de repasse do procedimento IP 77/' || ano || ' de Marcos Andrade', rp7, 'REPASSE_RECEBIDO', u_vb);
    INSERT INTO notificacao (id, data_criacao, lida, mensagem, referencia_id, tipo, usuario_destinatario_id) VALUES (nextval('seq_notificacao'), agora - interval '1 day', false,
        'Você recebeu uma solicitação de repasse do procedimento BO 43/' || ano || ' de Carolina Silva', rp8, 'REPASSE_RECEBIDO', u_vb);
    INSERT INTO notificacao (id, data_criacao, lida, mensagem, referencia_id, tipo, usuario_destinatario_id) VALUES (nextval('seq_notificacao'), agora - interval '30 minutes', false,
        'Você recebeu uma solicitação de repasse do procedimento BO 98/' || ano || ' de Victória Barros', rp4, 'REPASSE_RECEBIDO', u_al);
    INSERT INTO notificacao (id, data_criacao, lida, mensagem, referencia_id, tipo, usuario_destinatario_id)
    SELECT nextval('seq_notificacao'), agora - interval '3 hours', false, 'Novo cadastro pendente de aprovação: João Novo', u_jn, 'CADASTRO_PENDENTE', u.id
    FROM usuario u JOIN role r ON r.id = u.role_id WHERE r.acesso = 'ROLE_ADMIN';

    RAISE NOTICE 'Dados de demonstração criados. Login: vbarros / Demo@2026';
END $$;
