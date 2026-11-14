# Offline Unicode character library

Keywi bundles a version-pinned **Unicode 18.0.0** character catalog. Names and
properties are independent of Android's ICU version: a new character can be
found and inserted even when the installed fonts cannot display it.

## Scope

The catalog contains **172,873 assigned scalar characters**: 172,808 graphic and
format characters plus 65 controls. It expands all UnicodeData First/Last ranges
and uses DerivedName.txt for algorithmic names, including CJK, Hangul and Tangut.
Normative aliases (including control names and abbreviations), Unicode blocks,
and two-letter general categories are searchable. Broad category filters group
letters, marks, numbers, punctuation, symbols, separators, and controls/format.

This is a full character catalog for the pinned release, not a handpicked symbol
list. Unassigned code points, surrogate code units, noncharacters and private-use
areas are excluded: they have no standardized assigned character identity. The
catalog does not enumerate multi-character emoji ZWJ/flag/skin-tone combinations,
named sequences, every variation sequence, or the Unihan dictionary and readings.
Those can be separate catalogs later without claiming every possible string is
a character. Search names/aliases are the official English UCD metadata.

Controls, format characters, spaces and combining marks can be invisible or
context-dependent. A missing glyph is different from an intentionally invisible
character. The UI identifies these cases rather than treating every blank as a
font failure. Font checks describe Keywi's preview only; other applications may
use different fonts or shaping engines. Installing/downloading a font does not
grant an Android keyboard the ability to replace system or other apps' fonts.

## Footprint and execution

The bundled gzip asset is **1,029,574 bytes** (about 0.98 MiB); decompressed TSV is
10,771,978 bytes (about 10.27 MiB). Runtime objects require additional memory; the
catalog is loaded on demand on Dispatchers.IO and soft-cached so Android can reclaim it after the panel closes.
The open panel holds its own strong reference. Repeated category/block strings are shared. No font megabundle or network access
is required for browsing, searching, or insertion. The UI bounds result pages
and runs searches off the main thread; search supports cancellation checkpoints.
A font-download/help link intentionally opens an external browser and needs an
internet connection. The keyboard does not download fonts in the background.

## Reproduce the asset

Run from the repository root:

```sh
python3 scripts/unicode/generate_catalog.py
```

The generator downloads four official, explicitly versioned files from
`https://www.unicode.org/Public/18.0.0/ucd/`. For an offline regeneration, put these
files in a local directory, preserving the `extracted/` subdirectory, and run:

```sh
python3 scripts/unicode/generate_catalog.py --source-dir /path/to/ucd18
```

Add `--check` to regenerate in a temporary directory and verify both checked-in
asset and manifest without changing them. Asset writes use an atomic rename.

The checked-in `catalog.tsv.manifest.json` records source URLs, SHA-256 hashes,
entry count, compressed/uncompressed sizes, and catalog hash. gzip timestamps and
filenames are normalized so the same inputs produce identical bytes. Regenerate
the manifest with the asset and review source hash changes together. This script
never relies on the host Python or Android Unicode database.

Data is covered by [Unicode License V3](LICENSE-UNICODE.txt). Unicode is a
registered trademark of Unicode, Inc. Official release and data documentation:

- https://www.unicode.org/Public/18.0.0/ucd/ReadMe.txt
- https://www.unicode.org/versions/Unicode18.0.0/
- https://www.unicode.org/reports/tr44/
