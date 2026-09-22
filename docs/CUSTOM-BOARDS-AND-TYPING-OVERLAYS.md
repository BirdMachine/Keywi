# Custom boards and typing overlays

## Rooms

Open **Settings → Advanced Board Management**. Add a blank room or start with Emoji or Unicode. Enable rooms with their switches; use ↑ to reorder them. Duplicate preserves both sides and options under a new identity. Delete asks for confirmation.

Each room has two editable 3×3 sides. Select A or B, select a key, then assign its tap, eight directional swipes, or hold. Text assignments preserve the full string, including emoji sequences and kaomoji. Action assignments include Shift (side B), space, backspace, Enter, editing commands, next room, and ABC.

- Swipe right on the ordinary #/ABC key: first enabled room → subsequent rooms → original keyboard.
- Tap #/ABC on the original keyboard: unchanged letters/numbers behavior.
- In a room, tap A/B to switch sides; swipe right for the next room.
- Swipe left or hold A/B: return to the original keyboard.
- Bottom row also provides space and backspace; swipe up on backspace for Enter.

Navigation stays available independently of editable keys. With no enabled rooms, existing key assignments remain unchanged. With rooms enabled, right-swipe on #/ABC is reserved for room paging (replacing any previous assignment in that direction).

Room options: return to ABC after a text key, allow vibration (still respects the global vibration setting), and show swipe hints. Existing layout remapping remains separate from custom-room assignments. This first editor uses a fixed 3×3 grid and protected control row; arbitrary row/column geometry is not included.

## Typing overlay

Open **Settings → On-type GIF / PNG overlay**, choose a file, and configure display duration, opacity, and size. Preview fires the same renderer used over the keyboard.

The overlay is centered over the keyboard and does not take touches. PNGs display then fade. GIFs play once and hold their last frame until the display duration ends. Each typed text key restarts the one overlay; rapid typing does not allocate overlapping instances. Native emoji-picker selections trigger it too. Toolbar palette selections, clipboard paste, and completion replacements are not wired as typing events in this first version.

Images must be GIF/PNG, at most 8 MB and 2048×2048. Decoding happens on an IO coroutine when the source changes, not on each keystroke. Conserved mode schedules up to about 30 frames/second; Unbridled up to about 60. Restricted mode disables the overlay. Hidden/detached views stop animating. Moved, deleted, or revoked source files need to be selected again. These custom preferences are not included in the existing database-only export flow.

## Appearance fix

Keys now render the saved key-surface gradient instead of the fixed grey preset. Solid and None surface modes, saved borders, and shadow options are also connected to rendering. An alpha-zero surface exposes the keyboard backdrop; the existing pressed-key highlight remains visible while pressing/releasing a key. It does not make the Android input-method window itself transparent to the app underneath.

## Verification

Automated board tests cover Unicode/emoji persistence across A/B, navigation on empty boards, preservation of tap and other swipes when paging is injected, and independent swipe/hold bindings. CI runs these before Android lint and the APK build.

Device checks before merging:

1. With no rooms enabled, verify existing #/ABC tap/swipes, language switching, and cursor navigation.
2. Add two rooms; cycle through both and back, disable/reorder/delete, and confirm the cycle matches the manager.
3. Assign a joined emoji, a combining-character string, and kaomoji; verify tap/swipe/hold output in a text editor and terminal.
4. Change A/B, return-after-input, hints, and haptics. Ensure navigation stays usable with all editable bindings empty.
5. Try a transparent PNG and a GIF. Type quickly, switch apps, hide/show the keyboard, and check that only one effect is active and touches pass through.
6. Select Restricted mode and verify overlay suppression. Compare typing latency with overlays disabled, then with a small GIF, including device power-saving mode.
7. Set every surface gradient stop to zero alpha; verify the backdrop shows through idle keys. Check Solid and None, then each border style.

### Current validation status (2026-09-21)

- Kotlin compiler PSI parser: all 13 changed Kotlin files parsed with zero syntax errors. This does not check types, Android APIs, or Compose compilation.
- `git diff --check`: passed.
- Full Gradle verification: blocked before task execution because Android Gradle plugin `com.android.library:9.4.1` could not be resolved in this environment. Unit tests, Android lint, and APK assembly have not run.
- Feature-branch push: rejected by automatic approval review pending explicit authorization to publish source and workflow changes to `BirdMachine/thumb-key`. No CI run or APK has been produced for this change.
