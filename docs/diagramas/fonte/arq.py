import html, json

# Diagrama de arquitetura em camadas (Figura 8 do docx). Coordenadas em px CSS; o render.js amplia 1.5x
W, H = 960, 1067
PAL = {'mint': ('#E3F4EE', '#2E8B6A', '#135C44', '#1F7A5A'), 'salmon': ('#FAEAE5', '#B5553A', '#7A2E18', '#9A3F24'),
       'lav': ('#EEEDFB', '#6B5FC7', '#3D3591', '#5547B8')}
e = html.escape
s = []


def grupo(x, y, w, h, rotulo):
    s.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="10" fill="none" stroke="#bbb" '
             f'stroke-dasharray="6 4" stroke-width="1.2"/>')
    s.append(f'<text x="{x + 16}" y="{y + 25}" class="grp">{e(rotulo)}</text>')


def caixa(x, y, w, h, titulo, linhas, pal, italico_ultima=False):
    bg, bd, fg, fg2 = PAL[pal]
    s.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="5" fill="{bg}" stroke="{bd}" stroke-width="1.2"/>')
    cx = x + w / 2
    s.append(f'<text x="{cx}" y="{y + 38}" class="tit" fill="{fg}" text-anchor="middle">{e(titulo)}</text>')
    for i, t in enumerate(linhas):
        cls = 'sub it' if italico_ultima and i == len(linhas) - 1 else 'sub'
        s.append(f'<text x="{cx}" y="{y + 66 + i * 21}" class="{cls}" fill="{fg2}" text-anchor="middle">{e(t)}</text>')


def seta(x, y1, y2):
    s.append(f'<line x1="{x}" y1="{y1}" x2="{x}" y2="{y2}" stroke="#555" stroke-width="1.3"/>'
             f'<polyline points="{x - 5},{y2 - 8} {x},{y2} {x + 5},{y2 - 8}" fill="none" stroke="#555" stroke-width="1.3"/>')


def texto(x, y, t, cls='nota'):
    s.append(f'<text x="{x}" y="{y}" class="{cls}">{e(t)}</text>')


grupo(53, 27, 854, 160, 'Cliente (navegador)')
caixa(293, 67, 374, 93, 'React (SPA, Vite)', ['Telas, chamadas fetch com o token, rotas protegidas'], 'mint')
seta(480, 187, 240)
texto(500, 212, 'HTTP / JSON (REST)', 'nota')
texto(500, 229, 'Authorization: Bearer <JWT>', 'nota it')

grupo(53, 247, 854, 746, 'Servidor (Spring Boot)')
caixa(187, 287, 586, 100, 'Spring Security - OAuth2 Resource Server (JWT)',
      ['Valida token HS256, extrai papel (role), autoriza por endpoint',
       'Intercepta toda requisicao antes do Controller'], 'salmon', italico_ultima=True)
seta(480, 387, 434)
caixa(187, 440, 586, 93, 'Camada de Apresentacao - Controllers (REST)',
      ['Recebe requisicao, valida entrada (Bean Validation), retorna DTO ou arquivo'], 'lav')
seta(480, 533, 580)
caixa(187, 587, 586, 93, 'Camada de Servico - Service',
      ['Regras de negocio: custodia e repasse, oitivas, veiculos, avisos, exportacao'], 'lav')
seta(480, 680, 727)
caixa(187, 733, 586, 93, 'Camada de Persistencia - Repository',
      ['Spring Data JPA - interfaces de repositorio por entidade'], 'lav')
seta(480, 827, 874)
texto(500, 854, 'SQL via Hibernate', 'nota it')
caixa(293, 880, 374, 93, 'PostgreSQL', ['usuario, procedimento, oitiva, repasse, veiculo...'], 'mint')

texto(69, 1012, 'Legenda: setas indicam direcao da chamada/dependencia entre camadas.', 'nota it')
texto(69, 1033, 'Autenticacao stateless (sem sessao no servidor) - RNF01/RNF02.', 'nota it')

svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}"><style>
text{{font-family:'Liberation Sans',Arial,sans-serif}} .grp{{font-size:14.5px;fill:#555;font-style:italic}}
.tit{{font-size:17px;font-weight:bold}} .sub{{font-size:14px}} .it{{font-style:italic}} .nota{{font-size:14.5px;fill:#555}}
</style><rect width="100%" height="100%" fill="#fff"/>{"".join(s)}</svg>'''
open('arq.html', 'w').write(f'<!doctype html><html><head><meta charset="utf-8"><style>html,body{{margin:0}}</style>'
                           f'</head><body>{svg}</body></html>')
json.dump({'w': W, 'h': H}, open('arq.json', 'w'))
