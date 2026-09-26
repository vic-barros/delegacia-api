import html,json
PAL={'lav':('#EEEDFB','#6B5FC7','#3D3591'),'mint':('#E3F4EE','#2E8B6A','#135C44'),'salmon':('#FAEAE5','#B5553A','#7A2E18'),'beige':('#FAEEDC','#A8742A','#6B4510')}
def diagram(fname,W,H,title,box,actors,ucs,links,gens=(),incs=()):
    s=[]; e=html.escape
    s.append(f'<rect x="{box[0]}" y="{box[1]}" width="{box[2]}" height="{box[3]}" rx="10" fill="none" stroke="#bbb" stroke-dasharray="6 4" stroke-width="1.3"/>')
    s.append(f'<text x="{box[0]+20}" y="{box[1]+28}" class="sys">{e(title)}</text>')
    A={}
    for k,(x,y,lab,sub) in actors.items():
        A[k]=(x,y)
        s.append(f'<g stroke="#555" stroke-width="3" fill="none"><circle cx="{x}" cy="{y}" r="28"/><line x1="{x}" y1="{y+28}" x2="{x}" y2="{y+100}"/><line x1="{x-46}" y1="{y+50}" x2="{x+46}" y2="{y+50}"/><line x1="{x}" y1="{y+100}" x2="{x-38}" y2="{y+150}"/><line x1="{x}" y1="{y+100}" x2="{x+38}" y2="{y+150}"/></g>')
        s.append(f'<text x="{x}" y="{y+176}" class="act" text-anchor="middle">{e(lab)}</text>')
        if sub: s.append(f'<text x="{x}" y="{y+200}" class="act it" text-anchor="middle">{e(sub)}</text>')
    U={}
    for k,(cx,cy,rx,lab,pal) in ucs.items():
        U[k]=(cx,cy,rx); bg,bd,fg=PAL[pal]
        s.append(f'<ellipse cx="{cx}" cy="{cy}" rx="{rx}" ry="46" fill="{bg}" stroke="{bd}" stroke-width="1.3"/>')
        s.append(f'<text x="{cx}" y="{cy+8}" class="uc" text-anchor="middle" fill="{fg}">{e(lab)}</text>')
    L=[]
    for a,u in links:
        x,y=A[a]; cx,cy,rx=U[u]
        L.append(f'<line x1="{x+50}" y1="{y+55}" x2="{cx-rx}" y2="{cy}" stroke="#999" stroke-width="1.5"/>')
    for c,p in gens:
        (x1,y1),(x2,y2)=A[c],A[p]; ty=y2+212; tx=x2+(12 if x1>x2 else -12)
        import math; ang=math.atan2(ty-(y1-28),tx-x1); L_=16
        p1=(tx-L_*math.cos(ang-0.4),ty-L_*math.sin(ang-0.4)); p2=(tx-L_*math.cos(ang+0.4),ty-L_*math.sin(ang+0.4))
        bx,by=(p1[0]+p2[0])/2,(p1[1]+p2[1])/2
        L.append(f'<line x1="{x1}" y1="{y1-28}" x2="{bx}" y2="{by}" stroke="#555" stroke-width="1.8"/><polygon points="{tx},{ty} {p1[0]},{p1[1]} {p2[0]},{p2[1]}" fill="#fff" stroke="#555" stroke-width="1.8"/>')
    for a,b in incs:
        ax,ay,_=U[a]; bx,by,_=U[b]; x=bx if abs(bx-ax)<150 else ax
        L.append(f'<line x1="{x}" y1="{ay+46}" x2="{x}" y2="{by-47}" stroke="#666" stroke-width="1.4" stroke-dasharray="6 4"/><polyline points="{x-6},{by-57} {x},{by-47} {x+6},{by-57}" fill="none" stroke="#666" stroke-width="1.4"/>')
        L.append(f'<text x="{x+12}" y="{(ay+by)/2+5}" class="inc">«include»</text>')
    svg=f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}"><style>text{{font-family:'Liberation Sans',Arial,sans-serif}}
.sys{{font-size:19px;fill:#555}} .act{{font-size:20px;fill:#555}} .it{{font-style:italic}} .uc{{font-size:23px}} .inc{{font-size:18px;fill:#555}}</style>
<rect width="100%" height="100%" fill="#fff"/>{"".join(L)}{"".join(s)}</svg>'''
    open(fname+'.html','w').write(f'<!doctype html><html><head><meta charset="utf-8"><style>html,body{{margin:0}}</style></head><body>{svg}</body></html>')
    json.dump({'w':W,'h':H},open(fname+'.json','w'))

diagram('uc_auth',1360,900,'Sistema - Autenticacao e Usuarios',(460,40,860,820),
 {'usr':(140,90,'Usuario','(cadastrado)'),'novo':(140,370,'Novo usuario',None),'adm':(140,640,'Admin',None)},
 {'auth':(880,130,240,'Autenticar usuario','mint'),'sol':(880,300,240,'Solicitar cadastro','lav'),
  'apr':(880,470,280,'Aprovar / rejeitar cadastro','salmon'),'ger':(880,630,240,'Gerenciar usuarios','salmon'),'red':(880,790,260,'Redefinir senha de usuario','salmon')},
 [('usr','auth'),('novo','sol'),('adm','apr'),('adm','ger'),('adm','red')])
diagram('uc_veic',1400,1000,'Sistema - Veiculos Apreendidos',(500,30,870,950),
 {'srv':(200,80,'Servidor','(abstrato)'),'pol':(100,420,'Policial',None),'del':(300,420,'Delegado',None),'est':(200,700,'Estagiario',None)},
 {'cad':(930,110,290,'Cadastrar veiculo apreendido','lav'),'lac':(1010,250,250,'Gerar lacre automatico','beige'),
  'sit':(930,400,290,'Alterar situacao do veiculo','mint'),'his':(1010,540,290,'Registrar historico de situacao','beige'),
  'edi':(930,700,270,'Editar dados e pericia','salmon'),'con':(930,880,300,'Consultar veiculos e historico','lav')},
 [('srv','cad'),('srv','sit'),('srv','edi'),('srv','con'),('est','con')],
 gens=[('pol','srv'),('del','srv')],incs=[('cad','lac'),('sit','his')])
