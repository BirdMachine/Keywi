#!/usr/bin/env python3
"""Build Keywi's pinned offline Unicode character catalog from official UCD files."""
import argparse
import bisect
import gzip
import hashlib
import json
import os
import tempfile
from pathlib import Path
import urllib.request

VERSION = "18.0.0"
FILES = ["UnicodeData.txt", "Blocks.txt", "NameAliases.txt", "extracted/DerivedName.txt"]
ROOT = Path(__file__).resolve().parents[2]


def records(path):
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.split("#", 1)[0].strip()
        if line:
            yield [part.strip() for part in line.split(";")]


def codepoints(value):
    parts = value.split("..")
    return range(int(parts[0], 16), int(parts[-1], 16) + 1)


def generate(source_dir, destination):
    categories = {}
    first = None
    for fields in records(source_dir / "UnicodeData.txt"):
        cp = int(fields[0], 16)
        if fields[1].endswith(", First>"):
            first = cp
        elif fields[1].endswith(", Last>"):
            assert first is not None
            for item in range(first, cp + 1):
                categories[item] = fields[2]
            first = None
        else:
            categories[cp] = fields[2]
    assert first is None
    names = {}
    for value, name in records(source_dir / "extracted/DerivedName.txt"):
        for cp in codepoints(value):
            names[cp] = name.replace("*", f"{cp:04X}")
    aliases = {}
    for value, alias, _kind in records(source_dir / "NameAliases.txt"):
        aliases.setdefault(int(value, 16), []).append(alias)
    blocks = []
    for value, name in records(source_dir / "Blocks.txt"):
        points = codepoints(value)
        blocks.append((points.start, points.stop - 1, name))
    starts = [block[0] for block in blocks]
    rows = []
    for cp, category in sorted(categories.items()):
        if category in {"Cn", "Cs", "Co"}:
            continue
        name = names.get(cp)
        if not name:
            assert category == "Cc", f"Missing derived name for {cp:04X} ({category})"
            # Controls have no Name property; use their normative control alias.
            name = next(iter(aliases.get(cp, [])), f"CONTROL-{cp:04X}")
        block_index = bisect.bisect_right(starts, cp) - 1
        assert block_index >= 0 and cp <= blocks[block_index][1], hex(cp)
        rows.append("\t".join([f"{cp:04X}", name, category, blocks[block_index][2], "|".join(aliases.get(cp, []))]))
    payload = f"# Keywi Unicode {VERSION}\n" + "\n".join(rows) + "\n"
    destination.parent.mkdir(parents=True, exist_ok=True)
    # Empty filename and mtime=0 make the checked-in bytes reproducible.
    with tempfile.NamedTemporaryFile(dir=destination.parent, delete=False) as raw:
        temp_path = Path(raw.name)
        try:
            with gzip.GzipFile(filename="", fileobj=raw, mode="wb", mtime=0, compresslevel=9) as out:
                out.write(payload.encode("utf-8"))
            raw.flush()
            os.fsync(raw.fileno())
        except BaseException:
            temp_path.unlink(missing_ok=True)
            raise
    temp_path.replace(destination)
    manifest = {
        "unicode_version": VERSION,
        "entries": len(rows),
        "compressed_bytes": destination.stat().st_size,
        "uncompressed_bytes": len(payload.encode("utf-8")),
        "sources": {name: {"url": f"https://www.unicode.org/Public/{VERSION}/ucd/{name}", "sha256": hashlib.sha256((source_dir / name).read_bytes()).hexdigest()} for name in FILES},
        "catalog_sha256": hashlib.sha256(destination.read_bytes()).hexdigest(),
    }
    destination.with_suffix(".manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
    print(json.dumps(manifest, indent=2))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-dir", type=Path, help="Use already downloaded UCD sources; no network access")
    parser.add_argument("--output", type=Path, default=ROOT / "app/src/main/assets/unicode/catalog.tsv.gz")
    parser.add_argument("--check", action="store_true", help="Regenerate to a temporary directory and verify checked-in asset and manifest")
    args = parser.parse_args()
    source_dir = args.source_dir or ROOT / ".unicode-source" / VERSION
    if args.source_dir is None:
        for name in FILES:
            target = source_dir / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(urllib.request.urlopen(f"https://www.unicode.org/Public/{VERSION}/ucd/{name}", timeout=60).read())
    if args.check:
        with tempfile.TemporaryDirectory() as temp_dir:
            generated = Path(temp_dir) / "catalog.tsv.gz"
            generate(source_dir, generated)
            for expected, actual in [(args.output, generated), (args.output.with_suffix(".manifest.json"), generated.with_suffix(".manifest.json"))]:
                if expected.read_bytes() != actual.read_bytes():
                    raise SystemExit(f"Catalog out of date: {expected}")
        print("Checked-in Unicode catalog and manifest are reproducible.")
    else:
        generate(source_dir, args.output)


if __name__ == "__main__":
    main()
