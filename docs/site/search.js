const input = document.querySelector('#search');
const results = document.querySelector('#results');
let index;
input.addEventListener('input', async () => {
  const query = input.value.trim().toLowerCase();
  results.replaceChildren();
  if (!query) return;
  try {
    index ||= await fetch('search.json').then(response => {
      if (!response.ok) throw new Error('Search index unavailable');
      return response.json();
    });
    if (input.value.trim().toLowerCase() !== query) return;
    const terms = query.split(/\s+/);
    const found = index.filter(page => terms.every(term => (page.title + ' ' + page.text).toLowerCase().includes(term)));
    for (const page of found) {
      const link = document.createElement('a');
      link.href = page.url;
      link.textContent = page.title;
      results.append(link);
    }
    if (!found.length) results.textContent = 'No matching pages. Try a broader word.';
  } catch {
    results.textContent = 'Search is unavailable. Use the page navigation below.';
  }
});
