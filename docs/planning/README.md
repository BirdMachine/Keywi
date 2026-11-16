# Keywi workbench 🥝

One place to choose the next task. This is the current coordination plan; issues and PRs keep their detailed acceptance criteria and evidence. [Ideas](ideas.md) are separate from this queue. [Birdie's design choices](decisions.md) remain open until she chooses.

Reviewed **2026-11-16** against `main` and `Dev` at [`02e47ab8`](https://github.com/BirdMachine/Keywi/tree/02e47ab8fe300337c460b4f6300446d0477bb2ab). Both branches match. This document proposes an integration order; it does not authorize merging pending UI work or publishing a release.

## What is already there

| Work | Evidence / remaining check |
| --- | --- |
| Gesture label fitting and independent editor folds (#62 / #64) | Merged through Dev to main. Preserve whole-label sizing, inherited fonts, hitboxes, composed drafts and selection in future editor work. |
| Channel-specific names (#66) | Merged. `Dev` builds use **Keywi-dev** / `keywi-dev`; `main` uses **Keywi** / `keywi`. |
| Oklab Broad / Oklab Tight (#39) | Both stable built-in IDs and approximation descriptions exist in [GradientLibrary](../../app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/GradientLibrary.kt). [Regression tests](../../app/src/test/java/com/dessalines/thumbkey/ui/components/keyboard/OklabReferenceGradientsTest.kt) cover distinct ramps, dense ordered stops and closed seams. Retain #39 for reference-image/device visual acceptance; don't reimplement the presets. |
| Sound, formatting, kaomoji and toolbar controls | Existing code/guide work supersedes the early “imminent” and “sound shell” descriptions. A full Unicode browser remains pending #58. |
| Theme packages | Current [ThemeEngine](../../app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/ThemeEngine.kt) uses **schema 3**. Schema 4 feedback portability is proposed in #56; sound packs and per-action artwork are not made portable by that PR. |

Baseline Android CI passed for [main](https://github.com/BirdMachine/Keywi/actions/runs/43610280118) and [Dev](https://github.com/BirdMachine/Keywi/actions/runs/43610349951). The separate [live-keyboard showcase](https://github.com/BirdMachine/Keywi/actions/runs/43610279877) failed its HK settings-shortcut assertion after APK installation; do not report the entire release verification as green.

## Integration order and overlapping work

All nine existing work PRs were retargeted to **Dev** on 2026-11-16. A green check below is historical evidence at that PR's recorded head, not proof that a future combined merge is tested. Recheck the diff and CI after each integration.

| Order / lane | Work | Next concrete action |
| --- | --- | --- |
| 1 · Small independent polish | [#48 — settings header copy](https://github.com/BirdMachine/Keywi/pull/48) | Existing three-string request; review current diff and integrate independently when ready. No need to fold it into a settings rewrite. |
| 2 · Persistence first | [#56 — theme library + schema 4](https://github.com/BirdMachine/Keywi/pull/56) | Review delete/overwrite behavior and legacy theme/feedback compatibility. Run combined tests on Dev and verify a real export/import before calling portability done. Historical Android CI passed. |
| 3 · Theme navigation | [#60 — subpages + Tap effects](https://github.com/BirdMachine/Keywi/pull/60) | Resolve the [ownership decision](decisions.md), then reconcile with #56's ThemeManagerCompositionTest and package docs. Preserve the original session snapshot/Revert across pages. Head CI passed in run 42499517406; device checks remain open. |
| 4 · Documentation follows behavior | [#61 — round trips + guide](https://github.com/BirdMachine/Keywi/pull/61) | Keep its real-package test and provider/capture evidence. Reconcile schema-3 statements if #56 lands; update routes if #60 lands. Use [ideas.md](ideas.md) as the canonical wishlist instead of restoring its older README list. Head CI passed in run 43599113449; Drive/Samsung Files/live captures remain unverified. |
| Decision track | [#63 / #65 — settings architecture](https://github.com/BirdMachine/Keywi/pull/65) | Review three alternatives and choose a first slice. Documentation can be reviewed independently; merging research does not choose or implement Hybrid. Preserve #64's completed work. |
| Independent repair | [#58 — offline Unicode](https://github.com/BirdMachine/Keywi/pull/58) | Latest recorded CI fails at `:app:lintKotlinMain` (run 40592326238), before Android compile/tests. Read its lint diagnostics, fix on its own branch, then validate catalog provenance, compile, paging/insertion and narrow-screen UI. Keep separate from multi-character emoji audit #47. |
| Selective experiment | [#57 — Chromatic UI Lab](https://github.com/BirdMachine/Keywi/pull/57) | Review the final journal and 15-file diff, not only its session-1 summary. Recipes, jump rail, slider textures and staged/non-destructive theme selection overlap #56/#60. Extract chosen pieces after those owners settle. Do not merge the whole lab automatically. Head Android CI passed in run 40508993338; device visuals remain open. |
| Selective upstream review | [#51 — Thumb-Key harvest](https://github.com/BirdMachine/Keywi/pull/51) | Remove the declined Compose behavior from any proposed integration; don't add IME previous/next toggles. Evaluate layout search/pinning, caps/lifecycle fixes and non-destructive colon emoji suggestions separately. Glyph sketch is not whole-word handwriting. Historical Android CI passed, but scope and live wiring remain incomplete. |
| Documentation experiment | [#50 — SVG Atlas](https://github.com/BirdMachine/Keywi/pull/50) | Reconcile handbook changes after #60/#61. Repair its failed handbook build, inspect SVG contrast/links, and retain Markdown fallback navigation. Android CI alone did not validate this PR's docs build. |

#56 → #60 → #61 is a **proposed review order**, not a strict code dependency. #65 informs navigation decisions; it need not block unrelated fixes. Don't duplicate feature implementations to make a large combined PR.

## Maintenance queue

| Task | Definition of done |
| --- | --- |
| Retire merged branch clutter | [Dated audit](2026-11-16-repo-audit.md) records 42 eligible names and their SHAs. Recheck remote tips and open PR heads before deletion; keep `main`, `Dev`, theme-gallery and all unmerged work. Remote deletion is still blocked by missing write authentication. |
| Repair live HK showcase | Diagnose `HK settings shortcut missing in live IME` at capture script line 95; inspect the captured UI and actual selected geometry. Keep the assertion until behavior or its selector/setup is verified. Rerun the showcase with an actual emulator. |
| Establish a clean Kotlin formatting baseline | Separate format-only change, followed by format/lint/tests/lint/APK. Only then remove CI's temporary runner-side formatting step. Do not mix this into editor or schema changes. |
| Refresh the handbook once integrations settle | Preserve dated screenshot/source provenance; reconcile #50/#60/#61 rather than maintaining competing guide versions. Generate the site and run its link/anchor/asset checker. |
| Recheck reported typing issues | Reproduce ASCII quote conversion, Shift-sensitive suggestions, formatter strikethrough and rotation/theme mode before assigning new implementation scope. |

## Working rules

- Feature/fix/docs branch → **Dev** integration → reviewed **Dev → main** release. Merge main back when it carries missing changes; preserve integration history. Use a fresh topic branch for each bounded task.
- Keep geometry, character mappings, custom boards and appearance as separate domains. The [Gem Cut ADR](../adr-001-gem-cut-axes.md) and migration/serialization contracts remain references.
- Preserve saved themes, board IDs, group overrides, media, Tasker assignments and access to expert controls. A navigation move is not a schema migration.
- Record evidence as **source reviewed**, **CI passed**, or **device verified** with a commit/run. Do not collapse those into “done”. Close an issue only when its remaining acceptance criteria are met.
- Keep shader work isolated. Preserve experiments and unmerged commits during cleanup; “old” does not mean “safe to delete”.

When status changes, update the relevant row here and the issue/PR. Keep implementation detail in its owning document; add a new idea once in [ideas.md](ideas.md). Older roadmap files remain history/reference, not additional implementation queues.
