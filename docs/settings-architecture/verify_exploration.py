"""Validate this source-pinned design package, without building or modifying Keywi."""
from pathlib import Path
import csv
import re
import subprocess
import struct
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[2]
DOC=Path(__file__).resolve().parent
BASE='8ef014017a020346da3d3e2f8922bb184a51688b'

def source(path):
 return subprocess.check_output(['git','show',f'{BASE}:{path}'],cwd=ROOT,text=True)

rows=list(csv.DictReader((DOC/'inventory.csv').open()))
assert len(rows)==275
assert len({r['id'] for r in rows})==len(rows)
inventory=(DOC/'inventory.md').read_text()
trees=(DOC/'alternatives.md').read_text()
cache={}
for r in rows:
 assert all(v for v in r.values()),r['id']
 assert r['id'] in inventory,r['id']
 if r['source'] not in cache:cache[r['source']]=source(r['source']).splitlines()
 assert 1<=int(r['source_line'])<=len(cache[r['source']]),r
for field in ['creative_studio','instrument_controls','hybrid']:
 assert len({r[field] for r in rows})==39
# Expand each ID range appearing in the three trees, checking each ID in each tree.
for marker,next_marker in [('### A · Creative Studio navigation','### B · Instrument & Controls navigation'),('### B · Instrument & Controls navigation','### C · Hybrid navigation'),('### C · Hybrid navigation','## Shared editor')]:
 text=trees.split(marker,1)[1].split(next_marker,1)[0]
 found=set()
 for group in re.findall(r'\*\*\[([^]]+)\]\*\*',text):
  for token in group.split(', '):
   if '–' in token:
    a,b=token.split('–');assert a[:-2]==b[:-2]
    found.update(f'{a[:-2]}{i:02}' for i in range(int(a[-2:]),int(b[-2:])+1))
   else:found.add(token)
 assert found=={r['id'] for r in rows},(marker,found^{r['id'] for r in rows})
main=source('app/src/main/java/com/dessalines/thumbkey/MainActivity.kt')
routes=re.findall(r'(?:gemSettingsDestination|composable)\(\s*"([^"]+)"',main)
assert len(routes)==23
for doc in ['current-map.md','migration.md']:
 content=(DOC/doc).read_text()
 for route in routes:assert f'`{route}`' in content,(doc,route)
links=0
for f in DOC.glob('*.md'):
 for url in re.findall(r'\]\(([^)]+)\)',f.read_text()):
  if '://' in url or url.startswith('#'):continue
  assert (f.parent/url.split('#')[0]).exists(),(f.name,url)
  links+=1
for f in (DOC/'visuals').glob('*.svg'):
 root=ET.parse(f).getroot()
 assert not root.findall('.//{http://www.w3.org/2000/svg}script')
 png=f.with_suffix('.png').read_bytes()
 assert png[:8]==b'\x89PNG\r\n\x1a\n'
 w,h=struct.unpack('>II',png[16:24])
 assert w==int(root.get('width')) and h==int(root.get('height')),(f.name,w,h)

def luminance(c):
 channels=[int(c[i:i+2],16)/255 for i in (1,3,5)]
 channels=[x/12.92 if x<=.04045 else ((x+.055)/1.055)**2.4 for x in channels]
 return sum(a*b for a,b in zip(channels,[.2126,.7152,.0722]))
def contrast(a,b):
 x,y=sorted([luminance(a),luminance(b)])
 return (y+.05)/(x+.05)
for name,bg,surface,text,muted,edge,accents in [
 ('dark','#141627','#23263B','#F6F3FC','#BEC2DB','#8088A8',['#F4AAE3','#A6E5DE','#F6D98B']),
 ('light','#F7F3FA','#E7E6F1','#202037','#535A76','#727995',['#7C286F','#146C69','#735820'])]:
 ratios=[]
 for fg in [text,muted]+accents:
  for back in [bg,surface]:
   ratio=contrast(fg,back);assert ratio>=4.5,(name,fg,back,ratio);ratios.append(ratio)
 for back in [bg,surface]:assert contrast(edge,back)>=3,(name,'outline',contrast(edge,back))
 print(f'{name}: minimum tested text/accent contrast {min(ratios):.2f}:1; outlines >=3:1')
print(f'PASS: {len(rows)} entries; complete mappings in 3 trees; {len(routes)} routes; {links} local links; SVG/PNG pairs')
