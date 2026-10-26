# Shader feature — overnight handoff and progress

Repository: BirdMachine/Keywi
Working branch: `feature/shadertoy-renderer-layers`
Target: **A downloadable, CI-built debug APK** containing a Shadertoy-compatible GLSL renderer, one usable shader *material* (keyboard background), and one separately enabled/composited shader *effect layer*, with bundled defaults to test. Keep main untouched. This is a night-session handoff, **not instructions for repository agents**.

## User-approved design
- One shader library shared by theme materials and effect layers.
- Theme backdrop can select Shader alongside gradient/solid/GIF/image, with a shader picker and manager shortcut.
- Independent effect layer(s) drawn above/below parts of the keyboard. Initial goal: one independently toggleable effect layer, distinct from the material source.
- Target Shadertoy GLSL `mainImage`, `iTime`, `iResolution`, and touch mapping; report actual implemented subset honestly. Prefer GLES renderer for GLSL, not AGSL-only.
- Ultimately expose KeyWi-specific touch/feedback variables to shaders; include basic touch interaction in prototype only if feasible.
- Defaults OFF unless verified safe; preserve typed input, keyboard lifecycle, and graceful fallback if a shader fails.

## Checked 2026-10-25
- [x] Feature branch created from main.
- [x] Read ComposeKeyboardView.kt, BackdropVisualLayer.kt, BackdropThemePreferences.kt, ThemeEngine.kt, ThemeAssetStore.kt, GemCutArchitecture.kt and app/build.gradle.kts on main.
- [x] Identified keyboard Box ordering and existing backdrop and GIF overlay regions.
- [x] Established overnight checkpoint workflow in this document.
- [x] Add initial GLES 2.0 Shadertoy-style single-pass renderer with mainImage, iResolution/iTime/iFrame/iMouse, frame cap and visibility lifecycle. NOT compiled or integration-tested yet.
- [x] Add two built-in source presets (MOLTEN material and transparent SHIMMER effect), not yet exposed in settings.
- [x] Wire experimental shader material over existing backdrop, behind keys, controlled independently in Theme Manager. NOTE: not yet a native BackdropMode/ThemeDocument material option; still to implement.
- [x] Wire transparent shimmer GLSurfaceView overlay into keyboard Box with independent setting. NEED device validation: GLSurfaceView SurfaceView composition may not honor Compose z ordering; consider TextureView if overlay is misplaced.
- [x] Add two opt-in toggle switches in ThemeManagerSection for material and overlay. Shader picker and editor still pending.
- [x] CI checks passed: Kotlin formatting, 153 unit tests, Android lint and debug assembly on GitHub Actions run 40263720047.
- [x] GitHub Actions artifact `keywi-debug` created in successful run 40263720047; run page https://github.com/BirdMachine/Keywi/actions/runs/40263720047 (contains APK in ZIP).
- [ ] Device-verify Android SurfaceView transparency, draw ordering, touch pass-through, FPS, appearance. APK build passed but shader effects themselves are not device-tested.
- [ ] If suitable, open a draft PR. Do **not** merge without user request.

## Overnight rhythm / safety
- A scheduled task may run every 2 hours but cannot be assumed to run for 1h50, or to be continuously active between runs. Perform a *bounded useful chunk in each invocation*, then commit and update this file before ending.
- Before any modification, reread this document and current branch status, inspect recent commits/CI results, and avoid duplicating already-completed changes.
- If usage telemetry is *actually accessible*, compare remaining 5-hour and weekly quotas: at 25% remaining prioritize finishing current work; at 10% remaining checkpoint/stop expanding. If telemetry is unavailable, do not guess percentages and remain conservative.
- Keep work incremental and resist starting unrelated tasks. Do not automatically purchase usage resets.
- At the end of EACH run, mark done/remaining items; record exactly what changed, tested, build status, blockers, next action, and commit SHA.
- If a previous run left incomplete edits, inspect before continuing. Never overwrite unrelated concurrent changes.
- Avoid claiming an APK exists until a build has succeeded and the artifact is available.
- Stop the recurring automation once target APK is verified (or once explicitly instructed by user); report final state.

## Run log
- 2026-10-25: Branch created; initial code architecture inspected. No shader renderer or APK built yet. Next: prototype GLES shader view composited in keyboard Box with opt-in flags.

- 2026-10-25 first manually initiated shift: committed shader presets `b25a1a5` and renderer `1d9cb2b`. No UI wiring, test run, CI build or APK yet. Next: add Compose AndroidView integration and opt-in control; verify GLES transparency/compositing behavior and compile with CI. The shader renderer source might need lifecycle revisions after device/build testing. Do not mark working until verified.

- 2026-10-25 continuous follow-up: created ShaderExperimentLayer/Preferences `0de294d`, integrated keyboard Box `32ea282`, exposed two switches `6bbd332`. Latest CI run was pending (`40263135344`) at last check; no successful build or APK verified. Need inspect next CI run because updating this document triggers new push and cancels superseded queued runs. Critical: GLSurfaceView transparency/z-order needs real device verification. Next: resolve CI failures, verify artifact; improve composition with TextureView if needed.

- 2026-10-25/26: CI run 40263242741 failed 1/153 unit tests (saved theme tile hidden after extra controls inserted at top). Moved shader switches after saved theme content in commit `e80d8f1`. Rerun `40263720047` completed **success**, uploaded artifact `keywi-debug` (artifact ID 12625442539, expires 2026-11-09). Target proof-of-build APK is available. NOT yet device-validated. Future steps: real-device verification, optional native theme integration, user source editing/library/import/export, iMouse events and Shadertoy channels. Do not claim these are implemented. Note: any progress-file commit creates a new CI run; original successful run remains the verified source for APK.
