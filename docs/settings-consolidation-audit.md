# Settings consolidation audit — October 12

## Implemented

| Old hub entry/control | Canonical destination | Result |
| --- | --- | --- |
| Modify keys | Key & Word Selection → Open raw YAML key editor | Remove duplicate hub entry; retain expert editor and saved YAML. |
| Look & Feel | Themes & appearance → Keyboard tuning | Remove duplicate hub entry; retain unique sizing, position, base palette/mode, key borders, visibility, timings and feedback. |
| Behavior | Advanced Input → Gestures & typing behavior | Move navigation into its input family; retain all existing behavior controls. |
| Other | Advanced Input → Hardware keyboard compatibility | Remove miscellaneous hub entry; give its sole setting an explicit home. |
| Legacy Backdrop switch and moved-controls notice | Appearance editor → Main backdrop | Remove ineffective legacy switch and obsolete notice component. The live host derives backdropEnabled from the current backdrop mode; its stored DB field is retained for compatibility. |
| Layouts | Character layouts | Clarify that these are character mappings, independent of Gem Cuts geometry. |

Existing routes and preference storage remain compatible. Advanced Key & Word Selection's
source and styling are unchanged. No theme, board, YAML, or database migration/reset is performed.

## Findings that block further deletion

- Matrix edits text for nine positions and offers main/shifted/numeric views. Raw YAML also
  exposes key actions, long press, display text, sizing/color, width multipliers, slide/swipe
  types and additional modes. It is an expert capability, not fully superseded functionality.
- Keyboard tuning still controls actual IME sizing, placement, border thickness/radius,
  haptics and legacy click sound. Advanced theme controls do not replace those settings.
- Display mode/base palette use the AppSettings theme fields; saved appearance themes use
  ThemeEngine/KeyTheme/Backdrop preferences. Similar labels do not imply duplicate data.
- Gesture settings and field-context adaptation act on different input mechanisms.
- Power presets are not equivalent to the stored key-animation timing controls.
- Advanced Sound is still a settings shell on this branch. The existing click-sound toggle
  cannot be retired on that basis; it is used by the live keyboard.
- Clipboard history/cleanup/private clipboard and Backup & Restore have unique functionality.
- Character layouts, Gem Cuts and custom boards describe different axes/surfaces.

## Later extraction candidates

Split Keyboard tuning's retained controls into named groups: geometry/placement in Gem Cuts;
base palette, key border dimensions and legend visibility in Appearance; haptics/click in a
functional Feedback/Sound section; animation timing in Motion; editor/notification behavior
in Advanced Input. Keep one UI per stored preference when doing that extraction. This pass
consolidates navigation without rewriting the broad LookAndFeelUpdate save model.

## Validation

CI: formatting, existing unit tests, Android lint and signed debug APK assembly.
Source checks: all relocated routes stay registered; Matrix/raw YAML link remain unchanged;
legacy BackdropSettingsSection has no references. Device navigation checks remain unverified.
