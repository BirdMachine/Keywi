# Maintaining the settings guide

[Guide home](README.md)

The canonical guide lives in `docs/guide/` alongside the code. It renders directly on GitHub without a separate wiki repository or publishing service. The README links here. Keep navigation labels literal and explain the effect, persistence and dependencies of each setting.

## Refresh real screenshots

Use the **Settings guide screenshots** Actions workflow. It is manual after merge; its initial documentation branch also runs when the capture script/workflow changes. Ordinary APK builds do not launch it.

The workflow builds a disposable debug APK, installs it on a fresh API 34 Pixel 6 emulator, enables the IME, and opens existing `startRoute` destinations. It captures settings screens without pretending to test a live IME. No phone or personal data is used, and no release is published.

Download the `settings-guide-captures` artifact. For each candidate image:

1. Check `manifest.json` for the source commit, emulator configuration and expected-page verification.
2. Visually inspect the PNG and its corresponding UI XML. Reject setup screens, crashes, wrong routes and incomplete captures.
3. Copy only reviewed PNGs into `docs/guide/images/`, then add alt text and a useful caption.
4. Record the workflow run, source commit, date, device profile and build-label differences in `screenshots.md`.

Artifacts expire after 14 days. Images committed to the guide remain available. Do not commit logcat, diagnostic exports or UI dumps as user-guide illustrations.

For long pages, capture separate useful scroll positions and expanded controls. A screenshot of a collapsed editor does not document its hidden controls. Use the separate live-keyboard showcase workflow when verifying actual typing, layers or editor actions; settings-route captures do not establish those behaviors.

## Source checklist

Check `MainActivity.kt` for reachable destinations and `SettingsScreen.kt` for current navigation. Read screen implementations, English string resources and preference/engine code before describing behavior. Source files without a visible route are not automatically shipped user-facing controls.

Particularly verify: Custom versus named-theme saves, Save/Revert behavior, theme export versus database backup, border ownership, HK prototype limitations and parent-switch dependencies. Do not turn planned features into instructions.
