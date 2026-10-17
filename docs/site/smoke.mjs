import { chromium } from 'playwright';
import fs from 'node:fs/promises';

await fs.mkdir('handbook-preview', { recursive: true });
const browser = await chromium.launch({ headless: true });
try {
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } });
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.goto('http://127.0.0.1:8765/', { waitUntil: 'networkidle' });
  await page.screenshot({ path: 'handbook-preview/desktop.png' });
  await page.locator('#search').fill('backup');
  await page.locator('#results a').first().waitFor();
  if (!(await page.locator('#results a').allTextContents()).some(text => text.includes('Power, backup'))) {
    throw Error('Search did not find the backup guide');
  }
  await page.locator('#search').fill('zzzz-no-such-setting');
  await page.getByText('No matching pages. Try a broader word.').waitFor();
  await page.locator('#search').fill('');
  const links = await page.locator('.sidebar nav a').evaluateAll(nodes => nodes.map(n => n.href));
  for (const url of links) {
    await page.goto(url, { waitUntil: 'networkidle' });
    if (!(await page.locator('article h1').count())) throw Error('Missing page title: ' + url);
    await page.setViewportSize({ width: 390, height: 844 });
    if (await page.evaluate(() => document.documentElement.scrollWidth > innerWidth)) {
      throw Error('Mobile horizontal overflow: ' + url);
    }
    const brokenImages = await page.locator('article img').evaluateAll(nodes => nodes.filter(n => n.complete && n.naturalWidth === 0).length);
    if (brokenImages) throw Error('Broken screenshot: ' + url);
  }
  await page.goto('http://127.0.0.1:8765/', { waitUntil: 'networkidle' });
  await page.screenshot({ path: 'handbook-preview/mobile.png', fullPage: true });
  if (errors.length) throw Error(errors.join('\n'));
  console.log('Search, nine-page navigation, screenshots and mobile layout passed');
} finally {
  await browser.close();
}
