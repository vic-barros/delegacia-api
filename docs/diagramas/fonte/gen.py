import html,json
LH,HEAD,PAD,FS=21,38,10,13.5
PAL={'lav':('#EEEDFB','#6B5FC7','#3D3591'),'pink':('#FBE9F1','#C2407A','#8A1F4F'),
     'mint':('#E3F4EE','#2E8B6A','#135C44'),'salmon':('#FAEAE5','#B5553A','#7A2E18'),
     'beige':('#FAEEDC','#A8742A','#6B4510'),'green':('#EAF6DF','#5E9E3A','#2F5E14'),
     'cyan':('#E3F4F7','#2A8FA3','#0F5563')}
W=2020; COLW=340; GAP=60; XS=[40+i*(COLW+GAP) for i in range(5)]

def build(title,tables,pos,edges,fname):
    B={}
    for k,(x,y,w) in pos.items():
        n=len(tables[k][1]); B[k]=dict(x=x,y=y,w=w,h=HEAD+2*PAD+n*LH)
    H=max(b['y']+b['h'] for b in B.values())+50
    s=[]
    def ln(pts,dash=False):
        d=' stroke-dasharray="6 4"' if dash else ''
        s.append('<polyline points="%s" fill="none" stroke="#555" stroke-width="1.3"%s/>'%(' '.join('%s,%s'%p for p in pts),d))
    def tx(x,y,t,cls='lbl',anchor='start',rot=None):
        r=f' transform="rotate({rot} {x} {y})"' if rot else ''
        s.append(f'<text x="{x}" y="{y}" class="{cls}" text-anchor="{anchor}"{r}>{html.escape(t)}</text>')
    for e in edges: e(B,ln,tx)
    for k,b in B.items():
        name,rows,pal=tables[k]; bg,bd,fg=PAL[pal]; x,y,w,h=b['x'],b['y'],b['w'],b['h']
        s.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="6" fill="{bg}" stroke="{bd}" stroke-width="1.5"/>')
        s.append(f'<line x1="{x}" y1="{y+HEAD}" x2="{x+w}" y2="{y+HEAD}" stroke="{bd}"/>')
        s.append(f'<text x="{x+w/2}" y="{y+25}" text-anchor="middle" class="head" fill="{fg}">{html.escape(name)}</text>')
        for i,(t,st) in enumerate(rows):
            cls={'pk':'b','fk':'b','n':'','i':'it'}[st]
            s.append(f'<text x="{x+14}" y="{y+HEAD+PAD+15+i*LH}" class="row {cls}" fill="{fg}">{html.escape(t)}</text>')
    svg=f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
<style>text{{font-family:'Liberation Sans',Arial,sans-serif}} .row{{font-size:{FS}px}} .b{{font-weight:bold}} .it{{font-style:italic}}
.head{{font-size:17px;font-weight:bold}} .lbl{{font-size:11px;font-style:italic;fill:#444}} .card{{font-size:12.5px;fill:#333}} .ttl{{font-size:22px;font-weight:bold;fill:#222}}</style>
<rect width="100%" height="100%" fill="#fff"/>{"".join(s)}</svg>'''
    open(fname+'.html','w').write(f'<!doctype html><html><head><meta charset="utf-8"><style>html,body{{margin:0}}</style></head><body>{svg}</body></html>')
    json.dump({'w':W,'h':H},open(fname+'.json','w'))

# ---------- edge helpers ----------
def horiz(a,b,dy,ca,cb,label,side_a='r',dash=False):
    """straight horizontal between box a (right side) and b (left side) at a.y+dy"""
    def f(B,ln,tx):
        A,Bb=B[a],B[b]; y=A['y']+dy
        if side_a=='r': x1,x2=A['x']+A['w'],Bb['x']
        else: x1,x2=A['x'],Bb['x']+Bb['w']
        ln([(x1,y),(x2,y)],dash)
        d=1 if x2>x1 else -1
        tx(x1+6*d,y-6,ca,'card','start' if d>0 else 'end'); tx(x2-6*d,y-6,cb,'card','end' if d>0 else 'start')
        if label: tx((x1+x2)/2,y+16,label,'lbl','middle')
    return f
def vert(a,b,ca,cb,label):
    def f(B,ln,tx):
        A,Bb=B[a],B[b]; x=A['x']+A['w']/2
        ln([(x,A['y']+A['h']),(x,Bb['y'])])
        tx(x+6,A['y']+A['h']+16,ca,'card'); tx(x+6,Bb['y']-6,cb,'card')
        if label: tx(x-8,(A['y']+A['h']+Bb['y'])/2+4,label,'lbl','end')
    return f
def chan(src,fx,tgt,tx_,lane,cs,ct,label,via=None):
    """src top at fraction fx -> up to lane y -> horizontal -> up into tgt bottom at x=tx_ (absolute).
       via=(side,gx,dy): leave src from side at y+dy, go to gutter x gx, then up."""
    def f(B,ln,tx):
        S,T=B[src],B[tgt]; ly=LANE0+lane*LSTEP; tb=T['y']+T['h']
        if via:
            side,gx,dy=via; sx=S['x'] if side=='l' else S['x']+S['w']; sy=S['y']+dy
            pts=[(sx,sy),(gx,sy),(gx,ly),(tx_,ly),(tx_,tb)]
            tx(sx+(-6 if side=='l' else 6),sy-6,cs,'card','end' if side=='l' else 'start')
            tx(gx+(6 if tx_>gx else -6),ly-5,label,'lbl','start' if tx_>gx else 'end')
        else:
            sx=S['x']+S['w']*fx; pts=[(sx,S['y']),(sx,ly),(tx_,ly),(tx_,tb)]
            tx(sx-5,S['y']-7,cs,'card','end'); tx(sx+5,S['y']-7,label,'lbl','start')
        ln(pts); tx(tx_+5,tb+15,ct,'card')
    return f
