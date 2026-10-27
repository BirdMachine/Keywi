# KeyWi Chromatic UI Lab — October 27 overnight handoff

## Mission and guardrails
Explore playful, weird, visually expressive **non-shader** KeyWi styling, interaction polish, and clearer interfaces for KeyWi's deep customization. Start from `Dev`, branch `feature/chromatic-ui-lab-2026-10-27`. Separate from previous GLSL shader branch and from `main`. No merges to Dev/main unless explicitly approved.

Aim for **working, testable changes** and an APK when feasible, not only mockups. Retain editability, sensible defaults, accessibility/contrast, low input latency, keyboard touch reliability, older Android compatibility, theme import/export integrity. Avoid shader/render-backend enhancements and avoid broad cleanup/optimization tonight (likely tomorrow's focus).

## Suggested design investigations — choose what evidence favors
- Clarify complexity with a cohesive information architecture: basic controls, expandable advanced panels, direct preview, discoverable actions, settings search or focused quick-jumps.
- Dramatic but lightweight styling: facet-aware borders, inset/beveled/glossy controls, visual depth with conventional gradients/draw paths, ornamental trims, chromatic key-group motifs, luminous accents without new shader backends.
- Further customize per-key/per-group feedback fades, gradient alignment and key materials where existing infrastructure allows.
- Better theme editor affordances: image/GIF visibility, readable labels, current theme vs saved theme separation, safe preview/undo, import/export clarity.
- Explore questions opportunistically while CI runs. Document prototypes, alternatives, tradeoffs; don't throw off the core goal.

## Workflow
- At start of every session: read this file fresh; inspect branch commits and latest CI; select one bounded and worthwhile task.
- Make incremental commits, run existing tests/CI, inspect artifacts; keep working in the current active session when possible. The scheduled sessions are fallback/recovery, **not** permission to abandon in-flight CI.
- At end of each session: update this journal with changes, commit SHAs, test/CI results, remaining work, next recommended step.
- Never claim emulator visual proof without a real emulator screenshot or measured run; never claim an installable APK until verified CI artifact exists.
- If a blocker asks for user intervention, investigate available free alternatives first.
- Do not assume live Work quota data. If fresh authoritative data is available: >=75% consumed prioritize finishing scope, >=90% consumed checkpoint rather than open major tasks.
- Sessions scheduled for 1/3/5/7/9 AM America/New_York, October 27, 2026. Stop by 10 AM and give a final status summary with best build/artifacts.

## Initial progress
- [x] Branch created from `Dev`.
- [x] Mission, safeguards, and continuation structure written.
- [x] Preflight audit of AdvancedLookAndFeelScreen and KeyDesignEditor; identified target: context-sensitive shortcuts and a compact editor navigation strip rather than replacing existing folds.
- [x] Implement at least one meaningful non-shader styling/customization enhancement.
- [x] Implement at least one coherent navigation/presentation improvement.
- [x] Add tests or coverage appropriate to changes.
- [x] Run CI/build and obtain a verified test APK if possible.
- [x] Capture trustworthy Robolectric Compose slider UI evidence (not an Android emulator/device).
- [ ] Summarize improvements, tradeoffs, opportunities and remaining work.

## Run log
- 2026-10-27 setup: branched from Dev; no feature code committed yet. Next: inspect look-and-feel editor and Settings screens, then implement smallest viable improvement.

## Preflight research & selected experiments (2026-10-27)
- Inspected `AdvancedLookAndFeelScreen.kt`: existing live test bench, Save/Revert, expandable Keyboard background / Suggestion bar / Keys / Typography sections. Preserve existing workflows and scroll behavior.
- Inspected `KeyDesignEditor.kt`: already supports stable-random, incremental and shared border gradient distribution; do **not** recreate these features. Existing per-group/per-mode overrides make a natural target for discoverability.
- External UI references: Blender properties editor uses contextual property tabs (https://docs.staging.blender.org/manual/en/latest/editors/properties_editor.html); Ableton Rack macros group numerous advanced parameters into adjustable macros and recallable variations (https://www.ableton.com/en/live-manual/12/instrument-drum-and-effect-racks/).
- Concrete recommended first implementation: a compact, horizontally scrollable **Jump to** selector for existing editor sections, visibly indicating the selected panel, without introducing a generic card dashboard. Keep labels accessible, scrolling intact, and the live preview visible. Verify Compose UI regression tests.
- Candidate second experiment: non-shader material presets using existing gradient + surface/border/shadow controls; offer focused one-click combinations with an undo/revert path. Avoid overwriting unsaved Custom themes without affirmative selection.
- Longer-range ideas: selective macro controls that map several existing styling parameters, snapshot/revert variations and optionally a reversible seed shuffle. These should remain design notes until safe data model is clear.
- No emulator proof, APK or feature code change in this preflight. Next scheduled run should implement and test a small navigation improvement first, then tackle one visual preset.

## Session 1 — 2026-10-27 01:00 EDT (complete)

**Code and behavior**
- Added a compact horizontally scrollable **Jump to a section** rail to the Advanced Theme Editor. Short labels (Backdrop, Suggestions, Key faces, Fonts) target existing folds; they reopen a manually collapsed fold and use Compose `BringIntoViewRequester` attached to the *header*, not the potentially taller-than-viewport body. The live preview, independent fold states, Save/Revert, and existing editor content remain.
- Added three explicit non-GLSL global key **material recipes**: Opal enamel (jewel-tone shared border), Molten lacquer (staggered ember rim), Cobalt glass (cool cobalt gradients). Each uses existing Compose/keyboard gradient primitives; small gradient swatches and descriptions help users understand the choice. One-step Undo restores the prior global key state until the next manual edit. No theme library writes, no automatic application, and no changes to per-key overrides, groups, or background media.
- Added Robolectric Compose tests for jump/expansion behavior and pure Kotlin tests for material uniqueness and preservation of key overrides/groups.
- Opened **draft** PR [#57](https://github.com/BirdMachine/Keywi/pull/57) targeting Dev strictly for CI; do **not merge** without explicit approval. Main and Dev untouched.
- Main feature SHA **`0c90dd3913e93969a0693b521fe3a21b8fbd0ee5`**. Earlier relevant changes: `bcc9255` (jump rail), `caab2fd` (jump tests), `f133611` (recipes), `2960dd1` (reversible controls), `b5f4212` (swatches), `7601dff` and `91456a6` (header-only reveal), `867169f` (recipe filename), `e6c65ff` (test import), `0c90dd3` (unique short chip labels).

**Validation**
- **Verified GitHub Actions success:** [Keywi CI run 40454068640](https://github.com/BirdMachine/Keywi/actions/runs/40454068640), all required validation/build steps successful: Kotlin formatting, unit tests, Android lint, debug APK assembly. Confirmed nonexpired `keywi-debug` APK artifact (artifact ID `12712037698`, ~32.4 MB), available from the run's Artifacts panel. This is build/test verification, **not** actual-device or emulator visual verification.
- Resolved two CI regressions during iteration: ktlint's singular-class filename rule and a mistaken test import; the full unit suite then revealed a duplicate `Suggestion bar` text selector caused by the new chip, fixed with distinct concise jump labels. Final CI green. Avoid frequent commits while CI runs: PR concurrency cancels superseded builds.
- No emulator screenshots captured. The existing `emulator-showcase.yml` is intentionally main-only; do not casually turn on screenshot workflows for every feature branch. The repository already has a Robolectric screenshot approach in `GemGlassSliderTest.kt` that could provide limited UI evidence next session, but not substitute for device testing.

**Research and next session**
- `BringIntoViewRequester` can scroll ancestors to a targeted modifier; targeting the compact header avoids oversize-section behavior. Reference: https://composables.com/jetpack-compose/androidx.compose.foundation/foundation/interfaces/BringIntoViewRequester/api
- Existing `GemGlassSlider.kt` already draws glass, spectral rims, and gradient rails with standard Compose Canvas. A promising next **non-shader** experiment is opt-in patterned slider rails (fine ticks, stipple, mesh, diagonal hatching), rendered with inexpensive Canvas primitives, while retaining native Slider semantics and power-mode behavior. Prefer targeted tests/screenshots before broader adoption.
- `drawWithCache` is an option for more intricate conventional 2D decorations where caching drawing objects is warranted; avoid claiming it automatically improves all draw performance. Reference: https://composables.com/jetpack-compose/androidx.compose.ui/ui/modifiers/drawWithCache/examples
- Keep minimum comfortable touch targets (~48dp), readable contrasts, reduced-motion/power modes and no typed-content exposure. Reference: https://github.com/cvs-health/android-compose-accessibility-techniques/blob/main/doc/interactions/MinimumTouchTargetSize.md
- Next: inspect artifact and visual previews; consider a screenshot test of material swatches, contrast guardrails, a reversible pattern selector for the glass slider, or richer theme navigation. Don't merge. Check new CI triggered by this journal-only commit before beginning further edits.

## Session 2 — 2026-10-27 03:00 EDT (complete)

**Feature commit:** [`9c3e9f57544ec413c42cba66859301384a72489e`](https://github.com/BirdMachine/Keywi/commit/9c3e9f57544ec413c42cba66859301384a72489e)

**Code and behavior**
- Added opt-in **Studio slider textures** for the existing `GemGlassSlider` component: Plain (unchanged default), Ticks, Stipple, Mesh, Diagonal. Patterns decorate the **unfilled** recessed track using only Compose Canvas lines/circles and clipping. Filled chromatic gradient and glass thumb remain intact; unfilled region mirrors for RTL.
- Added a compact, horizontally scrollable live pattern picker and adjustable demo slider in the Theme Editor, beneath the live preview. This changes the **settings UI sliders**, not the actual keyboard theme. Selection is persisted in local `keywi_control_appearance` preferences and deliberately **not** exported with keyboard themes. No existing theme files, saved groups, or per-key overrides touched.
- The existing Material3 `Slider` still owns gesture/keyboard/accessibility semantics; `DESPERATE` power mode continues to use its lightweight stock fallback.
- Extended `GemGlassSliderTest` with dark Stipple and light Mesh rendering + `SetProgress` semantics tests, and a preference persistence/unknown-value fallback test.

**Verified validation**
- [GitHub Actions CI run 40464835615](https://github.com/BirdMachine/Keywi/actions/runs/40464835615) **completed successfully** on feature SHA `9c3e9f5`: Kotlin formatting, full unit tests, Android lint, and debug APK assembly passed.
- Nonexpired **`keywi-debug` artifact ID `12716790211`**, ZIP ~32.4 MB, is downloadable from the run's Artifacts panel.
- Nonexpired **`tool-page-review` artifact ID `12716645148`** contains Robolectric Compose screenshots `glass-slider-dark-plain.png`, `glass-slider-dark-dots.png`, `glass-slider-light-plain.png`, `glass-slider-light-mesh.png`.
- Inspected those actual PNG files: dotted/mesh marks are visible only in the unfilled track. The two pairs have identical 272×48 dimensions; comparing plain vs dotted dark images changed **61 pixels**, bounding box (165,22)-(256,26); plain vs mesh light changed **184 pixels**, bounding box (164,21)-(256,27). This is useful **Robolectric-rendered UI evidence**, not physical-device or Android-emulator verification. Slider semantics tests also passed.
- Draft PR [#57](https://github.com/BirdMachine/Keywi/pull/57) still targets Dev; **no merge** and no changes to Dev/main.

**Research and next candidates**
- Official Android graphics docs: https://developer.android.com/develop/ui/compose/graphics/draw/modifiers — `drawWithCache` helps only when there are meaningful objects to cache; the present simple line/circle marks do not justify an extra caching layer without profiling.
- Official accessibility semantics: https://developer.android.com/develop/ui/compose/accessibility/semantics — preserving the underlying Material3 Slider avoids rebuilding progress semantics. Tests explicitly use `SemanticsActions.SetProgress`.
- Explore patterned **filled** tracks, customizable spacing/intensity/color, and per-slider pattern overrides only if a clear settings model emerges. Keep simple defaults and no overdraw-heavy animations.
- Inspect theme recipe contrast with existing user-configured glyph colors, and consider a clear "temporary preview vs apply" flow in Theme Manager before adding more recipes. Existing library tile selection calls `ThemeEngine.apply`; investigate preservation of unsaved Custom edits before modifying behavior.
- Next run: validate new pattern selector UI interaction in a focused Compose test; consider screenshot diff assertions to ensure patterns never silently disappear; try an emulator only if a reliable free test route is available. The build-verified APK is ready for optional manual testing.

## Session 3 — 2026-10-27 05:00 EDT (complete)

**Feature head:** [`35ec1031bba3752d41c74b4bd19cc69d2b2e857d`](https://github.com/BirdMachine/Keywi/commit/35ec1031bba3752d41c74b4bd19cc69d2b2e857d)
Related commits: `60dc42a` (stage selection / preserve scratch), `a124d34` (regression tests), `075a719` (active vs selected tile indicators and accessibility state), `358a3c3` (wire active ID), `35ec103` (accurate status messaging).

**Implemented**
- Saved-theme tiles now **stage a selection** instead of immediately applying it. A selected theme shows its own saved backdrop in the tile; the active keyboard and test bench remain unchanged until the explicit **Apply selected theme** action. **Cancel selection** leaves everything untouched.
- The strip now distinguishes **Active** from **Selected** and exposes descriptive accessibility state on the theme tile. This is not yet a full temporary keyboard-theme preview; only tile artwork is previewed.
- Before explicitly applying another theme while Custom is active, snapshot the **live** Custom controls to the Custom manifest. Reselecting Custom restores scratch edits, even when they were not previously saved via the editor. No saved library theme is modified.
- Reset Custom now requires an explicit destructive-action confirmation, with **Keep Custom** cancellation.
- Added Robolectric Compose regression tests for staging/canceling/applying, preservation and restoration of Custom's modified key border phase, saved-theme immutability, and reset cancellation.

**Verified validation**
- [Keywi CI run 40478213845](https://github.com/BirdMachine/Keywi/actions/runs/40478213845) **completed successfully** on feature SHA `35ec103`. Kotlin formatting, full unit tests (including the new composition regressions), Android lint and debug APK assembly all passed.
- Nonexpired `keywi-debug` artifact ID **12721454020** (ZIP ~32.4 MB), available in that run's Artifacts section. No device or Android emulator visual validation claimed. Previous screenshot evidence remains Robolectric-only.
- Earlier in-flight PR runs were cancelled by the workflow's expected PR concurrency behavior as new commits arrived. Final feature run is green. Draft PR #57 remains unmerged; Dev/main untouched.

**Follow-up research / hazards**
- Investigated `ThemeEngine.importIntoCustom` and `importPackage`: both currently write to Custom and immediately apply the imported theme. That behavior is **not** changed by this session's safe library-selection flow. A later explicit import staging/confirmation design should prevent accidental loss of Custom scratch work and allow importing to the library without automatic activation, especially for future Explore/download workflows.
- A selected tile currently previews its **backdrop**, not a full temporarily themed keyboard. Avoid describing it as a live keyboard preview until the test bench supports read-only theme overrides.
- Next: evaluate safe import staging and a read-only selected-theme preview, or return to expressive non-shader styling (pattern intensity/spacing and lightweight material details). Choose one bounded feature, add regression coverage, and verify another build. Preserve the successful APK above as fallback.

## Session 4 — 2026-10-27 07:00 EDT (complete)

**Verified feature SHA:** [`859dd4fcbc881dbb6f605b36851f2d96e5a522e7`](https://github.com/BirdMachine/Keywi/commit/859dd4fcbc881dbb6f605b36851f2d96e5a522e7)
Related commits: `9761468` (non-destructive JSON/ZIP library import APIs), `0831d95` (stage imported theme in manager), `48bcfb6` (new package/JSON tests), `4d6a3a6` (migration-aware test setup), `859dd4f` (invalid-package cleanup regressions).

**Code and behavior**
- Changed the Theme Manager **Import** UI from destructive `importIntoCustom`/`importPackage` to new `importIntoLibrary`/`importPackageIntoLibrary` methods. Imports now receive unique local theme IDs, appear as a **selected, inactive** library tile, and show explicit instructions to use **Apply selected theme** if desired. Existing active keyboard, live Custom edits, and saved Custom manifest are not replaced by import.
- ZIP packages still validate size, entry paths, assets and optional fonts in staging; new library imports copy bundled assets into their own per-theme directory. If a new library import fails, its destination directory is cleaned up. Importing the same name twice creates separate entries rather than silently overwriting an existing saved theme.
- Retained the legacy import-to-Custom methods for existing automation/callers. This UI change is deliberately scoped to the Theme Manager; review other import callers before any global behavior change.
- Added Robolectric tests proving JSON and ZIP library imports leave Custom and live state intact, preserve packaged GIF bytes, generate distinct IDs, and reject malicious/malformed packages without leaving a visible library theme.
- Draft PR [#57](https://github.com/BirdMachine/Keywi/pull/57) remains **open and unmerged**, targeting Dev; Dev and main unchanged.

**CI / verification**
- [GitHub Actions Keywi CI run 40493948672](https://github.com/BirdMachine/Keywi/actions/runs/40493948672) **completed successfully** for SHA `859dd4f`: Kotlin formatting, full unit suite, Android lint, and debug APK assembly. Verified nonexpired `keywi-debug` artifact **12729962583**, ZIP size 32,406,128 bytes (~32.4 MB), downloadable from the run.
- Initial test run [40493122367](https://github.com/BirdMachine/Keywi/actions/runs/40493122367) failed **one test out of 170** because the test recorded its Custom manifest before the one-time legacy asset migration. That migration legitimately synchronized live Custom state, invalidating the baseline; test setup now calls `ensureMigrated` first. Later CI is fully green. Intermediate CI runs were cancelled due to expected PR concurrency.
- **No Android emulator/physical-device verification** for import picker or keyboard visuals; prior Robolectric slider screenshots remain the only visual evidence. Do not claim the new import interaction has been device-tested.

**Research / remaining opportunities**
- Android's Storage Access Framework permits `ACTION_OPEN_DOCUMENT` / `ActivityResultContracts.OpenDocument` to return a readable URI; since KeyWi copies import bytes immediately to app-owned storage, it does not need persistent access to the source file. Reference: https://developer.android.com/training/data-storage/shared/documents-files
- Potential lifecycle concern for a later focused test: `ThemeManagerScreen` remounts its section on `ON_RESUME`; verify picker-return and pending selection survive that remount on an actual device or instrumented UI test before describing it as fully tested.
- A future **Explore** gallery can use the same stage-before-apply model. Still needed: delete/rename/overwrite confirmation for saved themes, read-only full-keyboard preview of a selected theme, and a visual contrast/accessibility audit of material recipes.
- Next (9 AM final session): inspect the latest CI and this journal, prefer one bounded visually expressive non-shader polish item (e.g., adjustable patterned-slider density/intensity or recipe swatch contrast), and reverify a debug APK. Conclude before 10 AM with a concise final report and direct best verified build link. Do not merge.

## Session 4 follow-on — 2026-10-27 07:28 EDT
- Inspected latest branch commits beyond the prior journal checkpoint: `a7efd8c`, `27eaa1c`, `9a86405` fix picker callback lifecycle and preserve staged selection across Theme Manager refresh. CI [40495234721](https://github.com/BirdMachine/Keywi/actions/runs/40495234721) passed on `9a86405`, with nonexpired `keywi-debug` artifact ID `12730074157` (~32.4 MB). These improvements were already present and were not duplicated.
- Added an independent **Pattern spacing** setting (Fine 4dp / Balanced 6dp default / Open 10dp) for the existing opt-in non-shader slider textures. Spacing persists locally, uses the original Material3 slider semantics, and does not alter theme exports or keyboard input. The spacing chips are disabled when Plain is selected.
- Extended Robolectric slider tests for Fine stipple screenshot/semantics and preference fallback. This checkpoint records intended tests; CI status for this new commit must be verified separately. No device/emulator evidence claimed.
- Next: inspect CI result and artifact; if green, journal the verified SHA and preserve as fallback. If failed, repair in bounded commit. Final 9 AM session should favor polish and morning report.
