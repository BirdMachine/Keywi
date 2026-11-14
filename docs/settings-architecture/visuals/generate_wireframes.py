"""Author exact, static SVG wireframes; no application code or network requests.
Run from any directory with Python 3. PNG rendering is documented in visual-design.md.
"""
from pathlib import Path
from html import escape
import math
OUT=Path(__file__).resolve().parent
BG='#141627'; SUR='#23263B'; TEXT='#F6F3FC'; MUTED='#BEC2DB'; EDGE='#8088A8'; PINK='#F4AAE3'; TEAL='#A6E5DE'; GOLD='#F6D98B'
class SVG:
 def __init__(self,w=390,h=960,title='Keywi concept'):
  self.w,self.h=w,h
  self.a=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}" role="img" aria-labelledby="title desc"><title id="title">{escape(title)}</title><desc id="desc">Documentation wireframe. Example values and decoration, not a running keyboard interface.</desc><defs><linearGradient id="chroma" x1="0" y1="1" x2="1" y2="0"><stop stop-color="#997AE9"/><stop offset=".5" stop-color="#F4AAE3"/><stop offset="1" stop-color="#F6D98B"/></linearGradient><linearGradient id="glass" x2="1" y2="1"><stop stop-color="#3B3656"/><stop offset="1" stop-color="#21263D"/></linearGradient></defs>']
 def rect(self,x,y,w,h,fill=SUR,stroke='none',rx=12,sw=1):self.a.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}"/>')
 def path(self,d,fill='none',stroke=EDGE,sw=1):self.a.append(f'<path d="{d}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}" stroke-linecap="round" stroke-linejoin="round"/>')
 def text(self,x,y,t,size=16,color=TEXT,weight=400,anchor='start'):
  self.a.append(f'<text x="{x}" y="{y}" font-family="DejaVu Sans, sans-serif" font-size="{size}" fill="{color}" font-weight="{weight}" text-anchor="{anchor}">{escape(t)}</text>')
 def line(self,x,y,x2,y2,c=EDGE,sw=1):self.path(f'M{x} {y} L{x2} {y2}',stroke=c,sw=sw)
 def circle(self,x,y,r,c,stroke='none',sw=1):self.a.append(f'<circle cx="{x}" cy="{y}" r="{r}" fill="{c}" stroke="{stroke}" stroke-width="{sw}"/>')
 def gem(self,x,y,c=PINK,r=10):
  self.path(f'M{x-r} {y-3} L{x-r/2} {y-r} L{x+r/2} {y-r} L{x+r} {y-3} L{x} {y+r} Z',stroke=c,sw=1.6)
  self.path(f'M{x-r} {y-3} L{x+r} {y-3} M{x-r/2} {y-r} L{x} {y+r} L{x+r/2} {y-r}',stroke=c)
 def petal(self,x,y,c=PINK):
  for a in [0,90,180,270]:
   self.a.append(f'<path d="M{x} {y} Q{x-18} {y-29} {x} {y-36} Q{x+18} {y-29} {x} {y}" fill="{c}" opacity=".22" transform="rotate({a} {x} {y})"/>')
  self.circle(x,y,5,c)
 def chevron(self,x,y):self.path(f'M{x-3} {y-5} l5 5 l-5 5',stroke=TEXT,sw=1.7)
 def chip(self,x,y,w,t,selected=False):
  self.rect(x,y,w,48,fill='#3E314D' if selected else SUR,stroke=PINK if selected else EDGE,rx=9)
  self.text(x+w/2,y+30,t,14,PINK if selected else TEXT,500,'middle')
 def button(self,x,y,w,t,primary=False):
  self.rect(x,y,w,48,PINK if primary else SUR,PINK if primary else EDGE,rx=8)
  self.text(x+w/2,y+30,t,15,BG if primary else TEXT,600,'middle')
 def start(self,sub='YOUR KEYBOARD, YOUR WAY',back=False,title='Keywi'):
  self.rect(0,0,self.w,self.h,BG,rx=28)
  self.text(24,29,'8:24',12,TEXT,600);self.text(366,29,'82%',12,MUTED,400,'end')
  if back:
   self.path('M33 61 l-8 8 l8 8 M25 69 h22',stroke=TEXT,sw=2)
   self.text(63,76,title,23,TEXT,600);self.text(24,112,sub,12,MUTED)
  else:
   self.text(24,86,title,31,TEXT,600);self.text(24,114,sub,12,MUTED)
   self.petal(342,80)
 def search(self,y=140):
  self.rect(24,y,342,48,SUR,EDGE,rx=10)
  self.circle(46,y+22,7,'none',MUTED,1.6);self.line(51,y+27,57,y+33,MUTED,1.6)
  self.text(70,y+30,'Find a control…',16,MUTED)
 def keyboard(self,x,y,w=310,h=115,selected=(),large=False):
  self.rect(x,y,w,h,fill='url(#glass)',stroke=EDGE,rx=10)
  gap=5;pad=9;inner=w-2*pad;rail=inner*.16;kw=(inner-rail-3*gap)/3;kh=(h-2*pad-3*gap)/4
  if large: kh=(h-2*pad-3*gap)/4
  for i,t in enumerate('ANIHORTES'):
   r,c=divmod(i,3);xx=x+pad+c*(kw+gap);yy=y+pad+r*(kh+gap);sel=i in selected
   self.rect(xx,yy,kw,kh,'url(#chroma)' if sel else '#292E49',PINK if sel else '#67708D',rx=5,sw=2 if sel else 1)
   self.text(xx+kw/2,yy+kh*.7,t,18 if large else 13,BG if sel else TEXT,600,'middle')
   if sel:self.circle(xx+kw-9,yy+8,3,BG)
  xx=x+pad+3*(kw+gap)
  for i,t in enumerate(['⇧','123','⌫']):
   self.rect(xx,y+pad+i*(kh+gap),rail,kh,'#30364E',EDGE,rx=5)
   self.text(xx+rail/2,y+pad+i*(kh+gap)+kh*.7,t,14 if large else 11,TEXT,400,'middle')
  self.rect(x+pad,y+pad+3*(kh+gap),3*kw+2*gap,kh,'#30364E',EDGE,rx=5)
  self.text(x+pad+(3*kw+2*gap)/2,y+pad+3*(kh+gap)+kh*.7,'space',13 if large else 10,MUTED,400,'middle')
  self.rect(xx,y+pad+3*(kh+gap),rail,kh,'#30364E',EDGE,rx=5)
  self.text(xx+rail/2,y+pad+3*(kh+gap)+kh*.7,'↵',14,TEXT,400,'middle')
 def row(self,y,title,sub='',accent=TEAL,h=62,index=None):
  if index is None:self.gem(38,y+27,accent,7)
  else:self.text(26,y+30,f'{index:02}',12,accent,600)
  self.text(60,y+25,title,17,TEXT,500)
  if sub:self.text(60,y+47,sub,12,MUTED)
  self.chevron(350,y+28);self.line(60,y+h-1,366,y+h-1,'#515872')
 def end(self,name):
  self.rect(151,self.h-18,88,4,TEXT,rx=2)
  self.a.append('</svg>');(OUT/name).write_text('\n'.join(self.a))

def home(kind):
 s=SVG(title=kind+' settings home')
 if kind=='Creative Studio':
  s.start('MAKE A LITTLE KEYBOARD MAGIC');s.search()
  s.text(24,224,'Your workbench',18,TEXT,500);s.text(366,224,'Custom',13,PINK,400,'end')
  s.keyboard(30,244,330,128);s.text(24,405,'CONTINUE EXPLORING',11,TEAL,600)
  s.chip(24,420,162,'Tap colors');s.chip(198,420,168,'Group fill')
  titles=[('Dress your keyboard','Themes, color & type'),('Shape your keyboard','Geometry, keys & boards'),('Tune your typing','Gestures & suggestions'),('Make it respond','Sound, touch & effects'),('Build your toolkit','Panels & palettes'),('Connect & automate','Tasks & theme routines')]
  for i,(t,sub) in enumerate(titles):
   x=24+(i%2)*176;y=492+(i//2)*112
   # A clipped petal edge, not a stack of standard cards.
   s.path(f'M{x+12} {y} H{x+151} Q{x+166} {y} {x+166} {y+16} V{y+75} Q{x+166} {y+98} {x+142} {y+98} H{x} V{y+12} Z',fill='url(#glass)',stroke=EDGE)
   s.gem(x+18,y+21,[PINK,TEAL,GOLD][i//2],7)
   # Intentionally wrap long titles on natural word boundaries.
   words={'Dress your keyboard':['Dress your','keyboard'],'Shape your keyboard':['Shape your','keyboard'],'Tune your typing':['Tune your typing'],'Make it respond':['Make it respond'],'Build your toolkit':['Build your toolkit'],'Connect & automate':['Connect &','automate']}[t]
   for j,line in enumerate(words):s.text(x+12,y+47+j*18,line,15,TEXT,500)
   s.text(x+12,y+85,sub,12,MUTED)
  s.row(833,'App & care','Power, backups & help',TEAL,h=62)
 elif kind=='Instrument & Controls':
  s.start('SHAPE · MAPPING · EXPRESSION');s.search()
  s.keyboard(36,210,318,110);s.text(24,348,'ME-Like',14,TEAL,600);s.text(366,348,'Main · Sound · Conserved',11,MUTED,400,'end')
  s.text(24,384,'CONTROL FAMILIES',11,PINK,600)
  titles=[('Keyboard body','Geometry, size & position'),('Keys & assignments','Mappings, boards & YAML'),('Surfaces & legends','Themes, paint & typography'),('Typing systems','Gestures, context & words'),('Toolbar & tools','Panels, strip & navigation'),('Feedback','Sound, haptics & effects'),('Connections','Tasker keys & routines'),('System','Power, backup & diagnostics')]
  for i,(t,sub) in enumerate(titles):s.row(400+i*61,t,sub,[TEAL,PINK,GOLD][i%3],h=61,index=i+1)
 else:
  s.start('A LITTLE KEYBOARD ATMOSPHERE');s.search()
  s.keyboard(36,211,318,112)
  s.text(24,350,'Custom',14,PINK,600);s.text(366,350,'Try typing  ›',14,TEAL,500,'end')
  s.text(24,386,'PINNED',11,TEAL,600);s.text(366,386,'Edit',13,MUTED,400,'end')
  s.chip(24,399,164,'Tap colors');s.chip(200,399,166,'Power mode')
  titles=[('Look & feel','Themes, surfaces & tap effects'),('Layout & keys','Geometry, mappings & groups'),('Typing','Gestures, words & field behavior'),('Feel & sound','Sound, vibration & feedback'),('Tools & connections','Panels, toolbar & Tasker'),('App & care','Power, backups & help')]
  for i,(t,sub) in enumerate(titles):s.row(473+i*65,t,sub,[PINK,TEAL,GOLD][i%3],h=65)
  s.text(24,906,'Recent: Suggestion chip color  ›',13,MUTED)
 s.end({'Creative Studio':'home-studio.svg','Instrument & Controls':'home-instrument.svg','Hybrid':'home-hybrid.svg'}[kind])
for k in ['Creative Studio','Instrument & Controls','Hybrid']:home(k)

s=SVG(title='Hybrid advanced Tap effects page')
s.start('LOOK & FEEL  /  TAP EFFECTS',True,'Tap effects');s.text(362,77,'☆',26,PINK,400,'end')
s.chip(24,137,164,'Flash & trails',True);s.chip(200,137,166,'Artwork')
s.keyboard(36,206,318,112,selected=[4]);s.text(24,346,'Try a key to see its fade',14,MUTED)
s.rect(24,368,342,66,'#343049',EDGE,rx=6);s.circle(44,390,4,GOLD);s.text(58,393,'Artwork paused by Restricted',14,GOLD,500);s.text(58,416,'Saved settings are kept.  Power options ›',12,TEXT)
s.text(24,470,'Fade duration',18,TEXT,500);s.rect(264,444,102,48,SUR,EDGE,rx=8);s.text(315,474,'600 ms',15,TEXT,500,'middle')
s.line(37,511,353,511,EDGE,5);s.line(37,511,147,511,PINK,5);s.circle(147,511,10,PINK,BG,3)
s.text(24,558,'Color frames',18,TEXT,500);s.text(366,558,'Edit order ›',13,TEAL,500,'end')
for i,c in enumerate([PINK,'#EA9EB5','#EDBD88',GOLD]):
 x=24+i*88;s.rect(x,580,78,58,c,EDGE,rx=7);s.text(x+39,615,str(i+1),18,BG,600,'middle')
s.text(24,665,'Colors play in order during one fade.',13,MUTED);s.text(24,689,'Add a frame  +',14,TEAL,500);s.text(366,689,'Use theme accent',12,MUTED,400,'end')
s.line(24,714,366,714,'#515872');s.text(24,750,'Drag trails',18,TEXT,500);s.text(24,774,'Uses these colors and fade time',13,MUTED)
s.rect(304,726,62,36,PINK,rx=18);s.circle(348,744,12,BG)
s.row(807,'Artwork placement & timing','Origin, layer, size & playback',TEAL,h=64)
s.button(24,890,162,'Preview fade');s.button(200,890,166,'Done',True)
s.end('advanced-tap-effects.svg')

s=SVG(title='Hybrid Keys and Groups editor')
s.start('LAYOUT & KEYS  /  KEYS & GROUPS',True,'Keys & groups');s.text(362,77,'☆',26,PINK,400,'end')
s.chip(24,137,164,'Style',True);s.chip(200,137,166,'Actions & values')
s.text(24,216,'ME-Like · English',14,TEAL,500);s.text(366,216,'Change ›',13,MUTED,400,'end')
s.chip(24,235,106,'Main',True);s.chip(140,235,106,'Shift');s.chip(256,235,110,'Numeric')
s.keyboard(24,302,342,200,selected=[0,2,6,8],large=True)
s.text(24,529,'4 keys selected',16,PINK,600);s.text(366,529,'Clear · Select all',13,TEAL,400,'end')
s.text(24,557,'Multiple keys  ✓',14,TEXT);s.text(366,557,'Key list ›',13,TEAL,500,'end')
s.rect(24,582,342,48,SUR,EDGE,rx=8);s.text(38,612,'Group: Petals',16,TEXT,500);s.text(350,612,'Saved groups ›',12,MUTED,400,'end')
s.text(24,658,'Applies to Main mode only.',14,TEXT,500);s.text(24,681,'Individual key overrides take priority.',13,MUTED)
s.row(702,'Fill','Gradient · across selected keys',PINK,h=57)
s.row(762,'Border & glyph','Inherit · follows group / theme',TEAL,h=57)
s.text(24,849,'Share inherited gradients & borders',13,TEXT)
s.rect(306,829,60,34,PINK,rx=17);s.circle(348,846,11,BG)
s.button(24,890,162,'Reset this mode');s.button(200,890,166,'Apply group style',True)
s.end('keys-groups.svg')

# SVG comparison sheets reference embedded source groups, not external assets.
from xml.etree import ElementTree as ET

def sheet(names,labels,filename):
 w=40+len(names)*410;h=1060
 a=[f'<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="{w}" height="{h}" viewBox="0 0 {w} {h}"><title>Keywi settings architecture wireframes</title><rect width="100%" height="100%" fill="#0B0D19"/>']
 for i,(n,l) in enumerate(zip(names,labels)):
  x=30+i*410
  a.append(f'<text x="{x}" y="32" fill="{TEXT}" font-family="DejaVu Sans, sans-serif" font-size="17">{escape(l)}</text>')
  # Nested SVG establishes a local namespace for gradient IDs in each viewport.
  root=(OUT/n).read_text().replace('<svg xmlns="http://www.w3.org/2000/svg"',f'<svg x="{x}" y="54" xmlns="http://www.w3.org/2000/svg"',1)
  # Unique IDs also avoid renderer cross-resolution between nested SVGs.
  for id in ['title','desc','chroma','glass']:
   root=root.replace('id="'+id+'"','id="'+str(i)+id+'"').replace('url(#'+id+')','url(#'+str(i)+id+')')
  root=root.replace('aria-labelledby="title desc"',f'aria-labelledby="{i}title {i}desc"')
  a.append(root)
 a.append(f'<text x="30" y="1042" fill="{MUTED}" font-family="DejaVu Sans, sans-serif" font-size="13">ISSUE #63 · STATIC DESIGN EXPLORATION · EXAMPLE VALUES · 390 × 960 LOGICAL PIXELS PER SCREEN</text></svg>')
 (OUT/filename).write_text('\n'.join(a))
sheet(['home-studio.svg','home-instrument.svg','home-hybrid.svg'],['A · Creative Studio','B · Instrument & Controls','C · Hybrid'],'home-comparison.svg')
sheet(['advanced-tap-effects.svg','keys-groups.svg'],['Hybrid · Advanced Tap effects','Hybrid · Keys & groups'],'editor-comparison.svg')
print('Wrote five dark mobile wireframes and two comparison sheets')

# Light counterpart: same hierarchy and controls, different contrast tokens.
s=(OUT/'home-hybrid.svg').read_text()
for old,new in [(BG,'#F7F3FA'),(SUR,'#E7E6F1'),(TEXT,'#202037'),(MUTED,'#535A76'),(EDGE,'#727995'),(PINK,'#7C286F'),(TEAL,'#146C69'),(GOLD,'#735820'),('#515872','#B3B7CA'),('#292E49','#E0DFEF'),('#30364E','#E8E7F0'),('#67708D','#858DA4'),('#3B3656','#E7DCF0'),('#21263D','#DDE9F1')]:
 s=s.replace(old,new)
s=s.replace('Hybrid settings home','Hybrid settings home, light appearance')
(OUT/'home-hybrid-light.svg').write_text(s)
