import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

let marked;
try { ({ marked } = await import('marked')); }
catch (error) {
  // The shared development runtime can supply the same pinned dependency.
  if (!process.env.CODEX_PRIMARY_RUNTIME_NODE_MODULES) throw error;
  ({ marked } = await import(pathToFileURL(path.join(process.env.CODEX_PRIMARY_RUNTIME_NODE_MODULES, 'marked/lib/marked.esm.js'))));
}
const here = path.dirname(fileURLToPath(import.meta.url));
const source = path.resolve(here, '../guide');
const out = path.resolve(process.argv[2] || path.join(here, '../../_site'));
const pages = [
  ['README.md', 'Start here', '◇'],
  ['cuts-and-layouts.md', 'Cuts & layouts', '⌨'],
  ['themes-and-appearance.md', 'Themes & appearance', '✦'],
  ['boards-and-keys.md', 'Boards & key assignments', '▦'],
  ['typing-and-clipboard.md', 'Typing & clipboard', '⌘'],
  ['power-backup-diagnostics.md', 'Power, backup & diagnostics', '⚙'],
  ['troubleshooting.md', 'Troubleshooting', '?'],
  ['screenshots.md', 'Screenshot gallery', '▧'],
  ['maintaining-the-guide.md', 'Maintainer notes', '↗'],
];
const htmlName = name => name === 'README.md' ? 'index.html' : name.replace(/\.md$/, '.html');
const escape = text => text.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;');
const slug = text => text.toLowerCase().replace(/[^\p{L}\p{N}_\- ]/gu, '').replaceAll(' ', '-');
await fs.mkdir(out, { recursive: true });
await fs.cp(path.join(source, 'images'), path.join(out, 'images'), { recursive: true });
await fs.copyFile(path.join(here, 'style.css'), path.join(out, 'style.css'));
await fs.copyFile(path.join(here, 'search.js'), path.join(out, 'search.js'));
const search = [];
for (const [file, title] of pages) {
  const markdown = await fs.readFile(path.join(source, file), 'utf8');
  const headings = [];
  const counts = new Map();
  const renderer = new marked.Renderer();
  renderer.heading = function ({ tokens, depth, text }) {
    const base = slug(text);
    const count = counts.get(base) || 0;
    counts.set(base, count + 1);
    const id = base + (count ? `-${count}` : '');
    if (depth === 2) headings.push({ id, text });
    return `<h${depth} id="${id}">${this.parser.parseInline(tokens)}</h${depth}>\n`;
  };
  let body = marked.parse(markdown, { renderer, gfm: true });
  body = body.replace(/href="([^"#]+)\.md(#[^"]*)?"/g, (_, target, anchor = '') => `href="${htmlName(target + '.md')}${anchor}"`)
    .replaceAll('<table>', '<div class="table-scroll"><table>').replaceAll('</table>', '</table></div>')
    .replaceAll('<img ', '<img loading="lazy" ');
  const nav = pages.map(([name, label, icon]) => `<a href="${htmlName(name)}" ${name === file ? 'aria-current="page"' : ''}><span aria-hidden="true">${icon}</span>${escape(label)}</a>`).join('');
  const toc = headings.map(h => `<a href="#${h.id}">${escape(h.text)}</a>`).join('');
  const n = pages.findIndex(p => p[0] === file);
  const previous = pages[n - 1];
  const next = pages[n + 1];
  const pager = `<nav class="pager" aria-label="Adjacent pages">${previous ? `<a href="${htmlName(previous[0])}">← ${escape(previous[1])}</a>` : '<span></span>'}${next ? `<a href="${htmlName(next[0])}">${escape(next[1])} →</a>` : ''}</nav>`;
  await fs.writeFile(path.join(out, htmlName(file)), `<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="color-scheme" content="dark light"><meta name="description" content="Keywi's illustrated settings handbook: ${escape(title)}."><title>${escape(title)} · Keywi handbook</title><link rel="stylesheet" href="style.css"><script defer src="search.js"></script></head>
<body><a class="skip" href="#content">Skip to content</a>
<header><a class="brand" href="index.html"><span class="gem" aria-hidden="true">◇</span><span>KEYWI<small>THE SETTINGS HANDBOOK</small></span></a><a class="repo" href="https://github.com/BirdMachine/Keywi">GitHub ↗</a></header>
<div class="layout"><aside class="sidebar"><label for="search">Find a setting</label><input id="search" type="search" placeholder="Try borders, GIFs, backup…" autocomplete="off" aria-controls="results"><div id="results" role="status" aria-live="polite"></div><nav aria-label="Handbook">${nav}</nav><div class="checkpoint">Build 516 checkpoint<br>Made to be made yours. ✦</div></aside>
<main id="content"><div class="eyebrow">KEYWI / USER GUIDE</div><article>${body}</article>${pager}<footer>Grown from Thumb-Key, made colorful by Keywi. <a href="https://github.com/BirdMachine/Keywi/blob/main/docs/guide/${file}">Edit this page ↗</a></footer></main>
<aside class="contents"><div>ON THIS PAGE</div><nav aria-label="On this page">${toc}</nav></aside></div></body></html>`);
  search.push({ title, url: htmlName(file), text: body.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ') });
}
await fs.writeFile(path.join(out, 'search.json'), JSON.stringify(search));
await fs.writeFile(path.join(out, '.nojekyll'), '');
console.log(`Built ${pages.length} handbook pages in ${out}`);
