"""Check generated handbook links, images and heading anchors without a browser."""
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import unquote, urlsplit


class Page(HTMLParser):
    def __init__(self, path):
        super().__init__()
        self.ids = set()
        self.links = []
        self.feed(path.read_text())

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if attrs.get("id"):
            self.ids.add(attrs["id"])
        for attr in ("href", "src"):
            if attrs.get(attr):
                self.links.append(attrs[attr])


root = Path("_site").resolve()
pages = {p.resolve(): Page(p) for p in root.glob("*.html")}
assert len(pages) == 9, "Expected all nine handbook pages"
for path, page in pages.items():
    for link in page.links:
        url = urlsplit(link)
        if url.scheme or url.netloc:
            continue
        target = (path.parent / unquote(url.path)).resolve() if url.path else path
        assert target.is_relative_to(root), f"Link leaves website: {path}: {link}"
        assert target.exists(), f"Missing target: {path}: {link}"
        if url.fragment and target in pages:
            assert unquote(url.fragment) in pages[target].ids, f"Missing anchor: {path}: {link}"
print(f"All local links, anchors and assets passed across {len(pages)} pages")
