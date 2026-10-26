# Keywi documentation freshness audit — October 25, 2026

This is a review queue, **not a claim that the current app implements the listed changes**.

## Source inventory
- `docs/` contains 30 root entries, including the `guide/` handbook and `site/` static site generator.
- The `guide/` currently has nine Markdown handbook pages plus an `images/` directory.
- `docs/guide/README.md` explicitly targets build 516 / October 17, 2026.
- `docs/site/build.mjs` likewise hardcodes a "Build 516 checkpoint" label. Any later APK features need new screenshots and route checks before publication as current behavior.

## Reverification priority
| Subject | Current guide statement | Check against latest Keywi code and APK |
|---|---|---|
| Advanced sound | Editor says Advanced Sound source has no routed entry at build 516 | Verify current settings route, scale/tone functionality, modes, and per-key behavior |
| On-type overlays | Described as planned or unreachable in build 516 | Verify route, GIF/PNG modes, fadeout and per-key options |
| Feedback toggle | Not in build 516 guide table | Verify location and three feedback modes |
| Toolbar view cycling | Guide describes separate Fancy Text / Kaomoji tools | Confirm current page cycling, scroll inversion and icon placement |
| Emoji picker | Guide source snapshot predates newer emoji-handling work | Check shipped Unicode/Emoji version and built-in picker behavior |
| Theme editor / cuts | Guide describes ME-Like and prototype HK-Like | Verify routed controls, latest preview, experimental cuts and theme packaging |
| Photos / screenshots | Guide screenshots documented for older checkpoint | Capture actual current app and update alt text / labels |

## Publishing rules
1. Confirm behavior from latest `main` source *and preferably an installed current APK*.
2. Treat newly merged features separately from pending PRs or designs.
3. Update individual Markdown pages first; the SVG atlas should **link to** those canonical sources, not duplicate unverified instructions.
4. Rebuild `docs/site/` after changes and run its smoke test.
5. The SVG uses self-contained artwork. Embedded SVG links may not be clickable on GitHub; Markdown provides canonical fallback navigation.
