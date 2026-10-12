# Gem settings design language

Extends the October 12 Gem Cuts direction across Keywi settings: navy/violet surfaces,
magenta selection borders, gem details, compact outlined controls and section headers.
Light mode uses the same hierarchy with pale violet surfaces and darker accents.
Shared filled/outlined button wrappers use explicit gem-cut corners so Material pill
defaults do not reappear in the board or appearance editors.

## Scope and boundaries

- Settings hub: grouped Advanced / Settings / Help navigation, outlined rows and chevrons.
- Boards: outlined enabled entries, template/cell selection borders and gem-corner previews.
- Theme manager and appearance editors: compact framed sections and shared controls.
- Behavior, clipboard, legacy Look & Feel, Other: existing preference components decorated
  with a shared modifier; their dialogs, enable conditions and save callbacks remain intact.
- Input/context/characters, power, debug, backup, legacy Modify keys and About: shared
  palette/chrome, outlined cards or navigation preferences as appropriate.
- Advanced Key & Word Selection stays on its original composable route, original
  Material theme and original top bar. Its implementation is not edited. Destination-local
  theme scopes also keep it independent during navigation animations.
- Setup retains its original onboarding style.
- The settings theme is purely compositional. It never applies a ThemeDocument, rewrites
  keyboard settings, or overrides the IME theme. Gem Cut keyboard previews explicitly
  restore the original keyboard palette inside the new settings chrome.

## Validation

CI runs formatting, unit tests, Android lint and debug APK assembly. Isolation tests cover
Matrix/setup exclusions and settings destination coverage. No appearance-only pixel tests
are added. Physical-device visual and interaction checks still require a device.

Device checks: open all hub links; check dark/light mode and larger text; toggle existing
preferences; use layout dropdown/dialog; add/edit/reorder a board; select power modes;
edit/import/export a theme; expand/export a crash report; verify the Matrix screen and
actual IME theme look unchanged on both directions of navigation.

This follow-up builds on PR #25; review its incremental changes against
`feature/gem-cuts-visual-redesign`.
