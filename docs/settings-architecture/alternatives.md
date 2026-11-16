# Three distinct architectures

All proposals retain the entire [inventory](inventory.md), including expert YAML, all gesture modes, global and per-key effects, imported assets, board A/B behavior, clipboard policy and Tasker. The [CSV](inventory.csv) provides a control-by-control destination in every proposal. The trees below enumerate every destination and reference its complete leaf contents by inventory ID; they do not omit controls that are not common enough to feature on Home.

These are logical navigation trees, not promises to create one Android route for every line. A contextual inspector can present an existing editor without creating another permanent level. Each tree shares the reusable-editor contracts at the end.

## A · Creative Studio

**Mental model:** “What do I want to make Keywi do or look like?” Home is a creative workbench: a keyboard scene, an optional Continue editing shortcut, then seven verb-led destinations. Gem/petal motifs suggest tools around a work surface; no orbiting unlabeled controls or mandatory radial navigation.

- **Dress your keyboard** owns the theme library and whole-keyboard appearance.
- **Shape your keyboard** owns geometry, mappings, boards and targeted key editing.
- **Tune your typing** owns typing policy and content assistance.
- **Make it respond** owns audiovisual/haptic response.
- **Build your toolkit** owns panels, text palettes and toolbar operation.
- **Connect & automate** owns external task and theme integration.
- **App & care** owns power, data, recovery and help.

Advantages: inviting intent-first language; strong “build an instrument” identity; effects become a creative workflow; easy entry for someone who does not know what a facet or YAML is.

Disadvantages: seven destinations use substantial mobile height; “Dress” versus “Make it respond” splits static and animated theme composition; styling a group straddles Dress/Shape; translators must carry the metaphor; users seeking a precise component may need search. A keyboard instrument can still need a literal maintenance area, and App & care must remain obvious.

Realistic journeys: a user assembling a neon keyboard starts in Dress, adjusts key fill, follows “Only some keys?” to Shape/Keys & groups, then enters Make it respond to add flash colors. A user creating a kaomoji palette starts in Build, edits a collection, and enables its page. A battery recovery task goes straight to App & care/Power. These stories are distinct from merely renaming the current Advanced list.

## B · Instrument & Controls

**Mental model:** “Which part or subsystem am I adjusting?” Home resembles a labeled instrument panel, with a small topology preview and eight precise destinations. Component names remain literal; the visual treatment carries personality.

- **Keyboard body**: geometry and dimensions.
- **Keys & assignments**: mappings, individual keys, custom boards and YAML.
- **Surfaces & legends**: saved themes, background, key paint and typography.
- **Typing systems**: gesture engine, field policy, suggestions, clipboard, hardware.
- **Toolbar & tools**: strip appearance, page content and cycler controls.
- **Feedback**: master mode, sounds, haptics, flash and artwork.
- **Connections**: Tasker commands and routines.
- **System**: settings appearance, power, backup, diagnostics and help.

Advantages: precise owner for most low-level controls; easiest to document and predict from component knowledge; geometry and assignments are clearly separated; sound and haptics form a coherent feedback system; appropriate for experienced knob-tweakers.

Disadvantages: eight destinations and several abstract subsystems risk recreating a service manual; building one visual theme crosses Surfaces, Toolbar and Feedback; novices may not distinguish body/keys/surfaces; the strip mixes behavior/content/paint even though those use different stores. A component picker cannot replace the text navigation for TalkBack users.

Realistic journeys: to reduce accidental sliding, open Typing systems/Gestures; to recolor suggestion pills, open Toolbar & tools/Suggestion strip/Appearance; to assign a clip to one gesture, open Keys & assignments/Keys & groups/Actions and select a sound from Feedback’s shared library. The strength is predictable ownership; the cost is creative tasks spanning subsystems.

## C · Hybrid (recommended)

**Mental model:** “Start with a familiar goal; reveal precision inside that goal.” Home has six expressive-but-literal front doors. Each opens a short directory with current-value summaries and named subpages; advanced parameters remain within their owner. There is no global Basic/Expert mode hiding features.

- **Look & feel**: saved themes, background, key surfaces, suggestion-strip paint, typography/motion, Tap effects.
- **Layout & keys**: geometry, size/position, character layouts, custom boards, Keys & groups and YAML.
- **Typing**: gestures, field adaptation, suggestions/dictionary, clipboard, hardware/editor behavior.
- **Feel & sound**: feedback gate, sound source/library and haptics; link to Tap effects.
- **Tools & connections**: tool pages/palettes, toolbar gestures and Tasker.
- **App & care**: power, settings chrome, backup/reset, diagnostics and help.

Advantages: six broad, memorable doors; advanced theme subpages align with #60; preserves creative whole-theme work under Look & feel; Keys & groups has one owner and contextual entry points; separates ordinary typing from appearance; allows canonical adapters around existing stores before any data migration.

Disadvantages: Look & feel versus Feel & sound overlaps in ordinary language; Tap effects needs a clearly marked shortcut from Feel & sound; typography still mixes font selection and motion unless sublabels are explicit; Tools & connections combines two domains to keep Home small. Test those boundaries rather than treating six as a magic number.

Realistic journeys: “Make these four keys one gradient” → Layout & keys/Keys & groups, select group, Style, Fill. Starting from Look & feel/Key surfaces/Selected keys opens that same editor. “Why is my GIF still?” → Look & feel/Tap effects shows the effective power-policy reason and a Power shortcut. “Change triple tap” → Tools & connections/Toolbar controls, with the existing IME entry still available.

## Complete trees and control ownership

Home in each proposal also includes **Search controls**, optional **Pinned**, **Recent controls** (opt-in), **Quick controls** (existing shared values), and a compact **Try typing** preview. Setup and critical notices appear when relevant; release notes do not permanently dominate the first viewport. Below, `Home` denotes these common controls, not a ninth/eighth/seventh category. `Shared editors` are contextual surfaces, not extra Home destinations.


### A · Creative Studio navigation

- Home
  - Quick controls **[Q02–Q09]**
- Dress your keyboard
  - Theme library **[T01–T06]**
  - Theme workspace **[E01–E07]**
  - Background **[BG01–BG04]**
  - Suggestion strip **[SB01–SB11]**
  - Key surfaces **[KS01–KS08, BD01–BD03]**
  - Type & motion **[F01–F03, LM01–LM02]**
  - Base & legends **[LA01–LA06]**
- Shape your keyboard
  - Character layouts **[H02]**
  - Geometry **[G01–G02]**
  - Size & position **[L01–L09]**
  - Keys & groups **[M01–M14]**
    - Style **[MG01–MG10]**
    - Expert YAML **[Y01–Y02]**
  - Custom boards **[B01–B17]**
- Tune your typing
  - Editor behavior **[LI01–LI02]**
  - Suggestions & dictionary **[DW01–DW02, Q01]**
  - Gestures **[BE01–BE15]**
  - Field adaptation **[CX01–CX04]**
  - Hardware keyboard **[HW01]**
  - Clipboard **[CL01–CL06]**
- Make it respond
  - Feedback mode **[H01]**
  - Haptics **[LH01–LH02]**
  - Tap effects **[FX01–FX08]**
    - Artwork **[AO01–AO20]**
  - Sounds **[SO01–SO21]**
- Build your toolkit
  - Tool pages **[TP01–TP20]**
  - Toolbar controls **[TC01–TC05]**
- Connect & automate
  - Key tasks **[TK01–TK07]**
    - Command symbol **[TI01–TI04]**
  - Theme routines **[TA01–TA09]**
- App & care
  - Setup **[H03]**
  - Settings appearance **[V01]**
  - Power **[P01]**
  - Backup & reset **[BA01–BA03]**
  - Diagnostics **[DG01–DG06]**
  - Help & about **[HELP01–HELP05]**
- Shared editors
  - Gradient **[GR01–GR09]**
  - Color **[C01–C08]**

### B · Instrument & Controls navigation

- Home
  - Quick controls **[Q02–Q09]**
- Keyboard body
  - Geometry **[G01–G02]**
  - Size & position **[L01–L09]**
- Keys & assignments
  - Character layouts **[H02]**
  - Keys & groups **[M01–M14]**
    - Style **[MG01–MG10]**
  - Expert YAML **[Y01–Y02]**
  - Custom boards **[B01–B17]**
- Surfaces & legends
  - Theme library **[T01–T06]**
  - Theme workspace **[E01–E07]**
  - Background **[BG01–BG04]**
  - Key surfaces **[KS01–KS08, BD01–BD03]**
  - Typography & motion **[F01–F03, LM01–LM02]**
  - Base & legends **[LA01–LA06]**
- Typing systems
  - Editor behavior **[LI01–LI02]**
  - Suggestions & dictionary **[DW01–DW02, Q01]**
  - Gestures **[BE01–BE15]**
  - Field adaptation **[CX01–CX04]**
  - Hardware keyboard **[HW01]**
  - Clipboard **[CL01–CL06]**
- Toolbar & tools
  - Suggestion strip
    - Appearance **[SB01–SB11]**
  - Tool pages **[TP01–TP20]**
  - Toolbar controls **[TC01–TC05]**
- Feedback
  - Feedback mode **[H01]**
  - Haptics **[LH01–LH02]**
  - Tap effects **[FX01–FX08]**
    - Artwork **[AO01–AO20]**
  - Sounds **[SO01–SO21]**
- Connections
  - Key tasks **[TK01–TK07]**
    - Command symbol **[TI01–TI04]**
  - Theme routines **[TA01–TA09]**
- System
  - Setup **[H03]**
  - Settings appearance **[V01]**
  - Power **[P01]**
  - Backup & reset **[BA01–BA03]**
  - Diagnostics **[DG01–DG06]**
  - Help & about **[HELP01–HELP05]**
- Shared editors
  - Gradient **[GR01–GR09]**
  - Color **[C01–C08]**

### C · Hybrid navigation

- Home
  - Quick controls **[Q02–Q09]**
- Look & feel
  - Theme library **[T01–T06]**
  - Theme workspace **[E01–E07]**
  - Background **[BG01–BG04]**
  - Suggestion strip **[SB01–SB11]**
  - Key surfaces **[KS01–KS08, BD01–BD03]**
  - Typography & motion **[F01–F03, LM01–LM02]**
  - Base & legends **[LA01–LA06]**
  - Tap effects **[FX01–FX08]**
    - Artwork **[AO01–AO20]**
- Layout & keys
  - Character layouts **[H02]**
  - Geometry **[G01–G02]**
  - Size & position **[L01–L09]**
  - Keys & groups **[M01–M14]**
    - Style **[MG01–MG10]**
    - Expert YAML **[Y01–Y02]**
  - Custom boards **[B01–B17]**
- Typing
  - Editor behavior **[LI01–LI02]**
  - Suggestions & dictionary **[DW01–DW02, Q01]**
  - Gestures **[BE01–BE15]**
  - Field adaptation **[CX01–CX04]**
  - Hardware keyboard **[HW01]**
  - Clipboard **[CL01–CL06]**
- Feel & sound
  - Feedback mode **[H01]**
  - Haptics **[LH01–LH02]**
  - Sounds **[SO01–SO21]**
- Tools & connections
  - Tasker
    - Key tasks **[TK01–TK07]**
      - Command symbol **[TI01–TI04]**
    - Theme routines **[TA01–TA09]**
  - Tool pages **[TP01–TP20]**
  - Toolbar controls **[TC01–TC05]**
- App & care
  - Setup **[H03]**
  - Settings appearance **[V01]**
  - Power **[P01]**
  - Backup & reset **[BA01–BA03]**
  - Diagnostics **[DG01–DG06]**
  - Help & about **[HELP01–HELP05]**
- Shared editors
  - Gradient **[GR01–GR09]**
  - Color **[C01–C08]**

## Detailed subtrees reused at the mapped destinations

Attach each subtree below at its named destination in A, B and C above. These are part of all three proposed navigation trees. Parent inventory paths describe ownership; the following detail specifies which concepts become subpages versus local controls. Library-item pages repeat by stable item ID, not by creating a unique hard-coded route for every user item.

- **Theme library** → select theme; Edit opens **Theme workspace**; Save as naming dialog; Import; Export; Reset Custom. T01–T06 retain their current scopes.
- **Theme workspace** → Background; Suggestion strip; Key surfaces; Typography & motion (Type & motion in A); Tap effects shortcut. Enable custom appearance / Save to Custom / Revert remain at the workspace boundary, E01–E07. Each appearance page is also reachable directly from its family directory.
- **Background** → mode/opacity/media controls, BG01–BG04 → **Gradient editor** when Colorful.
- **Suggestion strip** → **Toolbar surface** (background, border, gap; SB01–SB04) and **Chips & current word** (SB05–SB11). Each color/gradient opens the shared editor; visibility behavior links to Suggestions & dictionary.
- **Key surfaces** → global fill/border/shadow controls, KS02–KS08 and BD01–BD03; dimensions shortcut to Base & legends; **Selected keys** (KS01) opens Keys & groups / Style. Do not fork its state.
- **Keys & groups** → selection/context (M01–M06) → **Style** (MG01–MG10); **Actions & values** → selected gesture (M10) → **Action inspector** with Tasker (TK01–TK07), command symbol (TI01–TI04), Sound (M11), Artwork (M12), pack links (M13); **Character values** (M07–M09); **Expert YAML** (M14, Y01–Y02). Style/Actions are tasks in the same editor; Character values and Gesture Actions can retain #64’s independent disclosures. Suggestions/dictionary is now a cross-link to Typing, not stored here.
- **Custom boards** → **Board list** (B01/B04/B16/B17) → **Add board** (B02–B03); **Reorder** (B04); **Board editor** → A/B, cell, Tap/Swipes/Hold, action and text (B05–B09) → **Board options** (B10–B15). Preserve Help, Delete confirmation and Undo. Board options is not a second category on Home.
- **Gestures** → **Typing basics** (BE01–BE03); **Swipe & slide** (BE04–BE10); **Return & circular gestures** (BE11–BE14); Ghost keys (BE15). Use named subpages if content exceeds a comfortable scan; do not create fifteen screens for fifteen toggles. Dependencies remain visible in summaries.
- **Tool pages** → **Order & visibility** (TP01–TP04); **Kaomoji** (TP05); **String palettes** (TP06); **Formatting** (TP15–TP17); **Quick Settings layout** (TP18–TP19); character-board shortcut (TP20).
  - Kaomoji / String palettes → **Collections** → **Collection editor** (name/symbol, enabled/order, delete; TP07–TP08) → **Entry editor** (text, optional label, alternates, enabled/order, add/delete; TP09–TP13). Restore defaults is section-scoped (TP14), with a scope summary and confirmation.
  - Formatting → category order/visibility; transformation order/visibility; previews; section restore. These can be tabs or short pages, not a nested route per style.
- **Toolbar controls** → inversion/home/icon (TC01–TC03) → **Gesture actions** (double/triple/hold, TC04); Done (TC05). A visible IME entry remains as an accelerator.
- **Sounds** → **Source** (SO01); **Generated scale** (SO02–SO05); **Sound library** (SO06–SO11) → rename dialog / explicit preview / remove; **Playback & assignments** (SO12–SO14); **Timing & mix** (SO15–SO19); **Suggestion sounds** (SO20–SO21). These are named pages instead of a growing bank of accordions. Choosing a per-key sound uses the same library with a target breadcrumb.
- **Tap effects** → **Flash & trails** (FX01–FX08); **Artwork** → **Artwork library** (AO01–AO04); **Playback rules** (AO05–AO07); **Per-key artwork** (AO08–AO11) opens topology-aware target inspector, with expert ID input retained; **Placement & timing** (AO12–AO19); Preview (AO20). Full libraries do not push all timing controls farther down one page.
- **Tasker** in Hybrid is a short directory → **Key tasks** (target assignment inspector) and **Theme routines** (TA01–TA09). A/B expose those directly under Connections. Command symbol edits belong to the selected key/gesture inspector, not another binding store.
- **Diagnostics** → snapshot/refresh/clear/export, crash list → **Report details** (DG04–DG05); logs/exits remain readable and exportable. An expandable report is appropriate progressive disclosure.
- **Shared Gradient editor** → preset/New, angle, stop list → **Stop detail** (position/color/remove) → **Color editor**; Add stop; Save as gradient. GR01–GR09. Local disclosure is acceptable for stop detail; do not hide the whole Background page inside it.
- **Shared Color editor** → disc, brightness, opacity, HEX, optional RGB channels, recent colors, Restore original, Apply/Cancel (C01–C08).

Field adaptation, hardware keyboard, Clipboard, Feedback mode, Haptics, Base & legends, editor behavior, Power, settings appearance, backup/reset, setup and help/about remain short leaf pages/dialogs as mapped in the trees and inventory. They need no artificial extra directory. A direct user-guide link, release/installation notices and Try typing remain available without introducing stored preferences.

## Shared editor and discovery contracts

- **Keys & groups** is the canonical targeted editor, with *Style*, *Actions* and expert *Values/YAML* capability. In all trees, Styles reuse MG controls, Actions reuse M/TK/TI plus sound/artwork pickers. “Key tasks” in Connections is an assignment list/shortcut into the same target-aware inspector, not another Tasker binding implementation. No second writable copy of key state.
- **Theme workspace** is the library’s edit entry and session boundary. The four current major folds become the named Background, Suggestion strip, Key surfaces and Typography pages. Their inventory entries E04–E07 map to those destinations as navigation affordances; the CSV’s workspace mapping preserves their current entry identity. The current cross-link KS01 opens Keys & groups with Style selected.
- **Color** opens Chroma Desk with explicit owner, initial value and apply/cancel callbacks. Keep numeric channels, opacity, validation, recents and restore. The spectrum disc always has numeric and assistive alternatives. No full-screen gesture-only color wheel.
- **Gradient** reuses presets, angle, editable stops, colors and Save as for background, toolbar, chips, global key surfaces and key/group overrides. Editing stop color returns to the selected stop with target/scope retained. Swatches do not replace textual names.
- **Sound and artwork libraries** own assets once. Global rules and per-key inspectors choose the same asset IDs. “Manage pack” returns to the invoking target, gesture and scroll position. Library removal reports affected assignments; do not silently repoint missing clips.
- **Boards and collections** retain meaningful internal pages. Flattening all entries into Home or one scrolling “customization” page would make the system less usable. Maintain stable board/collection/entry identity.

### Progressive disclosure

Show current source, effective state and one representative control before the next detail page. Prefer pages for major concepts; use disclosure for numeric color channels, per-stop detail and diagnostic text. A collapsed summary should say “Four colors · 600 ms · trails off”, not “Advanced settings”. Never require expanding a section to learn whether it is configured. Do not introduce a global expert toggle or remove unique controls to achieve shorter pages.

### Search

Use one local control catalog keyed by stable, nonlocalized control IDs and backed by the existing stores. Index label, parent path, plain-language description, aliases and availability rules. Search “fade”, “flash” or “tap colors” should return the same destination; “dictionary” should return the Typing behavior control and relevant help, not every color containing “word”. Results show path and a useful summary; selecting one navigates, reveals, scrolls to and focuses the exact control.

Keep an unavailable control searchable: show “Needs custom appearance”, “Requires Slide”, “No imported artwork” or “Suppressed by Restricted power”. Route to its owning page and explain the dependency; do not silently turn features on. Unknown removed IDs resolve to a safe owner with explanation. No token values, task names, typed text, clipboard content or user snippets in the settings index.

### Pinned and recent controls

Provide a visible star/pin action from each control’s overflow with a maximum of six Home pins and a full editable list. Empty state uses examples plus a Pin action, not an unsolicited permanent dashboard. Pin stable control/target references; label a targeted pin “Arrow group · fill · Main”. If a target disappears, mark the pin unavailable and offer removal; do not bind it to another key/group with the same display name.

Recent controls should be opt-in, on-device, capped (suggest 8), clearable, and limited to control IDs, target references and timestamps. Never store previous values, entered text, secrets or clip contents. Exclude token-copy and diagnostics-sharing actions from automatic recents. Pin/recent shortcuts lead to controls, never execute a Tasker task or destructive operation. The current kaomoji/color recents are separate data and remain intact.

### Contextual shortcuts

Offer explicit Edit actions alongside a preview component or current value. A long press may accelerate access, but must have a visible button equivalent. A layout preview selection is not a keypress and never types or launches a task. The IME retains Quick Settings and All settings escape routes even if toolbar gestures change. Editing from the IME should open a clearly labeled editing surface and return predictably, not overlap a second window over typing.

### Future-only controls

Settings search, settings favorites/recents, target-aware route arguments, status explanations, unified asset inspectors and cross-page edit-session handling are proposals. They are not baseline features. Unicode from #58 and any optional gesture-label size preference from #62 enter the same registry only after their implementations are reviewed; they do not justify speculative rows in the current inventory.
