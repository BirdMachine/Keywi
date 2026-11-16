# Current map and UX findings

## Evidence and limits

Baseline: `Dev` `8ef014017a020346da3d3e2f8922bb184a51688b`. The navigation registrations in [`MainActivity.kt`](../../app/src/main/java/com/dessalines/thumbkey/MainActivity.kt) contain **23 destinations including Setup and Home**. Routes alone undercount the experience: Boards and Tool pages have internal state pages; Keys & groups has task tabs; theme and sound editors contain expanders; Chroma Desk is a modal.

Reviewed the settings screens and shared editors, IME Quick Settings and Toolbar controls, relevant preference/serialization models, [`settings-consolidation-audit.md`](../settings-consolidation-audit.md), [`gem-settings-language.md`](../gem-settings-language.md), [`gem-cut-settings-redesign.md`](../gem-cut-settings-redesign.md), [ADR-001](../adr-001-gem-cut-axes.md), [Gem Cut invariants](../gem-cut-invariants.md), [theme packaging](../theme-asset-packages.md), and existing guide screenshots. The screenshot inspected describes build 186, so it is historical visual context, not evidence of current Dev layout. No APK/device session was run for this exploration. Scroll burden and discoverability findings are structural inferences; no screen-height or completion-time measurements are claimed.

## Current Home in displayed order

Welcome/release and installation-policy notices precede the menu. Setup is conditional. Home has a test text field below the navigation.

| Home grouping | Entry | Destination / kind |
| --- | --- | --- |
| Before groups | Setup (conditional) | `setup`; Android enable / select steps |
| Before groups | Feedback mode | Cycles stored master feedback gate |
| Advanced | Gem cuts | `gemCuts` |
| Advanced | Themes & appearance | `advancedLookAndFeel` → title **Theme manager** |
| Advanced | Board management | `advancedBoards` → title **Boards** |
| Advanced | Follow keyboard theme in settings | Toggle, not a destination |
| Advanced | Key & Word Selection | `advancedKeyWordSelection` → title **Keys & groups** |
| Advanced | Advanced input | `advancedInput` |
| Advanced | Keypress effects | `keypressEffects` |
| Advanced | Power options | `advancedPowerOptions` → title **Advanced Power Options** |
| Advanced | Debug & diagnostics | `advancedDebug` |
| Settings | Character layouts | Multi-select dialog, not route |
| Settings | Clipboard history | `clipboardSettings` |
| Settings | Backup and restore | `backupAndRestore` |
| Help | Thumb-Key user guide (upstream) | External URL |
| Help | About | `about`; Keywi and upstream links clearly distinguished |

“Advanced” currently contains everyday customization and operational recovery. It describes implementation history more than user intent.

## Complete navigation tree

Indented lists are page hierarchy; **[expand]** means in-page disclosure; **[local]** means state inside one registered destination. All controls and source anchors are in [inventory.md](inventory.md).

- **Home** `settings`
  - **Gem cuts** `gemCuts`: cut selection; About text; live geometry preview with layer tabs.
  - **Themes & appearance / Theme manager** `advancedLookAndFeel`
    - Advanced theme editor `advancedLookAndFeelEditor`: custom-appearance enable, Save to Custom, Revert, test keyboard.
      - Keyboard background **[expand, open initially]** → Main backdrop → mode, opacity, media or gradient.
      - Suggestion bar **[expand, closed initially]** → toolbar backdrop/border/gap; chip fill/border; current-word colors.
      - Keys **[expand, open initially]** → fill; border distribution; border/shadow; cross-link to `advancedKeyWordSelection`.
      - Typography **[expand, closed initially]** → import/system font; suggestion motion style.
      - Cross-link to `keypressEffects` for flash/trails/artwork.
      - Every managed gradient: preset/New; **[expand]** details → angle; **[nested expand]** stops → position/color/delete/add; Save as gradient.
      - Every color: Chroma Desk **[dialog]** → before/after; hue/saturation; brightness; opacity; HEX; **[expand]** RGB; recents; Restore original; Apply/Cancel.
    - Saved themes **[inline]**: library tiles, named-theme Edit or Custom Save as, naming dialog, import, export, Reset Custom.
    - Tasker theme updates `taskerThemeUpdates`: incoming commands, pairing token actions, command examples, temporary override restore/status.
    - Keyboard tuning `lookAndFeel`: display/base palette; geometry and sizing; legend visibility; editor/toast behavior; timing; haptics. Flat scrolling page.
  - **Board management / Boards** `advancedBoards`
    - List **[local]**: enabled toggles, Add, Reorder, Undo deletion.
    - Add **[local]**: Blank / Emoji / Unicode template, name, Create → board editor.
    - Reorder **[local]**: grip or up/down arrows; Done.
    - Board editor **[local]**: A/B; 3×3 cell selection; Tap / Swipes / Hold; direction; action kind/text; fixed navigation-row preview.
      - Board settings **[local]**: name, Enabled, Return to ABC, vibration, hints, Duplicate, Delete → confirmation.
    - Help **[dialog]**.
  - **Key & Word Selection / Keys & groups** `advancedKeyWordSelection`
    - Main / Shifted / Numeric; ME/HK target matrix; selected key(s).
    - Style keys & groups **[task tab, default]** → multi-select/clear/all; saved/new group; membership/name; shared canvas; glyph/fill/border inheritance; Apply / Reset this mode.
    - Actions & values **[task tab]** → selected gesture → Tasker assignment/mode/connection + command label/icon/image; sound and GIF assignments; links to packs.
      - ME character values: nine text fields, Save key, Clear draft. HK does not expose these text fields here.
      - Current word + personal dictionary: two behavior switches.
      - Raw key YAML `modifyKeys`: upstream help link, debounced YAML editor and validation, test field.
  - **Advanced input** `advancedInput`
    - Gestures & typing behavior `behavior`: capitalization/space behavior; swipe distance; slide/hold/deadzones/sensitivity; return/circular drag/actions; ghost keys; test field.
    - Hardware keyboard compatibility `otherSettings`: show IME with physical keyboard.
    - Context engine `contextEngine`: field adaptation plus three dependent switches; capability explanations.
    - Advanced Characters `advancedCharacters` → actually **Tool pages & palettes** (delegates to `ToolPageManagerScreen`).
      - Page order & visibility **[local]**: reorder/show; Characters cannot hide; home; restore defaults.
      - Kaomoji / String palettes **[local]**: collections show/order/create; collection page → rename/symbol/delete; entries show/order/add/edit/delete; text/label/alternates dialogs; section restore.
      - Formatting **[local]**: category and transformation order/visibility; previews; restore.
      - Quick Settings **[local]**: shortcut order/visibility; restore.
      - Cross-link to `advancedBoards`.
  - **Keypress effects** `keypressEffects`: master feedback, flash colors/order/timing/trails, test keyboard.
    - Sounds & sound packs `keypressSounds`: repeated master feedback, active sound source.
      - Generated scale **[expand; initially open when active source]**: root, scale, level, preview.
      - Sound library **[expand; initially open when active source]**: import files/Android sounds; rename/play/remove clips; restore starters.
      - Pack playback & mapping **[expand]**: single/ordered/random/direction; corresponding assignments.
      - Volume, timing & overlap **[expand]**: press/release, overlap, duration limiting, volume.
      - Suggestion sounds **[expand]**: scrolling and selection clips.
    - GIF / PNG overlays `typingOverlay` → top bar also says **Keypress effects**: assets enable/add/play/remove; playback/direction/per-key assignments; origin/layer/timing/duration/interval/overlap/opacity/size; preview. Flat page grows with library and rules.
  - **Power options** `advancedPowerOptions`: Unbridled / Conserved / Restricted / Desperate.
  - **Debug & diagnostics** `advancedDebug`: refresh/clear/export; per-crash expand/export; selectable text viewport; Android exits; logs; Back.
  - **Character layouts** **[dialog]**: multi-select mappings.
  - **Clipboard history** `clipboardSettings`: enable, cleanup/duration, size limiting/maximum, private clipboard.
  - **Backup and restore** `backupAndRestore`: AppDB export/import; AppSettings reset confirmation.
  - **About** `about`: build and external Keywi releases/issues/source; separate upstream source/donations.
- **Setup** `setup`: start destination when not enabled; conditional Home link otherwise.
- **IME Quick Settings** (separate keyboard surface)
  - Suggestions; Feedback mode; custom sound pack source toggle; GIF enable; four power modes.
  - Toolbar controls **[inline panel]**: invert scroll; home view; cycler icon; double/triple tap/hold action; Done.
  - Themes, Tasker themes, Sounds, Effects deep links; Manage tool pages; All settings.
  - Cycler hold defaults to configuring toolbar; single tap toggles suggestions; vertical swipes change pages; double/triple/hold are configurable. Always-available Quick Settings entry points provide recovery.

The activity also accepts a `startRoute` intent extra. Every registered legacy route above must continue to resolve, including names no longer shown as top-level entries. A direct start route has no guaranteed originating Home stack, so explicit Back/Up fallbacks deserve testing.

## Findings with concrete consequences

| Priority | Finding and source evidence | Consequence / proposed response |
| --- | --- | --- |
| High | Advanced Characters → tool manager (`AdvancedInputScreen.kt:265`); Home Key & Word Selection → Keys & groups; legacy `lookAndFeel` → Keyboard tuning | Labels do not predict destinations. Use literal public names and preserve old phrases as search aliases. |
| High | Keyboard tuning has 21 preference rows mixing Room appearance, size, IME behavior and vibration; its `LookAndFeelUpdate` writes a broad object | Restructure by owner, but first separate update boundaries to avoid overwriting another domain. Do not confuse base palette with ThemeEngine themes. |
| High | Keys & groups puts dictionary switches inside Actions; Style is the default tab | A user wanting word-chip behavior must discover an unrelated task switch. Move behavior to Typing; keep chip paint with Suggestion strip. |
| High | Theme editor has four major folds plus gradient details → stops → color modal → RGB disclosure | Multi-level disclosure obscures location and the next action. Use major subpages; reserve local disclosure for secondary numeric details. |
| High | Artwork per-key selector is hard-coded 4×5 plus ID field; matrix editor uses real definitions / HK topology | Two competing targeting experiences. Reuse topology-aware selection, preserve manual expert ID entry; never remap stored IDs as a side effect. |
| High | Toolbar controls live inside IME Quick Settings; Home view duplicated in tool manager; Suggestions enabled is mainly an IME control | Add canonical Tools pages and a Typing suggestions control; retain IME shortcuts backed by the same stores. |
| High | Backup screen exports `AppDB` using `RoomDBExportImport`, while themes/media/boards/tools/feedback use separate stores | “Backup and restore” can imply broader coverage than exists. Explain current database-only scope; navigation migration must not masquerade as a full backup solution. |
| Medium | Home → Themes → Editor → Keys fold → Design → Keys & groups, versus Home → Key & Word Selection | The appearance path requires 3 navigations plus a section action (Keys initially open), versus 1. Deep-link to the same editor with Style intent and preserve return location. |
| Medium | Home → Advanced input → Advanced Characters → Kaomoji → Collection → entry dialog | Long discovery chain with several local pages invisible to route metrics. Expose Tools directly; keep collection details because they are real content scope. |
| Medium | Overlay link/title mismatch; Quick Settings “Manage keypress images & GIFs” opens effects hub | Make shortcut labels describe exact destination and use a shared target-aware artwork editor. |
| Medium | “Enable custom keyboard backgrounds” hides font/key/toolbar editor too | Rename to custom appearance; preserve stored meaning. Show disabled sections with an explanation rather than apparent loss. |
| Medium | Matrix editor uses green terminal chrome excluded from Gem settings scope | A task switch feels like leaving the product. Preserve capability and selection semantics; explore an optional consistent chrome while acknowledging the established Matrix aesthetic. |
| Medium | Global flash/trails, suggestion motion, Room animation timings, power policy all affect motion | Present ownership and suppression status together. Power is an effective runtime policy, not another copy of saved animation values. |
| Medium | Immediate saves, Apply, Save key, Save to Custom, Save as theme, Revert and reset coexist | Name scope on actions. Do not apply one universal Save button across stores without a real transaction model. |
| Medium | Border paint, border width/radius, group canvas and glyph colors span three screens | Group paint and dimensions with contextual links; retain separate stored units and precedence. |
| Medium | Board help says “rooms”/Advanced Input Management while current pages use boards/tool pages; “facet”, “Gem cut”, “matrix”, “layout”, “layer” coexist | Establish controlled vocabulary; aliases preserve familiarity without requiring implementation jargon. |
| Lower | Long libraries (themes/clips/artwork/collections/logs) share vertical space with controls; Home has many multi-line entries and banners | Separate asset browsing from parameter tuning; use one preview and direct row navigation. Verify scrolling with real content before asserting precise reductions. |

### Duplication versus legitimate shortcuts

Actual repeated editing surfaces: **Home view** in tool manager and Toolbar controls; **master feedback** in Home/effects/sounds/Quick Settings; **GIF enable** in artwork and Quick Settings; **power mode** in Home destination and Quick Settings; **key-artwork assignments** in matrix and generic artwork target editor. These should observe the same canonical state; multiple entry points are useful, independent local models are not.

Related but distinct: current-word visibility vs paint; theme borders vs Room width/radius; theme preset vs base palette; main backdrop vs toolbar backdrop vs action artwork; global haptics vs board haptics vs feedback gate; geometry vs character layouts vs custom boards; power policy vs configured timings. Keep all of them.

### Update the interpretation of the October audit

The audit accurately explains why raw YAML, clipboard, legacy routes and separate data axes must survive. Its statement that Advanced Sound is a shell is **stale on this Dev snapshot**: `keypressSounds` wraps a working source/library/generated-scale/mapping editor. The old click field remains retained data; Keyboard tuning now preserves it rather than rendering its old toggle. This exploration adds a dated review instead of rewriting that historical audit as though it described current code.

## Vocabulary to test

| Current phrases | Proposed visible label | Meaning / searchable aliases |
| --- | --- | --- |
| Gem cuts / facet | Geometry · Gem cuts | Physical topology; retain ME-Like/HK-Like names and experimental marker |
| Layouts / Character layouts | Character layouts | Character mappings; “QWERTY”, “layout”, “language” search aliases as appropriate |
| Matrix / Key & Word Selection | Keys & groups | Targeted style and assignments; matrix/remap/modify keys aliases |
| Rooms / Board management | Custom boards | A/B boards with separate content, enabled order and navigation |
| Advanced Characters | Tool pages & palettes | Formatting, kaomoji, snippets, toolbar; Unicode only if separately merged |
| Lozenge | Suggestion chip | Keep lozenge/pill aliases; current-word chip explicitly named |
| Typing overlay / Action artwork | Tap effects → Artwork | GIF/PNG feedback from a gesture, separate from background GIF |
| Advanced input / Context engine | Typing / Field adaptation | Everyday typing behavior and Android text-field policy |
| Sound / Sound pack / Silent | Feedback mode + Sound source | Master gate and source are separate; explicit effective-state copy |

“Advanced” should describe detail depth, never a dumping ground. Do not rename storage enums, stable IDs or routes just to align copy.
