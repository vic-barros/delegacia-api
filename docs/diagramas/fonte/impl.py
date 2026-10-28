import html, json

# Diagrama de implantação local (seção 8 do docx). Mesmo estilo do arq.py; o render.js amplia 1.5x
W, H = 1220, 860
PAL = {'mint': ('#E3F4EE', '#2E8B6A', '#135C44', '#1F7A5A'), 'salmon': ('#FAEAE5', '#B5553A', '#7A2E18', '#9A3F24'),
       'lav': ('#EEEDFB', '#6B5FC7', '#3D3591', '#5547B8'), 'cinza': ('#F3F3F3', '#888888', '#333333', '#555555')}
e = html.escape
s = []


def grupo(x, y, w, h, rotulo):
    s.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="10" fill="none" stroke="#bbb" '
             f'stroke-dasharray="6 4" stroke-width="1.2"/>')
    s.append(f'<text x="{x + 16}" y="{y + 25}" class="grp">{e(rotulo)}</text>')


def caixa(x, y, w, h, titulo, linhas, pal, tracejada=False):
    bg, bd, fg, fg2 = PAL[pal]
    traco = ' stroke-dasharray="6 4"' if tracejada else ''
    s.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="5" fill="{bg}" stroke="{bd}" stroke-width="1.2"{traco}/>')
    cx = x + w / 2
    s.append(f'<text x="{cx}" y="{y + 34}" class="tit" fill="{fg}" text-anchor="middle">{e(titulo)}</text>')
    for i, t in enumerate(linhas):
        s.append(f'<text x="{cx}" y="{y + 60 + i * 21}" class="sub" fill="{fg2}" text-anchor="middle">{e(t)}</text>')


def seta(pontos, tracejada=False):
    """Linha (poligonal) com ponta de seta no último ponto; só segmentos horizontais ou verticais."""
    traco = ' stroke-dasharray="6 4"' if tracejada else ''
    pts = ' '.join(f'{x},{y}' for x, y in pontos)
    s.append(f'<polyline points="{pts}" fill="none" stroke="#555" stroke-width="1.3"{traco}/>')
    (x1, y1), (x2, y2) = pontos[-2], pontos[-1]
    if x1 == x2:
        d = 8 if y2 > y1 else -8
        ponta = f'{x2 - 5},{y2 - d} {x2},{y2} {x2 + 5},{y2 - d}'
    else:
        d = 8 if x2 > x1 else -8
        ponta = f'{x2 - d},{y2 - 5} {x2},{y2} {x2 - d},{y2 + 5}'
    s.append(f'<polyline points="{ponta}" fill="none" stroke="#555" stroke-width="1.3"/>')


def texto(x, y, t, cls='nota'):
    s.append(f'<text x="{x}" y="{y}" class="{cls}">{e(t)}</text>')


grupo(30, 20, 880, 790, 'Rede interna da delegacia')
caixa(250, 60, 440, 90, 'Computadores da delegacia (navegador)', ['Acesso pelo endereço interno do servidor'], 'mint')
seta([(470, 150), (470, 236)])
texto(485, 196, 'HTTPS (porta 443)', 'nota it')

grupo(60, 240, 820, 380, 'Servidor da delegacia - Docker Compose (reinicia sozinho)')
caixa(90, 290, 220, 135, 'front (Nginx)', ['app Expo (web) compilado', 'HTTPS: porta 443', 'repassa /api para a API'], 'mint')
caixa(360, 290, 220, 135, 'api (Spring Boot)', ['Java 21 embutido', 'porta 8080 só interna', 'segredos lidos do .env'], 'lav')
caixa(630, 290, 220, 135, 'banco (PostgreSQL)', ['porta 5432 não exposta', 'acesso só pela API'], 'salmon')
seta([(310, 357), (352, 357)])
seta([(580, 357), (622, 357)])

caixa(90, 480, 220, 110, '.env (segredos)', ['senha do banco, JWT_SECRET', 'fica fora do Git'], 'cinza')
caixa(360, 480, 220, 110, 'volume: arquivos', ['fotos da apreensão', '(fora do banco)'], 'cinza')
caixa(630, 480, 220, 110, 'volume: dados', ['arquivos do PostgreSQL', 'preservados na atualização'], 'cinza')
seta([(470, 425), (470, 472)])
seta([(740, 425), (740, 472)])

caixa(90, 660, 490, 110, 'Atualização', ['docker compose pull && docker compose up -d',
                                          'sem internet: docker save / docker load (pendrive)'], 'cinza')
caixa(630, 660, 220, 110, 'Backup diário', ['pg_dump para HD externo', 'ou servidor da regional'], 'cinza')
seta([(740, 590), (740, 652)])

# Integração opcional com IA (único tráfego para fora da rede interna)
caixa(960, 290, 230, 135, 'IA externa (opcional)', ['Gemini ou modelo local', 'só dados minimizados', 'chave só no servidor'],
      'cinza', tracejada=True)
seta([(470, 290), (470, 268), (1075, 268), (1075, 282)], tracejada=True)
texto(930, 260, 'HTTPS (internet)', 'nota it')

texto(30, 838, 'Legenda: setas = comunicação/dependência; tracejado = opcional, depende de internet. '
               'Sem a IA, o sistema funciona só na rede interna.', 'nota it')

svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}"><style>
text{{font-family:'Liberation Sans',Arial,sans-serif}} .grp{{font-size:14.5px;fill:#555;font-style:italic}}
.tit{{font-size:17px;font-weight:bold}} .sub{{font-size:14px}} .it{{font-style:italic}} .nota{{font-size:14.5px;fill:#555}}
</style><rect width="100%" height="100%" fill="#fff"/>{"".join(s)}</svg>'''
open('impl.html', 'w').write(f'<!doctype html><html><head><meta charset="utf-8"><style>html,body{{margin:0}}</style>'
                            f'</head><body>{svg}</body></html>')
json.dump({'w': W, 'h': H}, open('impl.json', 'w'))
