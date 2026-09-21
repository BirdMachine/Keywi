# Keywi upstream sync — September 2026

This document tracks the selective sync from `dessalines/thumb-key` after upstream commit `e192e47`.

## Integration policy

Keywi keeps its own identity, release/version lineage, README work, Kaomoji/palette features, and experimental Fishwi/Penguin branches. Upstream changes are evaluated for compatibility rather than blindly syncing the fork.

## Upstream changes to integrate

### Core / useful

- `5543dfb` — add `MEDIUM` key font-size variant.
- `b2e7257` — desktop-style compose-combo engine. Adapt the generic mechanism; do not require an upstream layout to use it.
- `cf23c36` — English Messagease compose-combo layout. Evaluate separately from the engine; optional for Keywi.
- `6b53e4c` — CapsLock support for TR/RU Arti layouts.
- `7d5736d` — CZTypeSplit layout. Optional layout import.

### Dependency / build maintenance

Evaluate as one compatible toolchain set rather than isolated version bumps:

- Compose BOM 2026.08 / 2026.09 and compileSdk 37
- Kotlin 2.4.20
- KSP 2.3.12
- Room 2.8.5
- Navigation Compose 2.10.0 / 2.10.1
- runtime-livedata 1.12.0 / 1.12.1
- FreeDroidWarn 1.14
- Android application/library plugin 9.4.1
- Prettier image 3.9.8

### Do not blindly import

- Upstream `Upping version.` commit: Keywi owns its APK/release version lineage.
- README / CONTRIBUTING prose that describes Thumb-Key as the product: adapt only where relevant.
- Translation-only changes unless the corresponding Keywi strings/layouts remain compatible.

## Safety

All integration work happens on `sync/upstream-2026-09-21`. `main` remains the known-good Keywi line until CI passes and the resulting diff has been reviewed.
