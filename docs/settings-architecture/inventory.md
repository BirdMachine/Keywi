# Current settings inventory

275 catalog entries. Parameterized controls (every gradient stop, key, gesture, board, clip, and collection entry) are listed once with their scope; this is not a claim of 275 independent stored preferences. Navigation entries and commands are included. Sources are pinned to `Dev` commit `8ef014017a020346da3d3e2f8922bb184a51688b` (2026-11-14).

See [navigation and findings](current-map.md), [alternatives](alternatives.md) and the [CSV with a destination in all three proposals](inventory.csv). “Current location” describes the visible path, including local tabs and internal state pages; it is not necessarily a Navigation route.

## Home

Storage / scope: Mixed; see linked destinations.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| H01 | Feedback mode | Cycle Sound / Vibrate only / Silent; same master gate also in effects and IME | [SettingsScreen.kt:129](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/SettingsScreen.kt#L129) |
| H02 | Character layouts | Multi-select character mappings; empty selection falls back to default; separate from geometry | [SettingsScreen.kt:187](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/SettingsScreen.kt#L187) |
| H03 | Setup | Conditional link when neither enabled nor selected; Android enable/select operations | [SettingsScreen.kt:124](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/SettingsScreen.kt#L124) |

## Home > Follow keyboard theme in settings

Storage / scope: SettingsAppearancePreferences.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| V01 | Follow keyboard theme in settings | Settings chrome follows keyboard artwork; does not apply a keyboard theme | [SettingsAppearancePreferences.kt:62](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/SettingsAppearancePreferences.kt#L62) |

## Home > Gem cuts

Storage / scope: GemCutPreferences; preview state is local.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| G01 | Gem cut | Select from KeywiFacets.all: ME-Like / HK-Like; HK experimental; observe saved layout without rewriting it | [GemCutSettingsScreen.kt:128](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/GemCutSettingsScreen.kt#L128) |
| G02 | Preview mode | ABC / Shift / Numeric preview only; cut-specific availability; not a stored layout change | [GemCutSettingsScreen.kt:175](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/GemCutSettingsScreen.kt#L175) |

## Home > Themes & appearance (Theme manager)

Storage / scope: ThemeEngine manifests and private assets; schema 3 at baseline.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| T01 | Saved theme | Select Custom or saved theme tile; applies that theme | [ThemeManagerSection.kt:192](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ThemeManagerSection.kt#L192) |
| T02 | Edit / Save as | Saved-theme Edit copies into Custom; Custom Save as asks a name; no delete/update UI at baseline | [ThemeManagerSection.kt:214](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ThemeManagerSection.kt#L214) |
| T03 | Theme name | Name field and Save / Cancel dialog | [ThemeManagerSection.kt:267](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ThemeManagerSection.kt#L267) |
| T04 | Import theme | Open ZIP or legacy JSON; import into Custom; errors reported | [ThemeManagerSection.kt:231](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ThemeManagerSection.kt#L231) |
| T05 | Export theme | CreateDocument ZIP including referenced theme assets; choose provider/destination | [ThemeManagerSection.kt:244](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ThemeManagerSection.kt#L244) |
| T06 | Reset Custom | Resets Custom; separate from whole AppSettings reset | [ThemeManagerSection.kt:252](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ThemeManagerSection.kt#L252) |

## Home > Themes & appearance > Advanced theme editor

Storage / scope: KeywiAppearancePreferences; ThemeEngine; surface preference stores.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| E01 | Custom appearance enabled | Label says custom keyboard backgrounds but gates editor for keys/fonts/toolbar too | [AdvancedLookAndFeelScreen.kt:111](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L111) |
| E02 | Save | Capture current appearance into Custom; not named-theme Save as | [AdvancedLookAndFeelScreen.kt:127](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L127) |
| E03 | Revert | Apply opening appearance snapshot; editor session scope at baseline | [AdvancedLookAndFeelScreen.kt:132](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L132) |
| E04 | Keyboard background section | Expandable; initially open; main backdrop editor | [AdvancedLookAndFeelScreen.kt:147](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L147) |
| E05 | Suggestion bar section | Expandable; initially closed; toolbar and chip surfaces | [AdvancedLookAndFeelScreen.kt:155](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L155) |
| E06 | Keys section | Expandable; initially open; global key surfaces and shortcut to Keys & groups | [AdvancedLookAndFeelScreen.kt:165](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L165) |
| E07 | Typography section | Expandable; initially closed; font plus suggestion motion | [AdvancedLookAndFeelScreen.kt:168](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L168) |

## Theme editor > Keyboard background > Main backdrop

Storage / scope: BackdropThemePreferences.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| BG01 | Backdrop mode | Colorful / Image / GIF / None; remaining controls conditional on mode | [AdvancedLookAndFeelScreen.kt:253](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L253) |
| BG02 | Backdrop opacity | 0–100 percent | [AdvancedLookAndFeelScreen.kt:257](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L257) |
| BG03 | Choose / change media | OpenDocument image input; copy into owned assets; Image or GIF mode | [AdvancedLookAndFeelScreen.kt:270](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L270) |
| BG04 | Backdrop gradient | Reusable managed gradient editor when Colorful | [AdvancedLookAndFeelScreen.kt:281](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L281) |

## Theme editor > Suggestion bar

Storage / scope: ToolbarThemePreferences; ToolbarBorder/Layout; SuggestionLozengeThemePreferences.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| SB01 | Toolbar background | Same mode / opacity / media / gradient editor as main backdrop; distinct store | [AdvancedLookAndFeelScreen.kt:157](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L157) |
| SB02 | Toolbar border width | 0–3 px | [AdvancedLookAndFeelScreen.kt:237](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L237) |
| SB03 | Toolbar border color | Reusable color editor | [AdvancedLookAndFeelScreen.kt:242](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L242) |
| SB04 | Toolbar-to-keys gap | 0–24 dp | [AdvancedLookAndFeelScreen.kt:246](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L246) |
| SB05 | Suggestion chip fill style | None / Solid / Gradient; original term lozenge | [AdvancedLookAndFeelScreen.kt:419](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L419) |
| SB06 | Suggestion chip fill | Color or managed gradient conditional on style | [AdvancedLookAndFeelScreen.kt:424](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L424) |
| SB07 | Suggestion chip border style | None / Solid / Gradient | [AdvancedLookAndFeelScreen.kt:441](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L441) |
| SB08 | Suggestion chip border width | 0–4 dp | [AdvancedLookAndFeelScreen.kt:446](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L446) |
| SB09 | Suggestion chip border | Color or managed gradient conditional on style | [AdvancedLookAndFeelScreen.kt:453](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L453) |
| SB10 | Current-word chip background | Color including opacity; separate from behavior toggle | [AdvancedLookAndFeelScreen.kt:474](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L474) |
| SB11 | Current-word chip highlight | Text/outline color; fallback reads older AdvancedKeyWord preference | [AdvancedLookAndFeelScreen.kt:476](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L476) |

## Theme editor > Keys

Storage / scope: KeyThemePreferences.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| KS01 | Design individual keys & groups | Cross-link opens advancedKeyWordSelection; same editor as home entry | [AdvancedLookAndFeelScreen.kt:495](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L495) |
| KS02 | Key surface style | None / Solid / Gradient | [AdvancedLookAndFeelScreen.kt:497](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L497) |
| KS03 | Key fill | Color or managed gradient | [AdvancedLookAndFeelScreen.kt:502](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L502) |
| KS04 | Key border style | None / Solid / Gradient / Shadow | [AdvancedLookAndFeelScreen.kt:517](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L517) |
| KS05 | Key border paint | Color or managed gradient; not border dimensions | [AdvancedLookAndFeelScreen.kt:522](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L522) |
| KS06 | Shadow color | Conditional on Shadow | [AdvancedLookAndFeelScreen.kt:530](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L530) |
| KS07 | Shadow opacity | 0–100 percent | [AdvancedLookAndFeelScreen.kt:533](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L533) |
| KS08 | Shadow elevation | 0–16 dp | [AdvancedLookAndFeelScreen.kt:540](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L540) |

## Theme editor > Keys > Border distribution

Storage / scope: KeyThemePreferences.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| BD01 | Border gradient distribution | Same / Stable random / Step per key / Across keyboard | [KeyDesignEditor.kt:45](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L45) |
| BD02 | Border phase step | 0–360 degrees; Step per key only | [KeyDesignEditor.kt:63](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L63) |
| BD03 | Shuffle starting points | Stable random only; increments seed | [KeyDesignEditor.kt:68](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L68) |

## Theme editor > Typography

Storage / scope: FontPreferences; SuggestionMotionPreferences.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| F01 | Import font | TTF/OTF/provider; copy and validate font | [AdvancedLookAndFeelScreen.kt:582](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L582) |
| F02 | System font | Clear imported font | [AdvancedLookAndFeelScreen.kt:587](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L587) |
| F03 | Suggestion motion style | SuggestionMotionStyle entries; unexpectedly under Typography | [AdvancedLookAndFeelScreen.kt:591](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L591) |

## Reusable gradient editor (background / toolbar / chips / keys / overrides)

Storage / scope: GradientLibrary plus invoking surface store.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| GR01 | Gradient preset | Choose saved preset or New gradient | [AdvancedLookAndFeelScreen.kt:328](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L328) |
| GR02 | Gradient details | Expandable; collapsed by default | [AdvancedLookAndFeelScreen.kt:309](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L309) |
| GR03 | Gradient angle | 0–360 degrees | [AdvancedLookAndFeelScreen.kt:286](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L286) |
| GR04 | Color stops | Nested expandable section; collapsed by default | [AdvancedLookAndFeelScreen.kt:633](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L633) |
| GR05 | Stop position | 0–100 percent; sorted when moved | [AdvancedLookAndFeelScreen.kt:658](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L658) |
| GR06 | Stop color | Reusable color editor | [AdvancedLookAndFeelScreen.kt:668](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L668) |
| GR07 | Delete stop | Enabled only when more than two stops | [AdvancedLookAndFeelScreen.kt:651](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L651) |
| GR08 | Add stop | Inserts at midpoint | [AdvancedLookAndFeelScreen.kt:690](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L690) |
| GR09 | Save gradient as | Name field and Save; separate gradient library | [AdvancedLookAndFeelScreen.kt:394](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/AdvancedLookAndFeelScreen.kt#L394) |

## Reusable Chroma Desk color dialog

Storage / scope: Invoker draft; recent colors in keywi_chroma_desk.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| C01 | Hue / saturation disc | Touch and accessibility custom actions; before/after samples | [ChromaDeskColorEditor.kt:214](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ChromaDeskColorEditor.kt#L214) |
| C02 | Brightness | 0–100 percent strip | [ChromaDeskColorEditor.kt:222](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ChromaDeskColorEditor.kt#L222) |
| C03 | Opacity | 0–100 percent checkerboard strip | [ChromaDeskColorEditor.kt:230](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ChromaDeskColorEditor.kt#L230) |
| C04 | HEX | RRGGBB or AARRGGBB; invalid input blocks Apply | [ChromaDeskColorEditor.kt:242](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ChromaDeskColorEditor.kt#L242) |
| C05 | RGB channels | Expandable numeric R/G/B 0–255; channel errors block Apply | [ChromaDeskColorEditor.kt:259](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ChromaDeskColorEditor.kt#L259) |
| C06 | Recent colors | Up to seven; selecting loads draft | [ChromaDeskColorEditor.kt:285](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ChromaDeskColorEditor.kt#L285) |
| C07 | Restore original | Restore dialog-opening color | [ChromaDeskColorEditor.kt:314](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ChromaDeskColorEditor.kt#L314) |
| C08 | Apply / Cancel color | Apply to invoking control or dismiss draft | [ChromaDeskColorEditor.kt:318](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/ChromaDeskColorEditor.kt#L318) |

## Theme manager > Keyboard tuning

Storage / scope: Room AppSettings / LookAndFeelUpdate broad save model.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| L01 | Position | KeyboardPosition enum | [LookAndFeelScreen.kt:261](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L261) |
| L02 | Position padding | −200 to 200 stored slider units | [LookAndFeelScreen.kt:282](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L282) |
| L03 | Ignore bottom padding | Toggle system bottom padding handling | [LookAndFeelScreen.kt:343](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L343) |
| L04 | Maximize to fit screen width | Auto-size toggle | [LookAndFeelScreen.kt:396](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L396) |
| L05 | Key size / width | 10–200; label changes for non-square mode | [LookAndFeelScreen.kt:422](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L422) |
| L06 | Use non-square keys | Enables independent height | [LookAndFeelScreen.kt:444](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L444) |
| L07 | Key height | 10–200; conditional non-square control | [LookAndFeelScreen.kt:470](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L470) |
| L08 | Key padding | 0–10 stored slider units | [LookAndFeelScreen.kt:495](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L495) |
| L09 | Bottom offset | 0–250 stored slider units | [LookAndFeelScreen.kt:558](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L558) |

## Theme manager > Keyboard tuning (appearance)

Storage / scope: Room AppSettings; not ThemeEngine fields.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| LA01 | Display mode | ThemeMode choices; distinct from named themes | [LookAndFeelScreen.kt:206](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L206) |
| LA02 | Base keyboard palette | ThemeColor enum; original base styling | [LookAndFeelScreen.kt:232](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L232) |
| LA03 | Hide letters | Legend visibility toggle | [LookAndFeelScreen.kt:307](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L307) |
| LA04 | Hide symbols | Legend visibility toggle | [LookAndFeelScreen.kt:325](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L325) |
| LA05 | Key border width | 0–50 stored tenths; paint selected elsewhere | [LookAndFeelScreen.kt:516](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L516) |
| LA06 | Key radius | 0–100 stored slider units | [LookAndFeelScreen.kt:537](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L537) |

## Theme manager > Keyboard tuning (motion)

Storage / scope: Room AppSettings.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| LM01 | Animation speed | 0–500 stored slider units | [LookAndFeelScreen.kt:579](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L579) |
| LM02 | Animation helper speed | 0–500; different helper timing | [LookAndFeelScreen.kt:602](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L602) |

## Theme manager > Keyboard tuning (feedback)

Storage / scope: Room AppSettings; master FeedbackPreferences still gates output.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| LH01 | Vibrate on tap | Requires system touch feedback | [LookAndFeelScreen.kt:625](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L625) |
| LH02 | Vibrate for slide gestures | Enabled only when sliding enabled; applies space/backspace | [LookAndFeelScreen.kt:646](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L646) |

## Theme manager > Keyboard tuning (editor behavior)

Storage / scope: Room AppSettings.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| LI01 | Show toast on layout switch | Notification behavior | [LookAndFeelScreen.kt:361](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L361) |
| LI02 | Disable fullscreen text editor | Android IME editor behavior | [LookAndFeelScreen.kt:378](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/LookAndFeelScreen.kt#L378) |

## Home > Key & Word Selection (title: Keys & groups)

Storage / scope: Local selection; AppSettings YAML; KeyThemePreferences.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| M01 | Keyboard mode | Main / Shifted / Numeric; local selection | [AdvancedKeyWordSelectionScreen.kt:191](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L191) |
| M02 | Editor task | Style keys & groups / Actions & values tabs | [AdvancedKeyWordSelectionScreen.kt:216](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L216) |
| M03 | Select multiple keys | Style task only; long press is alternate entry | [AdvancedKeyWordSelectionScreen.kt:229](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L229) |
| M04 | Clear / Select all | Selection controls; style group mode only | [AdvancedKeyWordSelectionScreen.kt:252](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L252) |
| M05 | Geometry preview | ME-Like matrix / HK-Like matrix; local editor target rather than saved cut selection | [AdvancedKeyWordSelectionScreen.kt:285](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L285) |
| M06 | Key selection | Tap key; long press enters multi-selection; dismisses soft keyboard | [AdvancedKeyWordSelectionScreen.kt:120](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L120) |
| M07 | Character values | ME-only nine directional text fields; blank inherits original | [AdvancedKeyWordSelectionScreen.kt:351](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L351) |
| M08 | Save key | Writes visual text overrides into existing YAML | [AdvancedKeyWordSelectionScreen.kt:383](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L383) |
| M09 | Clear key values | Clears draft; requires Save key to restore originals | [AdvancedKeyWordSelectionScreen.kt:392](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L392) |
| M10 | Gesture assignment target | Tap / eight directions / Hold; HK tap/hold only | [AdvancedKeyWordSelectionScreen.kt:711](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L711) |
| M11 | Sound assignment | Per key + gesture; Inherit / clear or clip; saved immediately | [AdvancedKeyWordSelectionScreen.kt:726](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L726) |
| M12 | GIF / PNG assignment | Per key + gesture; Inherit / clear or asset; immediate | [AdvancedKeyWordSelectionScreen.kt:735](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L735) |
| M13 | Sound / GIF pack links | Open shared pack settings from selected key | [AdvancedKeyWordSelectionScreen.kt:752](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L752) |
| M14 | Raw YAML link | Only under Actions task; retains expert capability | [AdvancedKeyWordSelectionScreen.kt:438](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L438) |

## Keys & groups > Style selected keys

Storage / scope: KeyThemePreferences; IDs row:column; group mode-specific; individual across modes.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| MG01 | Saved groups | Select existing group and membership | [KeyDesignEditor.kt:131](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L131) |
| MG02 | New group from selection | Begin new group draft | [KeyDesignEditor.kt:150](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L150) |
| MG03 | Group name | Collision validation; identifies saved group | [KeyDesignEditor.kt:153](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L153) |
| MG04 | Group membership | Tap cells; Apply saves membership | [KeyDesignEditor.kt:138](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L138) |
| MG05 | Share inherited gradients & borders | Mode-specific canvas; assigned fills fit selection either way | [KeyDesignEditor.kt:174](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L174) |
| MG06 | Glyph color inheritance | Inherit / Custom; color picker when custom | [KeyDesignEditor.kt:192](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L192) |
| MG07 | Border / highlight inheritance | Inherit / Solid / Gradient; shared editors | [KeyDesignEditor.kt:200](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L200) |
| MG08 | Key fill inheritance | Inherit / Solid / Gradient; shared editors | [KeyDesignEditor.kt:209](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L209) |
| MG09 | Apply key / group style | Individual overrides take priority; group changes active mode only | [KeyDesignEditor.kt:245](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L245) |
| MG10 | Reset key / this mode | Remove individual override or reset group mode to inheritance; does not delete group | [KeyDesignEditor.kt:266](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/lookandfeel/KeyDesignEditor.kt#L266) |

## Keys & groups > Actions & values > Current word + personal dictionary

Storage / scope: AdvancedKeyWordPreferences; Android user dictionary insertion from IME.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| DW01 | Show unfinished word in suggestions | Behavior toggle; distinct from current-word chip paint | [AdvancedKeyWordSelectionScreen.kt:411](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L411) |
| DW02 | Long-press unfinished word to add to dictionary | Enabled when unfinished word shown; provider action in IME; no dictionary list editor here | [AdvancedKeyWordSelectionScreen.kt:419](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/AdvancedKeyWordSelectionScreen.kt#L419) |

## Keys & groups > Actions & values > Raw key YAML

Storage / scope: Room AppSettings.keyModifications.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| Y01 | Raw YAML text | Debounced write (300 ms); validation message; not a separate data copy from matrix | [ModifyKeysScreen.kt:112](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/ModifyKeysScreen.kt#L112) |
| Y02 | How to modify keys | External upstream schema documentation | [ModifyKeysScreen.kt:49](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/ModifyKeysScreen.kt#L49) |

## Keys & groups > Actions & values > gesture > Tasker

Storage / scope: TaskerKeyBindings; selected cell+gesture across layers.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| TK01 | Task behavior | Replace key / Also run task; assignment changes never execute task | [TaskerKeyAssignments.kt:106](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/TaskerKeyAssignments.kt#L106) |
| TK02 | Choose Tasker task | External task picker; selected target retained | [TaskerKeyAssignments.kt:132](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/TaskerKeyAssignments.kt#L132) |
| TK03 | Enter existing task name | Manual exact name; Assign / Cancel; not verified by saving | [TaskerKeyAssignments.kt:136](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/TaskerKeyAssignments.kt#L136) |
| TK04 | Remove Tasker assignment | Clear binding; restore original action | [TaskerKeyAssignments.kt:144](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/TaskerKeyAssignments.kt#L144) |
| TK05 | Check connection | Refresh status | [TaskerKeyAssignments.kt:148](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/TaskerKeyAssignments.kt#L148) |
| TK06 | Grant access | Conditional permission request | [TaskerKeyAssignments.kt:153](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/TaskerKeyAssignments.kt#L153) |
| TK07 | Tasker preferences | External access settings | [TaskerKeyAssignments.kt:163](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/TaskerKeyAssignments.kt#L163) |

## Tasker assignment > Command symbol

Storage / scope: TaskerKeyBindings display; owned images.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| TI01 | Text / Unicode symbol | 1–16 characters; Apply label | [TaskerCommandAppearanceEditor.kt:109](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/TaskerCommandAppearanceEditor.kt#L109) |
| TI02 | Built-in command icon | Choose from TaskerCommandImages.icons | [TaskerCommandAppearanceEditor.kt:118](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/TaskerCommandAppearanceEditor.kt#L118) |
| TI03 | Import symbol image | Bitmap copy reduced to 256px; max 4MB/16MP | [TaskerCommandAppearanceEditor.kt:132](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/TaskerCommandAppearanceEditor.kt#L132) |
| TI04 | Use original label | Clear display override only | [TaskerCommandAppearanceEditor.kt:133](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/modifykeys/TaskerCommandAppearanceEditor.kt#L133) |

## Home > Board management (internal pages)

Storage / scope: CustomBoardPreferences; stable UUID board IDs.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| B01 | Board enabled | List switch; enabled boards cycle in order | [AdvancedBoardManagementScreen.kt:257](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L257) |
| B02 | Add board | Blank / Emoji / Unicode templates and name | [AdvancedBoardManagementScreen.kt:171](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L171) |
| B03 | Create board | Creates UUID; opens editor | [AdvancedBoardManagementScreen.kt:362](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L362) |
| B04 | Reorder boards | Separate internal page; drag grip or Move up / Move down; Done | [AdvancedBoardManagementScreen.kt:178](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L178) |
| B05 | Side A / Side B | Select alternate board assignments | [AdvancedBoardManagementScreen.kt:368](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L368) |
| B06 | Cell | 3×3 custom-board cell selection; navigation row fixed | [AdvancedBoardManagementScreen.kt:407](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L407) |
| B07 | Gesture | Tap / eight Swipes / Hold | [AdvancedBoardManagementScreen.kt:441](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L441) |
| B08 | Board action kind | Insert text / No action / Space / Backspace / Enter / Select side B / Copy / Paste / Cut / Undo / Redo / Select all / Return to ABC / Next board | [AdvancedBoardManagementScreen.kt:508](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L508) |
| B09 | Text to insert | Shown for text action; auto-save | [AdvancedBoardManagementScreen.kt:523](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L523) |
| B10 | Board name | Internal Board settings page; auto-save | [AdvancedBoardManagementScreen.kt:541](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L541) |
| B11 | Return to ABC after input | Board setting | [AdvancedBoardManagementScreen.kt:546](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L546) |
| B12 | Key vibration | Per-board setting; distinct from global haptics | [AdvancedBoardManagementScreen.kt:547](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L547) |
| B13 | Show swipe hints | Per-board setting | [AdvancedBoardManagementScreen.kt:548](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L548) |
| B14 | Duplicate board | New UUID and copied contents | [AdvancedBoardManagementScreen.kt:552](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L552) |
| B15 | Delete board | Confirmation; removes both sides | [AdvancedBoardManagementScreen.kt:553](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L553) |
| B16 | Undo deletion | Available from list in current session | [AdvancedBoardManagementScreen.kt:280](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L280) |
| B17 | Board help | Modal explains navigation and editing | [AdvancedBoardManagementScreen.kt:565](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/AdvancedBoardManagementScreen.kt#L565) |

## Home > Advanced input > Gestures & typing behavior

Storage / scope: Room AppSettings / BehaviorUpdate.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| BE01 | Auto capitalize | Toggle | [BehaviorScreen.kt:163](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L163) |
| BE02 | Spacebar multitaps | Toggle | [BehaviorScreen.kt:180](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L180) |
| BE03 | Return to letters after space | Toggle | [BehaviorScreen.kt:197](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L197) |
| BE04 | Minimum swipe length | 0–200 | [BehaviorScreen.kt:218](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L218) |
| BE05 | Slide enabled | Mutually exclusive with slide hold | [BehaviorScreen.kt:246](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L246) |
| BE06 | Cursor movement mode | CursorAccelerationMode enum; requires slide | [BehaviorScreen.kt:269](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L269) |
| BE07 | Slide sensitivity | 1–50; requires slide | [BehaviorScreen.kt:283](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L283) |
| BE08 | Spacebar slide deadzone | Requires slide | [BehaviorScreen.kt:318](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L318) |
| BE09 | Backspace slide deadzone | Requires slide | [BehaviorScreen.kt:336](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L336) |
| BE10 | Slide hold | Enabling disables slide and vice versa | [BehaviorScreen.kt:357](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L357) |
| BE11 | Drag return | Toggle | [BehaviorScreen.kt:378](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L378) |
| BE12 | Circular drag | Toggle | [BehaviorScreen.kt:395](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L395) |
| BE13 | Clockwise drag action | CircularDragAction enum; requires circular drag | [BehaviorScreen.kt:418](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L418) |
| BE14 | Counterclockwise drag action | CircularDragAction enum; requires circular drag | [BehaviorScreen.kt:444](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L444) |
| BE15 | Ghost keys | Toggle | [BehaviorScreen.kt:464](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/behavior/BehaviorScreen.kt#L464) |

## Home > Advanced input > Context engine

Storage / scope: ContextEnginePreferences.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| CX01 | Adapt to text field | Master capability-policy toggle | [AdvancedInputScreen.kt:166](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/AdvancedInputScreen.kt#L166) |
| CX02 | Smart Enter | Field-aware newline / Send / Search / Done / Next; depends on adaptation | [AdvancedInputScreen.kt:177](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/AdvancedInputScreen.kt#L177) |
| CX03 | Suppress sensitive-field suggestions | Depends on adaptation | [AdvancedInputScreen.kt:189](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/AdvancedInputScreen.kt#L189) |
| CX04 | Keep @ and dot inside structured tokens | Depends on adaptation; email/URL/handle token rules | [AdvancedInputScreen.kt:199](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/AdvancedInputScreen.kt#L199) |

## Advanced input > Hardware keyboard compatibility

Storage / scope: Room AppSettings.showOnScreenKeyboard.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| HW01 | Show on-screen keyboard with physical keyboard | Single option; route still named otherSettings | [OtherSettingsScreen.kt:91](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/other/OtherSettingsScreen.kt#L91) |

## Advanced input > Advanced Characters (title: Tool pages & palettes)

Storage / scope: ToolPagePreferences; ToolbarViewPreferences.home.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| TP01 | Page order | Internal Page order & visibility; move each page up/down | [ToolPageManagerScreen.kt:96](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L96) |
| TP02 | Page visibility | Characters always available; optional Formatting / Kaomoji / Quick Settings / String palettes | [ToolPageManagerScreen.kt:102](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L102) |
| TP03 | Home page | Set home; duplicates same preference in IME Toolbar controls | [ToolPageManagerScreen.kt:104](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L104) |
| TP04 | Restore page defaults | Section-scoped confirmation; home resets to Characters | [ToolPageManagerScreen.kt:108](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L108) |
| TP05 | Kaomoji collections | Enable / order / Add collection; internal collection page | [ToolPageManagerScreen.kt:110](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L110) |
| TP06 | String palette collections | Enable / order / Add collection; hidden tool page initially; plain text not commands | [ToolPageManagerScreen.kt:123](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L123) |
| TP07 | Collection name / symbol | Rename or create; Save collection / Cancel dialog | [ToolPageManagerScreen.kt:196](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L196) |
| TP08 | Delete collection | Confirmation; hide alternative keeps content | [ToolPageManagerScreen.kt:132](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L132) |
| TP09 | Entries | Enable / reorder; tap Edit entry; Add entry | [ToolPageManagerScreen.kt:145](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L145) |
| TP10 | Text to insert | Multiline entry editor | [ToolPageManagerScreen.kt:211](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L211) |
| TP11 | Entry label | Optional display label | [ToolPageManagerScreen.kt:212](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L212) |
| TP12 | Entry alternates | One per line; distinct nonblank entries | [ToolPageManagerScreen.kt:213](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L213) |
| TP13 | Delete entry | Confirmation | [ToolPageManagerScreen.kt:141](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L141) |
| TP14 | Restore collection defaults | Kaomoji resets built-ins; palettes clears; preserves favorites/recents elsewhere | [ToolPageManagerScreen.kt:125](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L125) |
| TP15 | Formatting category visibility/order | Built-in categories; no conversion-rule editing | [ToolPageManagerScreen.kt:154](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L154) |
| TP16 | Formatting style visibility/order | Built-in transformations and preview | [ToolPageManagerScreen.kt:160](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L160) |
| TP17 | Restore formatting defaults | Section-scoped confirmation | [ToolPageManagerScreen.kt:168](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L168) |
| TP18 | Quick Settings controls visibility/order | Suggestions / Feedback / Sound pack / GIFs / Power / Toolbar / Themes / Tasker / Audio / Effects | [ToolPageManagerScreen.kt:174](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L174) |
| TP19 | Restore Quick Settings defaults | Section-scoped confirmation | [ToolPageManagerScreen.kt:180](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L180) |
| TP20 | Manage character boards & key actions | Cross-link opens advancedBoards | [ToolPageManagerScreen.kt:90](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedinput/ToolPageManagerScreen.kt#L90) |

## IME > Quick Settings > Toolbar controls (not an activity route)

Storage / scope: ToolbarViewPreferences.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| TC01 | Invert tool scroll | Swap vertical swipe direction | [ToolbarCycler.kt:434](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/ToolbarCycler.kt#L434) |
| TC02 | Home view | Same stored value as tool-page Set home | [ToolbarCycler.kt:442](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/ToolbarCycler.kt#L442) |
| TC03 | Cycler icon | ToolbarIconStyle enum | [ToolbarCycler.kt:451](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/ToolbarCycler.kt#L451) |
| TC04 | Double / triple tap / hold actions | Each: Do nothing / Next / Previous / Home / Quick Settings / Open settings / Configure controls | [ToolbarCycler.kt:459](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/ToolbarCycler.kt#L459) |
| TC05 | Done | Dismiss inline controls; does not undo writes | [ToolbarCycler.kt:475](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/ToolbarCycler.kt#L475) |

## IME > Quick Settings

Storage / scope: Existing feature stores; shortcuts not independent copies.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| Q01 | Suggestions enabled | Only IME quick toggle / cycler single tap at baseline; suggestion_preferences | [QuickSettingsWorkspace.kt:93](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/QuickSettingsWorkspace.kt#L93) |
| Q02 | Use custom sound pack | Switch source to Clips or Default; can replace Generated source | [QuickSettingsWorkspace.kt:103](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/QuickSettingsWorkspace.kt#L103) |
| Q03 | Keypress images & GIFs enabled | Same overlay enabled flag as artwork editor | [QuickSettingsWorkspace.kt:110](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/QuickSettingsWorkspace.kt#L110) |
| Q04 | Feedback mode shortcut | Same master gate | [QuickSettingsWorkspace.kt:100](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/QuickSettingsWorkspace.kt#L100) |
| Q05 | Power mode shortcut | Same Unbridled / Conserved / Restricted / Desperate preference | [QuickSettingsWorkspace.kt:119](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/QuickSettingsWorkspace.kt#L119) |
| Q06 | Toolbar controls shortcut | Expands inline panel; recovery entry if hold remapped | [QuickSettingsWorkspace.kt:153](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/QuickSettingsWorkspace.kt#L153) |
| Q07 | Themes / Tasker / sounds / effects shortcuts | Deep links; Manage images & GIFs opens effects hub rather than artwork directly | [QuickSettingsWorkspace.kt:165](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/QuickSettingsWorkspace.kt#L165) |
| Q08 | Manage tool pages | Always available route advancedCharacters | [QuickSettingsWorkspace.kt:169](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/QuickSettingsWorkspace.kt#L169) |
| Q09 | All settings | Always available route settings | [QuickSettingsWorkspace.kt:170](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/keyboard/QuickSettingsWorkspace.kt#L170) |

## Home > Keypress effects > Key flash & drag trails

Storage / scope: AccentFeedbackPreferences; not included in schema 3 themes at baseline.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| FX01 | Fade-out duration | 0–5000ms; rounds to 25ms; zero off | [AccentFeedbackEditor.kt:41](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/AccentFeedbackEditor.kt#L41) |
| FX02 | Fade color frames | Ordered up to 12; color editor per frame | [AccentFeedbackEditor.kt:44](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/AccentFeedbackEditor.kt#L44) |
| FX03 | Move frame earlier | Order action; first disabled | [AccentFeedbackEditor.kt:48](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/AccentFeedbackEditor.kt#L48) |
| FX04 | Remove frame | Delete selected color | [AccentFeedbackEditor.kt:49](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/AccentFeedbackEditor.kt#L49) |
| FX05 | Add color frame | Limit 12 | [AccentFeedbackEditor.kt:53](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/AccentFeedbackEditor.kt#L53) |
| FX06 | Use theme accent | Clear color list; use theme accent fallback | [AccentFeedbackEditor.kt:54](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/AccentFeedbackEditor.kt#L54) |
| FX07 | Drag trails | Shares fade colors and duration | [AccentFeedbackEditor.kt:57](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/AccentFeedbackEditor.kt#L57) |
| FX08 | Preview fade | Local trigger and preview | [AccentFeedbackEditor.kt:60](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/AccentFeedbackEditor.kt#L60) |

## Keypress effects > Sounds & sound packs

Storage / scope: ActionSoundPreferences; owned clips.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| SO01 | Key sound source | Default click / Sound pack / Generated scale; clips retained | [AdvancedSoundScreen.kt:121](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L121) |
| SO02 | Generated scale root | Note-root selector; in expandable Generated scale | [AdvancedSoundScreen.kt:143](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L143) |
| SO03 | Generated scale kind | NoteScale entries | [AdvancedSoundScreen.kt:153](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L153) |
| SO04 | Tone level | 0–100 percent | [AdvancedSoundScreen.kt:162](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L162) |
| SO05 | Preview note | Explicit audio preview | [AdvancedSoundScreen.kt:163](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L163) |
| SO06 | Import sound files | Multiple audio; max 25MB each; copied into app | [AdvancedSoundScreen.kt:167](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L167) |
| SO07 | Android sounds | Notification sound picker import | [AdvancedSoundScreen.kt:182](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L182) |
| SO08 | Rename clip | Tap clip name; rename dialog | [AdvancedSoundScreen.kt:191](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L191) |
| SO09 | Preview clip | Explicit audio preview | [AdvancedSoundScreen.kt:194](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L194) |
| SO10 | Remove clip | Remove from sound pack | [AdvancedSoundScreen.kt:196](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L196) |
| SO11 | Restore subtle starter clips | Adds missing built-ins | [AdvancedSoundScreen.kt:211](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L211) |
| SO12 | Playback selection | Single / Ordered / Random / Direction; expandable Pack playback & mapping | [AdvancedSoundScreen.kt:215](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L215) |
| SO13 | One sound | Single mode clip selector | [AdvancedSoundScreen.kt:231](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L231) |
| SO14 | Direction sounds | Direction-mode per gesture clip / Off-unassigned | [AdvancedSoundScreen.kt:250](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L250) |
| SO15 | Trigger timing | Press / Release; resolved gesture timing | [AdvancedSoundScreen.kt:274](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L274) |
| SO16 | Let sound tails overlap | Boolean | [AdvancedSoundScreen.kt:286](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L286) |
| SO17 | Limit clip duration | Toggle; turns on at 50ms | [AdvancedSoundScreen.kt:287](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L287) |
| SO18 | Maximum clip duration | 10–2000ms if limited | [AdvancedSoundScreen.kt:290](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L290) |
| SO19 | Pack volume | 0–100 percent | [AdvancedSoundScreen.kt:295](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L295) |
| SO20 | Suggestion scrolling sound | Clip / Off; separate suggestion sounds expander | [AdvancedSoundScreen.kt:298](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L298) |
| SO21 | Suggestion selection sound | Clip / Off | [AdvancedSoundScreen.kt:299](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/advancedsound/AdvancedSoundScreen.kt#L299) |

## Keypress effects > GIF / PNG overlays (title: Keypress effects)

Storage / scope: ActionGifPreferences; owned assets.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| AO01 | Enable action overlays | Requires imported assets; power may suppress playback | [TypingOverlaySettingsScreen.kt:130](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L130) |
| AO02 | Add GIFs / PNGs | Multiple input; up to 32 per selection; 8MB / 2048×2048 each | [TypingOverlaySettingsScreen.kt:135](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L135) |
| AO03 | Play asset | Select and preview | [TypingOverlaySettingsScreen.kt:147](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L147) |
| AO04 | Remove asset | Remove artwork | [TypingOverlaySettingsScreen.kt:148](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L148) |
| AO05 | Playback | One image / Pack order / Random / By direction / By key+direction | [TypingOverlaySettingsScreen.kt:152](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L152) |
| AO06 | Default image | Default overlay or asset | [TypingOverlaySettingsScreen.kt:163](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L163) |
| AO07 | Direction defaults | Each gesture; Direction or Key mode | [TypingOverlaySettingsScreen.kt:169](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L169) |
| AO08 | Per-key target grid | 4×5 generic selector; does not derive actual topology | [TypingOverlaySettingsScreen.kt:196](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L196) |
| AO09 | Matrix key ID | Manual override including HK hk0_0 | [TypingOverlaySettingsScreen.kt:207](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L207) |
| AO10 | Key gesture | Every gesture or direction | [TypingOverlaySettingsScreen.kt:209](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L209) |
| AO11 | Assigned image | Default or asset; same assignment store as matrix | [TypingOverlaySettingsScreen.kt:217](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L217) |
| AO12 | Origin | Activated key / Keyboard center | [TypingOverlaySettingsScreen.kt:239](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L239) |
| AO13 | Layer | Above keys touch-through / Below keys | [TypingOverlaySettingsScreen.kt:251](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L251) |
| AO14 | Trigger timing | On action/tap / On release | [TypingOverlaySettingsScreen.kt:259](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L259) |
| AO15 | Display duration | 50–5000ms | [TypingOverlaySettingsScreen.kt:264](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L264) |
| AO16 | Minimum interval | 0–3000ms | [TypingOverlaySettingsScreen.kt:266](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L266) |
| AO17 | Overlapping effects | 1–8 | [TypingOverlaySettingsScreen.kt:268](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L268) |
| AO18 | Artwork opacity | 0–100 percent | [TypingOverlaySettingsScreen.kt:270](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L270) |
| AO19 | Image size | 10–100 percent of keyboard | [TypingOverlaySettingsScreen.kt:272](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L272) |
| AO20 | Preview assigned effect | Selected target + gesture; preview canvas | [TypingOverlaySettingsScreen.kt:275](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/boards/TypingOverlaySettingsScreen.kt#L275) |

## Theme manager > Tasker theme updates

Storage / scope: TaskerThemeAutomation token/enable/snapshot; no tokens in discovery index.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| TA01 | Allow incoming theme commands | Toggle; off clears temporary override | [TaskerThemeSettingsScreen.kt:55](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/TaskerThemeSettingsScreen.kt#L55) |
| TA02 | Copy pairing token | Explicit clipboard action | [TaskerThemeSettingsScreen.kt:69](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/TaskerThemeSettingsScreen.kt#L69) |
| TA03 | Replace token | Unpair old routines | [TaskerThemeSettingsScreen.kt:73](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/TaskerThemeSettingsScreen.kt#L73) |
| TA04 | Copy complete red-toolbar setup | Tasker instructions with token | [TaskerThemeSettingsScreen.kt:82](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/TaskerThemeSettingsScreen.kt#L82) |
| TA05 | Copy red-toolbar JSON | Command example | [TaskerThemeSettingsScreen.kt:85](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/TaskerThemeSettingsScreen.kt#L85) |
| TA06 | Copy clear-override JSON | Clear example | [TaskerThemeSettingsScreen.kt:88](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/TaskerThemeSettingsScreen.kt#L88) |
| TA07 | Copy complete clear setup | Tasker exit action setup | [TaskerThemeSettingsScreen.kt:91](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/TaskerThemeSettingsScreen.kt#L91) |
| TA08 | Restore theme now | Only when temporary override active | [TaskerThemeSettingsScreen.kt:110](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/TaskerThemeSettingsScreen.kt#L110) |
| TA09 | Refresh command status | Read last result | [TaskerThemeSettingsScreen.kt:117](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/TaskerThemeSettingsScreen.kt#L117) |

## Home > Power options

Storage / scope: KeywiPowerPreferences; overrides rendering without deleting appearance.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| P01 | Power mode | Unbridled / Conserved / Restricted / Desperate; directly selectable rows | [AdvancedPowerOptionsScreen.kt:60](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/power/AdvancedPowerOptionsScreen.kt#L60) |

## Home > Clipboard history

Storage / scope: Room AppSettings; ClipboardRepository/ClipboardDB content separate.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| CL01 | Enable clipboard history | Store copied text | [ClipboardSettingsScreen.kt:177](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/clipboard/ClipboardSettingsScreen.kt#L177) |
| CL02 | Auto-cleanup old clips | Requires history | [ClipboardSettingsScreen.kt:198](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/clipboard/ClipboardSettingsScreen.kt#L198) |
| CL03 | Cleanup duration | 5/10/30m; 1/2/4/12h; 1/2/4/7d; history+cleanup required | [ClipboardSettingsScreen.kt:219](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/clipboard/ClipboardSettingsScreen.kt#L219) |
| CL04 | Limit history size | Requires history | [ClipboardSettingsScreen.kt:247](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/clipboard/ClipboardSettingsScreen.kt#L247) |
| CL05 | Maximum history size | 2–100 clips; history+limit required | [ClipboardSettingsScreen.kt:268](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/clipboard/ClipboardSettingsScreen.kt#L268) |
| CL06 | Use private clipboard | Internal rather than system clipboard; requires history | [ClipboardSettingsScreen.kt:294](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/clipboard/ClipboardSettingsScreen.kt#L294) |

## Home > Backup and restore

Storage / scope: AppDB Room export/import only; not full collection of preference stores.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| BA01 | Backup database | CreateDocument ZIP; exports AppDB; not theme/media/SharedPreferences/ClipboardDB bundle | [BackupAndRestoreScreen.kt:218](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/backupandrestore/BackupAndRestoreScreen.kt#L218) |
| BA02 | Restore database | Open ZIP; destructive import indicated by warning; no new confirmation promised here | [BackupAndRestoreScreen.kt:236](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/backupandrestore/BackupAndRestoreScreen.kt#L236) |
| BA03 | Reset to defaults | Confirmation; resets AppSettings fields; not all app-private files or feature stores | [BackupAndRestoreScreen.kt:167](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/backupandrestore/BackupAndRestoreScreen.kt#L167) |

## Home > Debug & diagnostics

Storage / scope: Local diagnostics files and Android exit history.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| DG01 | Refresh diagnostics | Reload snapshot | [AdvancedDebugScreen.kt:120](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/debug/AdvancedDebugScreen.kt#L120) |
| DG02 | Clear diagnostics | Clears diagnostic data | [AdvancedDebugScreen.kt:138](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/debug/AdvancedDebugScreen.kt#L138) |
| DG03 | Export / share all diagnostics | Export report bundle/text action; explicit share | [AdvancedDebugScreen.kt:144](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/debug/AdvancedDebugScreen.kt#L144) |
| DG04 | Expand crash report | Per-report expandable scrollable selectable text | [AdvancedDebugScreen.kt:212](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/debug/AdvancedDebugScreen.kt#L212) |
| DG05 | Export crash text | Per-report .txt export | [AdvancedDebugScreen.kt:213](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/debug/AdvancedDebugScreen.kt#L213) |
| DG06 | Back to settings | Pop back stack | [AdvancedDebugScreen.kt:179](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/debug/AdvancedDebugScreen.kt#L179) |

## Home > Help / About

Storage / scope: Read-only external destinations.

| ID | Control / command | Choices, conditions, and save behavior | Source |
| --- | --- | --- | --- |
| HELP01 | Keywi releases | External Keywi release history | [AboutScreen.kt:53](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/about/AboutScreen.kt#L53) |
| HELP02 | Keywi issues | External issue tracker | [AboutScreen.kt:54](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/about/AboutScreen.kt#L54) |
| HELP03 | Keywi source | External repository | [AboutScreen.kt:55](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/about/AboutScreen.kt#L55) |
| HELP04 | Upstream source | Clearly marked Thumb-Key | [AboutScreen.kt:66](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/about/AboutScreen.kt#L66) |
| HELP05 | Upstream donations | Separate Patreon / Liberapay external links | [AboutScreen.kt:67](https://github.com/BirdMachine/Keywi/blob/8ef014017a020346da3d3e2f8922bb184a51688b/app/src/main/java/com/dessalines/thumbkey/ui/components/settings/about/AboutScreen.kt#L67) |

## Retained data without an equivalent visible control

- `AppSettings.soundOnTap` is preserved by Keyboard tuning’s broad update object; the baseline has no Sound on tap row there. Default click is now selected in Sounds. Do not infer that the old database field is the new source selector.
- `backdropEnabled` is retained in Room for compatibility, with the ineffective old UI removed by the October audit; live backdrop mode and custom appearance enablement are separate.
- `keyBorders` remains in AppSettings/reset compatibility data; this is not an additional visible row in the inspected tuning screen.
- Raw YAML still supports more than the nine text fields: key action, display text, size/color, removal, swipe-return text/action; key-item long press, width multiplier, background color, swipe type, slide type; layouts and additional modes. See `utils/KeyboardModificationService.kt` (`KeyItemCSerializable`, `KeyCSerializable`, `KeyActionSerializable`). Keep this expert path.
- Kaomoji favorites/recents and ordinary tool-content search exist as content interactions. They are not settings favorites, settings recents, or settings search. No global settings index/favorites/recent-controls UI was found in this baseline.
- Welcome/release and installation-policy banners, the upstream user-guide link on Home, test text fields, Android setup enable/select controls, and read-only diagnostics/version/capability summaries supplement the control catalog. They do not add stored customization preferences.
