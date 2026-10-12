# Theme assets and portable packages

Keywi stores theme manifests and their private media under `filesDir/themes/<theme-id>/theme.json` and `filesDir/themes/<theme-id>/assets/`. Display names are metadata; stable IDs identify directories. The `custom` directory is the editing workspace. This app-private storage persists through app updates, but is removed on uninstall; export a package for a portable backup.

Images, GIFs and imported fonts are copied into Custom when picked. Copies retain their original bytes, including GIF animation. Immutable SHA-256 filenames avoid collisions. Saving a named theme copies its referenced assets into its own directory; editing that saved theme copies its assets back into Custom. Resetting Custom does not remove another theme's copies. Unreferenced assets are retained for now rather than risking existing references.

Schema 2 documents contain relative `assets/<filename>` references and optional font metadata (`asset`, `name`). Runtime backdrop/font preferences resolve those references to private files. Geometry and character-layout settings remain independent of theme documents.

Export produces one `.keywi-theme.zip` containing `theme.json` and all referenced media/fonts. Import extracts into staging, validates paths, asset availability, font validity and size limits, then replaces Custom's manifest and applies it. A rejected package leaves the existing Custom manifest and active theme intact. Unknown entries, path traversal, external media references in ZIP manifests, duplicate entries and missing assets are rejected. Limits: 64 archive entries, 64 MiB per asset, 128 MiB total unpacked package, 1 MiB manifest.

Legacy JSON themes without assets continue to import. Legacy JSON containing readable external content URIs is copied into Custom; an asset-backed JSON containing relative references requires its ZIP package. Existing flat theme JSON files migrate into per-theme directories. Original flat files remain as migration backups. Because fonts were previously global, migration snapshots the currently selected font for legacy themes; it cannot reconstruct historical font choices.

Unreadable legacy media references are retained and reported in Theme Manager. Opening the editor still works so a missing image/GIF/font can be replaced. Missing assets prevent exporting an incomplete package. Keywi cannot recover a source file that was already deleted or whose provider permission was revoked before migration.
