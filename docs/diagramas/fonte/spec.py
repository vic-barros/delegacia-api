import gen
from gen import *
gen.LANE0,gen.LSTEP=400,18
Y1,Y2=40,590
def P(t):return (t,'pk')
def F(t):return (t,'fk')
def N(t):return (t,'n')
def I(t):return (t,'i')

DER={
'procedimento':('procedimento',[P('PK id: bigint'),N('tipo: varchar (enum TipoProcedimento)'),I('(BO, IP, TCO, APF, AIAI, AAFAI, ROP)'),N('numero: bigint'),N('ano: integer'),N('crime: varchar'),N('data_abertura: date'),N('data_remessa_final: date'),N('protocolo_remessa_final: varchar'),N('status: varchar'),I('(EM_ANDAMENTO, ARQUIVADO)'),F('FK detentor_atual_id: bigint'),I('UNIQUE (tipo, numero, ano)')],'lav'),
'usuario':('usuario',[P('PK id: bigint'),N('nome: varchar'),N('matricula: varchar (UNIQUE)'),N('login: varchar (UNIQUE)'),N('senha_hash: varchar'),N('status: varchar'),I('(PENDENTE, APROVADO, REJEITADO, DESATIVADO)'),F('FK role_id: bigint')],'mint'),
'role':('role',[P('PK id: bigint'),N('acesso: varchar (UNIQUE)'),I('ex.: ROLE_ADMIN, ROLE_DELEGADO,'),I('ROLE_POLICIAL, ROLE_ESTAGIARIO'),N('descricao: varchar')],'pink'),
'oitiva':('oitiva',[P('PK id: bigint'),N('data_hora: timestamp'),N('status: varchar'),I('(AGENDADA, CONCLUIDA, DESMARCADA)'),N('motivo_cancelamento: varchar'),N('data_cadastro: timestamp'),F('FK procedimento_id: bigint'),F('FK responsavel_id: bigint'),F('FK cadastrado_por_id: bigint')],'mint'),
'oitiva_parte':('oitiva_parte',[P('PK id: bigint'),N('tipo_parte: varchar'),I('(INVESTIGADO, TESTEMUNHA, VITIMA)'),N('nome_parte: varchar'),F('FK oitiva_id: bigint')],'mint'),
'repasse':('repasse',[P('PK id: bigint'),N('status: varchar'),I('(PENDENTE, ACEITO, RECUSADO)'),N('justificativa_recusa: varchar'),N('data_solicitacao: timestamp'),N('data_resposta: timestamp'),F('FK procedimento_id: bigint'),F('FK solicitante_id: bigint'),F('FK destinatario_id: bigint'),I('CHECK (solicitante_id <> destinatario_id)')],'salmon'),
'hc':('historico_custodia',[P('PK id: bigint'),N('data_inicio: timestamp'),N('data_fim: timestamp (null)'),N('origem: varchar'),I('(CADASTRO_INICIAL, REPASSE)'),F('FK procedimento_id: bigint'),F('FK usuario_id: bigint'),F('FK repasse_origem_id: bigint (null)')],'salmon'),
'hsp':('historico_status_procedimento',[P('PK id: bigint'),N('status_anterior: varchar'),N('status_novo: varchar'),N('motivo: varchar'),N('data_transicao: timestamp'),F('FK procedimento_id: bigint'),F('FK responsavel_id: bigint')],'lav'),
'notif':('notificacao',[P('PK id: bigint'),N('tipo: varchar (enum TipoNotificacao)'),N('mensagem: varchar'),N('lida: boolean'),N('data_criacao: timestamp'),F('FK usuario_destinatario_id: bigint')],'beige'),
'veiculo':('veiculo',[P('PK id: bigint'),N('tipo_veiculo: varchar'),N('lacre: varchar (UNIQUE)'),I('gerado: LAC-AAAA-NNNNNN'),N('marca: varchar'),N('modelo: varchar'),N('cor: varchar'),N('placa: varchar (null)'),N('chassi: varchar (null)'),N('motor: varchar'),N('caracteristicas_visuais: text'),N('pericia: varchar (enum Pericia)'),N('situacao: varchar (enum SituacaoVeiculo)'),I("DEFAULT 'NA_DEPOL'"),N('localizacao_patio: varchar (null)'),N('observacoes: text'),F('FK procedimento_id: bigint')],'green'),
'hsv':('historico_situacao_veiculo',[P('PK id: bigint'),N('situacao_anterior: varchar'),N('situacao_nova: varchar'),I('(NA_DEPOL, EM_PATIO,'),I(' DEVOLVIDO, DESCARTADO)'),N('motivo: varchar'),N('data_transicao: timestamp'),F('FK veiculo_id: bigint'),F('FK responsavel_id: bigint')],'cyan'),
}
CLS={
'procedimento':('Procedimento',[N('id: Long'),N('tipo: TipoProcedimento'),I('(BO, IP, TCO, APF, AIAI, AAFAI, ROP)'),N('numero: Long'),N('ano: Integer'),N('crime: String'),N('dataAbertura: LocalDate'),N('dataRemessaFinal: LocalDate'),N('protocoloRemessaFinal: String'),N('status: StatusProcedimento'),I('(EM_ANDAMENTO, ARQUIVADO)')],'lav'),
'usuario':('Usuario',[N('id: Long'),N('nome: String'),N('matricula: String'),N('login: String'),N('senhaHash: String'),N('status: StatusUsuario'),I('(PENDENTE, APROVADO, REJEITADO, DESATIVADO)')],'mint'),
'role':('Role',[N('id: Long'),N('acesso: String'),I('ex.: ROLE_ADMIN, ROLE_DELEGADO,'),I('ROLE_POLICIAL, ROLE_ESTAGIARIO'),N('descricao: String'),I('implements GrantedAuthority')],'pink'),
'oitiva':('Oitiva',[N('id: Long'),N('dataHora: LocalDateTime'),N('status: StatusOitiva'),I('(AGENDADA, CONCLUIDA, DESMARCADA)'),N('motivoCancelamento: String'),N('dataCadastro: LocalDateTime')],'mint'),
'oitiva_parte':('OitivaParte',[N('id: Long'),N('tipoParte: TipoParte'),I('(INVESTIGADO, TESTEMUNHA, VITIMA)'),N('nomeParte: String')],'mint'),
'repasse':('Repasse',[N('id: Long'),N('status: StatusRepasse'),I('(PENDENTE, ACEITO, RECUSADO)'),N('justificativaRecusa: String'),N('dataSolicitacao: LocalDateTime'),N('dataResposta: LocalDateTime')],'salmon'),
'hc':('HistoricoCustodia',[N('id: Long'),N('dataInicio: LocalDateTime'),N('dataFim: LocalDateTime'),N('origem: OrigemCustodia'),I('(CADASTRO_INICIAL, REPASSE)')],'salmon'),
'hsp':('HistoricoStatusProcedimento',[N('id: Long'),N('statusAnterior: StatusProcedimento'),N('statusNovo: StatusProcedimento'),N('motivo: String'),N('dataTransicao: LocalDateTime')],'lav'),
'notif':('Notificacao',[N('id: Long'),N('tipo: TipoNotificacao'),N('mensagem: String'),N('lida: boolean'),N('dataCriacao: LocalDateTime')],'beige'),
'veiculo':('Veiculo',[N('id: Long'),N('tipoVeiculo: String'),N('lacre: String'),N('marca: String'),N('modelo: String'),N('cor: String'),N('placa: String'),N('chassi: String'),N('motor: String'),N('caracteristicasVisuais: String'),N('pericia: Pericia'),I('(SEM_PERICIA, PERICIA_EM_ANDAMENTO,'),I(' PERICIA_CONCLUIDA, NAO_PRECISA_PERICIA)'),N('situacao: SituacaoVeiculo'),I('(NA_DEPOL, EM_PATIO,'),I(' DEVOLVIDO, DESCARTADO)'),N('localizacaoPatio: String'),N('observacoes: String')],'green'),
'hsv':('HistoricoSituacaoVeiculo',[N('id: Long'),N('situacaoAnterior: SituacaoVeiculo'),N('situacaoNova: SituacaoVeiculo'),I('(NA_DEPOL, EM_PATIO,'),I(' DEVOLVIDO, DESCARTADO)'),N('motivo: String'),N('dataTransicao: LocalDateTime')],'cyan'),
}
def layout(T):
    h=lambda k:HEAD+2*PAD+len(T[k][1])*LH
    Y3=Y2+max(h(k) for k in ['oitiva','repasse','hc','hsp','notif'])+70
    return {'procedimento':(240,Y1,460),'usuario':(960,Y1,440),'role':(1580,Y1,380),
     'oitiva':(XS[0],Y2,COLW),'repasse':(XS[1],Y2,COLW),'hc':(XS[2],Y2,COLW),'hsp':(XS[3],Y2,COLW),'notif':(XS[4],Y2,COLW),
     'oitiva_parte':(XS[0],Y3,COLW),'veiculo':(XS[1],Y3,COLW),'hsv':(XS[2],Y3,COLW)}
def rep_hc(c,label):
    def f(B,ln,tx):
        A,Bb=B['repasse'],B['hc']; y=A['y']+150; x1,x2=A['x']+A['w'],Bb['x']
        ln([(x1,y),(x2,y)],True); tx(x1+4,y-6,c,'card'); tx(x2-4,y+16,c,'card','end')
        tx((x1+x2)/2+4,y+22,label,'lbl','start',90)
    return f
def edges(one,many,L):
    return [
     horiz('procedimento','usuario',70,many,one,L['detentor']),
     horiz('role','usuario',70,one,many,L['role'],side_a='l'),
     vert('oitiva','oitiva_parte',one,('1..*' if many=='0..*' else many),L['oitiva']),
     horiz('veiculo','hsv',60,one,many,L['veiculo']),
     rep_hc('0..1',L['repasseOrigem']),
     chan('notif',.5,'usuario',1370,0,many,one,L['usuarioDestinatario']),
     chan('oitiva',.5,'usuario',980,0,many,one,L['responsavel']),
     chan('oitiva',.85,'usuario',1010,1,many,one,L['cadastradoPor']),
     chan('hsp',.5,'usuario',1300,1,many,one,L['responsavel']),
     chan('repasse',.5,'usuario',1040,2,many,one,L['solicitante']),
     chan('repasse',.85,'usuario',1070,3,many,one,L['destinatario']),
     chan('hc',.5,'usuario',1100,4,many,one,L['usuario']),
     chan('hsp',.15,'procedimento',670,5,many,one,L['procedimento']),
     chan('hc',.15,'procedimento',600,6,many,one,L['procedimento']),
     chan('oitiva',.15,'procedimento',280,7,many,one,L['procedimento']),
     chan('repasse',.15,'procedimento',XS[1]+COLW*.15,7,many,one,L['procedimento']),
     chan('veiculo',0,'procedimento',370,8,many,one,L['procedimento'],via=('l',410,24)),
     chan('hsv',0,'usuario',1210,8,many,one,L['responsavel'],via=('r',1210,24)),
    ]
LD={k:k for k in []}
LD=dict(detentor='detentor_atual_id',role='role_id',oitiva='oitiva_id',veiculo='veiculo_id',repasseOrigem='repasse_origem_id',usuarioDestinatario='usuario_destinatario_id',
 responsavel='responsavel_id',cadastradoPor='cadastrado_por_id',solicitante='solicitante_id',destinatario='destinatario_id',usuario='usuario_id',procedimento='procedimento_id')
LC=dict(detentor='detentorAtual',role='role',oitiva='oitiva',veiculo='veiculo',repasseOrigem='repasseOrigem',usuarioDestinatario='usuarioDestinatario',
 responsavel='responsavel',cadastradoPor='cadastradoPor',solicitante='solicitante',destinatario='destinatario',usuario='usuario',procedimento='procedimento')
build('DER',DER,layout(DER),edges('1','N',LD),'der')
build('Classes',CLS,layout(CLS),edges('1','0..*',LC),'classes')
