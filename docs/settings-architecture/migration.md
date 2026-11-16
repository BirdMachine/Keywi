# Recommendation and incremental migration

## Choose Hybrid, then validate its boundaries

Use the six-door Hybrid architecture in [alternatives.md](alternatives.md). The initial value is predictable ownership and direct access, not a wholesale visual rewrite. Let the component precision of Instrument & Controls shape the subpages, and let Creative Studio influence previews, examples and delight.

Recommended Home order: **Look & feel**, **Layout & keys**, **Typing**, **Feel & sound**, **Tools & connections**, **App & care**. A compact preview, search and optional pins sit above; a Try typing action is available in editors. Do not force all categories above the fold at large text by shrinking labels or targets. Scroll is acceptable; unstructured scrolling is the problem.

### Non-negotiable preserved behavior

- Theme owns appearance; Gem Cut owns topology; character layout owns mappings; modes/layers change assignment context. A new menu never switches a cut or layout implicitly.
- Existing custom boards keep UUIDs, sides, order, enabled state and every binding. Keep text strings, emoji sequences and combining marks unchanged.
- Per-key appearance IDs remain row:column; sound/artwork/Tasker IDs retain their existing key/gesture forms. New display names never rewrite these IDs. Group mode designs/canvas settings remain separate; individual overrides still take priority across modes.
- Every current unique control remains available, including full raw YAML, clipboard policy, generated sound, global/directional/per-key effects, hardware behavior, diagnostics and automation.
- Editing a named theme continues in Custom. Import/export format is not silently upgraded by a navigation change. Legacy manifests, readable media resolution and missing-asset errors remain supported.
- Master feedback and power policies gate output without destroying saved compositions. Preserve the distinction between configured state and effective runtime state.
- A settings preview never types, dispatches a gesture, or runs Tasker. Assigning a task still does not execute it.

## Canonical ownership and existing stores

| Domain | Preserve today | Proposed adaptation boundary |
| --- | --- | --- |
| Room AppSettings | Layout indexes/sets, raw YAML, geometry dimensions, base theme/mode, typing/clipboard/hardware fields | Small field/domain update functions; avoid broad stale `LookAndFeelUpdate` copies after extraction |
| ThemeEngine | Custom/saved IDs, manifests, relative assets, schema 3 import/export at reviewed baseline | Existing engine APIs wrapped by one editing-session owner; no store replacement |
| Surface preferences | KeyTheme, Backdrop, Toolbar, border/gap, chips, Font, SuggestionMotion | Expose existing stores through one descriptor registry; themes continue to capture the same fields |
| Key groups/overrides | IDs, membership, legacy shared style and mode-specific design/canvas maps | Editor target carries mode explicitly; preserve older group inheritance; no coordinate migration |
| Typing/context | Behavior fields, ContextEnginePreferences, AdvancedKeyWordPreferences, SuggestionPreferences | Typing directory with named owner pages and dependency summaries |
| Custom boards | CustomBoardPreferences JSON; UUID, sides, order, bindings | Reuse current editor, optionally add task-aware entry later |
| Tools | ToolPagePreferences order/hidden lists/content; ToolbarViewPreferences home/icon/gestures/inversion | One canonical toolbar-control adapter; existing IME surface remains a client |
| Feedback | FeedbackPreferences, ActionSoundPreferences, AccentFeedbackPreferences, ActionGifPreferences and owned assets | Sound/Artwork libraries share IDs with target inspectors; don't mirror enable flags in new state |
| Tasker | TaskerKeyBindings and command displays; theme automation enable/token/snapshot | Incoming theme commands and outgoing key tasks remain separate destinations; never index secrets |
| Power/chrome | KeywiPowerPreferences; SettingsAppearancePreferences | Effective-state readout; no destructive conversion of theme preferences into power presets |
| Data/diagnostics | AppDB export/import; separate ClipboardDB; local diagnostics | Accurate coverage labels; full-device backup, if desired, is a separate feature |

Before any later implementation, produce a key-by-key store/serialization contract from the then-current code and compare it with this inventory. These domain names identify existing APIs, not a new storage schema authorized by this PR.

## Compatibility routes

All **23** current routes stay registered or become explicit aliases. Strings may be used by IME `startRoute` extras and tests even where they are internal; do not assume only visible Home links matter. Suggested targets below are logical pages, not required new route strings. Alias resolution must not reset a store or create a blank theme.

| Existing route | Hybrid target / compatibility behavior |
| --- | --- |
| `setup` | Existing setup flow; preserve permission and system-selector behavior |
| `settings` | New Home when enabled; old Home when flag off |
| `lookAndFeel` | Compatibility Keyboard tuning page initially; later directory of extracted controls with position/focus links |
| `advancedBoards` | Layout & keys → Custom boards |
| `gemCuts` | Layout & keys → Geometry · Gem cuts |
| `typingOverlay` | Look & feel → Tap effects → Artwork; preserve selected asset/target if supplied |
| `keypressEffects` | Look & feel → Tap effects; keep obvious sound and feedback links |
| `keypressSounds` | Feel & sound → Sounds |
| `advancedLookAndFeel` | Look & feel → Theme library |
| `taskerThemeUpdates` | Tools & connections → Tasker → Theme routines |
| `advancedLookAndFeelEditor` | Look & feel → Theme workspace; begin/reuse existing Custom edit session correctly |
| `advancedKeyWordSelection` | Layout & keys → Keys & groups; default Style, existing capability preserved |
| `behavior` | Typing → Gestures |
| `advancedInput` | Typing directory plus labeled links for relocated tool/hardware controls |
| `contextEngine` | Typing → Field adaptation |
| `advancedCharacters` | Tools & connections → Tool pages & palettes |
| `advancedPowerOptions` | App & care → Power |
| `advancedDebug` | App & care → Diagnostics |
| `clipboardSettings` | Typing → Clipboard |
| `modifyKeys` | Layout & keys → Keys & groups → Expert YAML |
| `about` | App & care → Help & about |
| `backupAndRestore` | App & care → Backup & reset; preserve database operation semantics |
| `otherSettings` | Typing → Hardware keyboard |

Old `lookAndFeel` contains several domains. Keep its compatibility composite until links can focus exact new controls; redirecting it to just appearance would hide sizing, haptics and editor behavior from existing callers. Old `advancedInput` must likewise retain relocation links. Avoid an alias chain that builds multiple duplicate Home entries in Back.

## Later target-aware editor contract

An implementation can introduce a typed navigation request with optional **control ID**, **cut/geometry**, **layout reference**, **mode**, **key or group ID**, **gesture**, **task** and **return destination**. Prefer stable references over display names. Validate every target against the current definition/store; a missing group opens the group list with explanation rather than selecting another group. Do not serialize typed text, tokens, clipboard contents or entire theme documents in route arguments.

Opening Keys & groups from appearance selects Style; from sound/artwork chooses Actions with the relevant key/gesture. Switching preview mode is local until an explicit stored-control action occurs. A context change must clearly announce which values affect one mode versus all modes. Do not imply custom-board actions share an ID system with ME/HK runtime effects if their existing stores do not.

Preserve selection, scroll, current stop and modal draft across ordinary navigation and rotation. Persist only minimal draft state across process death, scoped to the same stable target and initial revision; if the source changed, show a conflict/reload choice rather than replaying a stale broad object. Explicit Up must return safely to an owner/Home when launched directly via `startRoute` with no parent.

### Save and undo semantics

Preserve per-control save behavior during initial routing: live sliders/toggles, Apply group style, Save key for matrix text, color-dialog Apply/Cancel, Save to Custom and Save as theme. Label the scope near the action. Do not claim that “Revert theme” reverses Tasker assignments, Room typing values or action artwork that ThemeEngine does not capture.

Before splitting the theme workspace across pages, lift the opening theme snapshot and Custom-fork/session ownership above those destinations. Returning from a color or asset page must not replace the original opening snapshot. Back navigation alone is not a save/cancel operation. Any proposed undo history requires real per-domain support; it is not a cosmetic button.

## Phased plan (future implementation only)

| Phase | Concrete scope | Gate before advancing | Rollback |
| --- | --- | --- | --- |
| 0 · Agree on the map | Review this exploration; first-click tests for six-door names; reconcile #60/#62 and pending schema/library changes | Direction chosen; updated control mapping; no implementation in this PR | Documentation only |
| 1 · Add a catalog and aliases | Read-only descriptors with stable IDs, labels/aliases, owner, availability and canonical route; no preference rename; automated coverage check for current routes/controls | Every catalog entry resolves; legacy `startRoute` coverage; no writes from registry lookup | Stop using registry; old routes untouched |
| 2 · New front doors | Feature-flagged Home and short directories; existing screens behind them; visible tool manager / toolbar recovery links | Current controls reachable in both shells; original data unchanged; Back/Up/direct-launch checks pass | Switch shell flag off; same stores |
| 3 · Extract broad pages by owner | Adopt #60’s theme subpages if merged; move dictionary behavior, dimensions/haptics/editor options to owners; split broad update boundaries before moving writers | Regression fixtures show unchanged unrelated values; session/Revert verified; no duplicate save model | Route aliases back to original composite screens |
| 4 · Contextual reusable editors | Shared topology selection and Color/Gradient/Sound/Artwork inspectors; preserve #62 fitting and collapsible sections; task/mode/target deep links | ME/HK, main/shift/numeric, group inheritance and per-gesture assignments round-trip; no task executes during editing | Retain existing editor destinations; adapters still use same stores |
| 5 · Discovery and delight | Local search; optional pins and opt-in recents; effective-policy summaries; restrained glass/petal language | Search dependency/alias tests; keyboard/TalkBack/text-scale checks; lower-power visuals; user task study | Disable discovery layers independently; keep core directory usable |
| 6 · Remove redundant writable surfaces carefully | Convert old duplicate entry UIs to shortcuts to canonical controls; keep legacy route aliases | Full inventory has owners; migration telemetry only if separately authorized; existing guides updated | Keep prior adapter version and old shell until at least one reviewed release cycle |

Use several reviewable PRs, with no automatic merge or release implied. Recheck `Dev` at the start of each; a promotion to `main` does not require rebasing this documentation onto unrelated UI branches.

## Compatibility acceptance matrix

Before the future UI migration, capture representative fixtures for default install, heavily customized Room/YAML, saved theme with GIF/font and missing-media legacy theme, mode-specific/older groups and individual overrides, ME/HK targets, boards A/B and disabled order, tool palettes and customized cycler, sound source/pack/global+key rules, artwork ALL+gesture rules, Tasker bindings/displays and temporary theme override, each power mode, clipboard settings and database export/import.

For each fixture: open old shell → inspect all relevant new pages → close/relaunch → switch back to old shell → compare stored values and owned asset bytes. Reading a page must be byte/state neutral apart from explicitly documented existing lazy migrations; opening a named theme for editing must retain its named copy. Exercise writes individually and verify unrelated fields remain unchanged. Keep real secrets and personal assets out of Git.

Required checks: default and large libraries; disabled dependency explanations; root and nested Back/Up; direct `startRoute`; rotation/process recreation; IME-to-settings return; ME/HK topology and widths; Shift/Numeric group styles; individual-over-group precedence; shared canvases with gaps; raw YAML additional modes/actions; missing clips/media; sound gate/source separation; Restricted/Desperate suppression without saved-value changes; database-only restore scope; theme ZIP compatibility; TalkBack reading/focus/selection and hardware-keyboard access; 320/360/412dp, 100/150/200% text, dark/light/reduced-transparency/reduced-motion appearances.

Automated tests should verify state contracts and meaningful navigation behavior. Device checks remain necessary for gesture text fitting, touch targets, blur legibility, actual file providers, font fallback and IME interactions. A successful build alone does not validate these.

## Explicit deferrals

A complete backup archive across every store; schema 4 adoption; theme delete/overwrite library work; Unicode implementation; shader materials; changes to gesture fitting/hitboxes; new swipe transitions; group deletion/reordering and per-gesture font sizing if not yet implemented. These may be good work, but this navigation exploration must not smuggle them into migration scope or treat wishlist controls as current functionality.
