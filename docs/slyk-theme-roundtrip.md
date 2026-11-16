# slyk theme: portability and showcase check

Requested by Birdie on 8 November 2026. Source checkpoint: Dev `8ef014017a020346da3d3e2f8922bb184a51688b`. This report separates archive inspection, engine tests and Android UI tests; none substitutes for the others.

## Supplied package

| Check | Result |
| --- | --- |
| Original filename | `slyk.keywi-theme (1).zip` |
| Archive size | 4,252,455 bytes |
| SHA-256 | `24a3e37e139fa5bd2d036ee65b54fa356ae8a3f11e4b9356786a90461ff2dee0` |
| ZIP CRC | Passed |
| Manifest | Valid JSON, schema 3, name `slyk` |
| Entries | `theme.json` and one referenced `assets/*.bin` |
| Embedded media | GIF, 500 × 500, 36 frames, 1.44-second loop |
| Asset integrity | SHA-256 matches its content-addressed filename |
| Key appearance | Gradient faces/borders, MainTools shared-gradient group |
| Separate feedback | Flash/fade, sound packs and typing-overlay settings are absent from the theme schema |

The `.bin` suffix is the asset store's fallback extension; the embedded bytes are an animated GIF. This inspection verifies the supplied archive, not successful installation or rendering in Android.

## Export/import UX findings

The current Theme manager already uses Android's Create document picker for export and Open document picker for import. They let the user choose an available location. Google Drive and other providers can appear when installed, signed in and exposed by Android; their behavior on Birdie's phone has not been verified.

An exported ZIP and a named theme in Keywi's strip are different things. Export writes the selected document; Save as creates an app-private named theme. The current success message does not show the destination or offer an immediate Share action. A file missing from a recent-files view does not establish that it was never written; Samsung file categorization, selected location and provider completion all need checking.

Requested improvements:

- Make **Save to…** and the chosen filename/location explicit; show useful completion information.
- Offer **Share** after a successful export, with temporary readable URI permission for Android share targets such as Drive. Do not require sharing to save locally.
- Exercise local, Drive and other provider imports/exports, cancellation, unavailable providers and failed writes. Show success only after the output stream closes successfully.
- Define whether future portable packages should optionally include feedback/sounds; current schema 3 packages cannot reproduce those preferences.

## Exact-fixture engine check

`ThemeFixtureRoundTripTest` accepts a real package outside Git. It imports through ThemeEngine, saves a named copy, exports through ThemeEngine, removes app-owned theme storage and re-imports the resulting package. It compares theme values and every embedded asset byte and verifies flash-feedback preferences are left alone. It is skipped when no fixture is supplied; an ordinary CI green result is not evidence that Birdie's ZIP was tested.

```bash
KEYWI_THEME_FIXTURE=/absolute/path/slyk.keywi-theme.zip \
KEYWI_THEME_ROUNDTRIP_OUTPUT=/absolute/path/slyk-roundtrip.keywi-theme.zip \
./gradlew testDebugUnitTest --tests '*ThemeFixtureRoundTripTest'
```

A JVM/Robolectric check exercises the package engine. It does not verify Android's file picker, Samsung My Files, Drive upload, live IME rendering or a full APK uninstall/reinstall.

## Android acceptance pass

Use a disposable emulator, record the APK source SHA, API, size/density, gem cut, power mode and each result. Keep the user's original ZIP unchanged.

1. Install the Dev APK and select Keywi as the IME. Import the original ZIP using **Theme manager → Import** and Android's file picker. Verify the success message and GIF/gradient appearance.
2. Use **Save as…** to create a named theme; relaunch Keywi and verify it and its media still load.
3. Export through the picker into an explicit Downloads subfolder. Confirm the filename and file size in Files and copy the ZIP off-device.
4. Clear app data or uninstall/reinstall only this disposable emulator's app. Import the exported ZIP through the picker; verify the appearance without the original theme storage. Re-export and compare manifest semantics plus asset hashes, allowing an intentional theme-ID change.
5. Repeat with Drive and another installed provider when available. Test cancel, duplicate filenames and offline/read/write failure. Device-provider checks remain necessary even after an emulator pass.

## Motion and sound capture recipe

The supplied theme does not contain Birdie's custom flash/fade or sound preferences. Any configured demo must state its added settings rather than claim they were restored from `slyk`.

For the visual demo, keep normal power/motion behavior, set a clearly labeled multicolor fade, record at least 30 fps, tap then lift a key and allow the fade to finish. Include a slower fade example plus the normal chosen duration. Capture the real key surface; the animated backdrop alone does not demonstrate tap feedback. Record a static baseline and check that flashing appears after release. Use dummy text, no personal fields.

Use a palette-generated GIF for README playback and an MP4 master for smoother motion. Example conversion from a real recording:

```bash
ffmpeg -i flash-demo.mp4 -filter_complex \
  '[0:v]fps=30,scale=540:-1:flags=lanczos,split[a][b];[a]palettegen[p];[b][p]paletteuse' \
  -loop 0 flash-demo.gif
```

For sounds, record two clearly different built-in/custom assignments and demonstrate them in the real IME. GIF has no audio. Android `screenrecord` alone ordinarily produces a silent video; enable emulator audio and capture its actual host output/monitor (or use a device recorder supporting internal audio), then mux the synchronized track. Inspect the exported video's audio stream, listen to it, and check that taps match the sounds before describing the clip as verified. Do not substitute separately generated clicks.

Existing showcase workflow options include `-noaudio`, so that workflow cannot substantiate audible sound customization as written. Keep sound capture opt-in and reject silent footage as sound evidence.

## Result ledger

| Layer | Result |
| --- | --- |
| Archive inspection | Passed: manifest, ZIP CRC, media bytes and hash |
| Dev build | `assembleDebug` passed on Java 21 / Android SDK 37 |
| Existing ThemeAssetPackageTest | 9 tests, 0 failures, 0 skipped |
| Exact supplied fixture | ThemeFixtureRoundTripTest: 1 test, 0 failures, 0 skipped; output `slyk-roundtrip.keywi-theme.zip` (4,252,477 bytes), CRC and GIF hash rechecked |
| Flash/fade component checks | AccentFeedbackTest 3/3 and AccentFeedbackRenderTest 2/2 passed, including continued fade after finger release |
| Fresh-storage restoration | Passed in Robolectric: removed all app-owned theme files, then restored the generated ZIP; values and every asset byte match |
| Android file-picker / live IME | Blocked: API 35 software emulator reached ADB but failed to become stable; package installation ended with `Failure calling service package: Broken pipe (32)` and window service became unavailable |
| Drive / Samsung file-manager behavior | Not tested |
| Flash GIF / audible video | Not produced: no verified live app capture; existing capture workflow also disables audio |

The exact-fixture test is an engine check in Robolectric API 34. It is not an APK uninstall/reinstall or provider test. No GIF/video is presented as an app capture without viewing the resulting frames and, for sound, listening to the audio.


### Emulator attempt

Used the existing private `keywi-dev` environment, preserving its old worktrees and creating checkpoint `v6` before installing missing host dependencies. A separate Dev checkout built the APK. Created an API 35 Google APIs x86_64 Pixel 6 AVD; the host has no `/dev/kvm`, so it ran with `-accel off`, SwiftShader and audio disabled. ADB became available after roughly five minutes, but Android's initial package verification and service startup remained very slow. A screen capture was black and was not used as a documentation illustration. After about fourteen minutes, the package installer returned a broken pipe and the window service was unavailable again. Keywi installation, file-picker import/export and live keyboard rendering were not verified. This is an emulator/environment failure, not evidence of a Keywi theme defect.

During diagnosis the disposable guest's adbd was restarted as root and the first-boot dexopt preference was changed to `skip`; neither established a stable guest. The attempt's processes were stopped afterward. The next live acceptance pass needs a working accelerated Android environment. The existing GIF/audio capture recipe remains a checklist, not a delivered recording.
