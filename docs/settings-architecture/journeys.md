# User journeys and evaluation

Paths start at Settings Home unless marked IME. `→` means a navigation/entry choice, not a measured tap count. Local tabs/disclosures are named so shallow route counts do not conceal work. Selection, value adjustment and confirmation steps remain necessary in every proposal. Search/pins are accelerators, not dependencies.

## Same tasks across architectures

| Task and realistic intent | Current baseline path | Creative Studio | Instrument & Controls | Hybrid |
| --- | --- | --- | --- | --- |
| Give four letter keys one continuous gradient | Key & Word Selection → Style default → Main/Shift/Numeric + ME/HK → multi-select → keys → group fill → gradient detail; theme route is Themes → Editor → Keys fold → Design | Shape → Keys & groups → select four → Style → group fill | Keys & assignments → Keys & groups → select four → Style | Layout & keys → Keys & groups → select four → Style; Look & feel → Key surfaces → Selected keys opens same inspector |
| Fade pink to gold after a tap | Keypress effects → flash frame colors/order/duration, each color opens Chroma Desk | Make it respond → Tap effects → Flash | Feedback → Tap effects → Flash | Look & feel → Tap effects → Flash; Feel & sound contains a labeled shortcut |
| Use a GIF behind keys but not on tap | Themes → Advanced theme editor → Keyboard background → GIF → media | Dress → Background → GIF | Surfaces & legends → Background → GIF | Look & feel → Background → GIF; search distinguishes “background GIF” from “tap artwork” |
| Play one sound on an upward swipe of a specific key | Keys & Word Selection → Actions → key → Up → Sound; pack import requires link out | Shape → Keys & groups → Actions → key/Up → shared Sound picker | Keys & assignments → Keys & groups → Actions → key/Up → Sound | Layout & keys → Keys & groups → Actions → key/Up → Sound; Manage pack returns with context |
| Reduce accidental cursor sliding | Advanced input → Gestures & typing behavior → slide, sensitivity, deadzones | Tune → Gestures → Sliding | Typing systems → Gestures → Sliding | Typing → Gestures → Sliding; visible enabled/dependency summaries |
| Hide unfinished-word chip | Key & Word Selection → switch from default Style to Actions → scroll past assignments → dictionary switches | Tune → Suggestions & dictionary | Typing systems → Suggestions & dictionary | Typing → Suggestions & dictionary; paint link is separate |
| Put a kaomoji collection on the toolbar | Advanced input → Advanced Characters → Kaomoji → collection/entry editor; Page order if hidden | Build → Tool pages → Kaomoji; Page order shortcut | Toolbar & tools → Tool pages → Kaomoji | Tools & connections → Tool pages → Kaomoji; page visibility shown alongside preview |
| Make triple tap return to keyboard | IME Quick Settings → Toolbar controls → Triple tap → Home; Home separately selected elsewhere | Build → Toolbar controls → Triple tap; Home view summary beside it | Toolbar & tools → Toolbar controls | Tools & connections → Toolbar controls; IME shortcut retained |
| Turn off decoration when battery is low | Power options → Desperate; also IME Quick Settings | App & care → Power; existing IME quick control | System → Power; existing IME quick control | App & care → Power; optional Home pin and existing IME quick control |
| Explain why imported artwork is not playing | Effects → artwork enable/rules; separately inspect Power and possibly custom appearance scope | Tap effects status → App & care/Power | Feedback status → System/Power | Tap effects shows “Saved and enabled; paused by Restricted power” → Power shortcut; does not alter artwork enable flag |
| Bind a task while keeping the original character | Keys & Word Selection → Actions → key/gesture → Also run task → picker; connection and display controls inline | Connect → Key tasks → target inspector, or Shape → Actions | Connections → Key tasks → target inspector, or Keys → Actions | Tools & connections → Tasker → Key tasks → target inspector, or Layout → Actions; assignment never runs a task |
| Export a portable visual theme | Themes → active Custom/save as → Export → Android destination picker; schema 3 scope only | Dress → Theme library → Export | Surfaces & legends → Theme library → Export | Look & feel → Theme library → Export; explicit package contents and saved destination |
| Back up typing settings | Backup and restore → Backup database; themes/other stores separate | App & care → Backup & reset → Database export | System → Backup & reset → Database export | App & care → Backup & reset → Database export; coverage summary prevents assuming a complete-device backup |
| Change an expert non-text key action | Keys & Word Selection → Actions → Raw YAML; matrix text fields are insufficient | Shape → Keys & groups → Expert YAML | Keys & assignments → Expert YAML | Layout & keys → Keys & groups → Expert YAML; retain full schema and validation |

## Worked Hybrid journeys

### Group gradient without losing mode context

1. Open Layout & keys → Keys & groups. Header states **ME-Like · Main · English layout** (actual selected mapping in implementation, not a hard-coded assumption).
2. Choose Multiple keys with a visible selection control; tap four keys. The diagram and list alternative both announce selection count.
3. Create group “Petals”, choose Style → Fill → Gradient. A text summary states that a group style affects Main; individual overrides take priority.
4. Edit two colors and stop positions using shared editors. Cancel color returns without applying its draft; Apply color returns to the same stop.
5. Apply group style writes the group membership/style using the existing store. Reset this mode restores inheritance only for that mode. A conflicting individual override is identified, not silently cleared.
6. Back returns to the invoking Key surfaces page when entered from there. Selection, mode, gesture and scroll state survive the library/color detour.

The illustrative wireframe uses a schematic ME-like 3×3 selection diagram with rail/navigation context; a production editor must derive all cells and widths from the active definition and provide access to the full topology. HK never inherits the assumption that it has a 3×3 center.

### Silence without losing a sound composition

1. Choose Feel & sound → Feedback mode → Silent, or the same mode in Quick Settings.
2. Sound source, pack assignments, root/scale and volumes remain saved. The sound page states **Silent mode is muting playback** with a Change mode shortcut.
3. Returning to Sound re-enables allowed audio without substituting the sound source. Do not recreate the current “Use custom sound pack” boolean as a universal sound enable: it currently changes source to Clips/Default and can replace Generated.

### Resume after a policy interruption

1. Tap effects shows four saved flash frames and enabled artwork; a visible status strip explains Restricted power suppression.
2. Open Power from that strip; select Conserved if desired. Merely inspecting Power does not change anything.
3. Back returns to Tap effects at the same control; saved durations and library selection remain unchanged. Do not “fix” suppression by overwriting the user’s effect preferences.

## Comparative assessment

These scores are **design judgments to guide discussion**, not user-study results. Scale 1–5, higher is better. Weighted total is `sum(weight × score) / 5`, out of 100.

| Criterion | Weight | Creative Studio | Instrument & Controls | Hybrid |
| --- | ---: | ---: | ---: | ---: |
| Creative identity and invitation | 20 | 5 | 3 | 5 |
| First-time destination prediction | 15 | 4 | 2 | 4 |
| Expert control predictability | 15 | 3 | 5 | 4 |
| Coherent whole-theme workflow | 15 | 4 | 2 | 5 |
| Typing/gesture task clarity | 10 | 4 | 5 | 4 |
| Recovery and operational clarity | 10 | 3 | 4 | 4 |
| Incremental adoption around current stores | 10 | 3 | 4 | 5 |
| Accessible nonvisual navigation | 5 | 4 | 4 | 4 |
| **Weighted total** | **100** | **77** | **69** | **89** |

Hybrid’s weakest boundary is Look & feel versus Feel & sound. Test “Where would you change tap colors?” and “Where would you silence the keyboard?” before accepting the names. If users repeatedly confuse them, consider **Appearance** and **Sound & touch**, retaining expressive descriptions below.

## Research and acceptance plan

Use an unimplemented clickable documentation prototype or paper wireframes first; then a feature-flagged app shell in a separate approved implementation workstream. Do not count this exploration as usability validation.

Recruit at least a few current power users and a few people new to Keywi, including someone using large text and someone navigating with TalkBack. Counterbalance proposal order. Use the same tasks above, identical control capabilities and realistic large libraries. Record first destination, wrong turns/backtracks, whether search was needed, task completion, confidence, and whether users can explain mode/group inheritance and power suppression.

Proposed decision gates: no lost expert task; users can find all six core tasks (group gradient, tap fade, gestures, dictionary, toolbar triple-tap, power) without guidance; fewer wrong turns than current navigation; search locates the exact control; no accidental Tasker execution; predictable Back behavior; no clipping or inaccessible actions at 320dp and 200% text. Actual target thresholds and time limits should be agreed before the study. Do not manufacture percentage improvements from a navigation diagram.
