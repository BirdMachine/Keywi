# Theme round-trip and media capture

[Guide home](README.md) · [Themes & appearance](themes-and-appearance.md)

This is a maintainer checklist. Record results against the APK actually installed; source review alone does not verify a picker, cloud provider, animation or sound.

## Reproduce an export that is hard to find

Record the Keywi build, Android version, file manager/provider, selected destination and exported filename. Distinguish a missing file in the phone's file manager from a missing saved-theme tile inside Keywi. Export and Save as… are separate operations.

1. Import the original `.keywi-theme.zip` with **Theme manager → Import**. Confirm success and a visible backdrop in the live test keyboard.
2. Use **Save as…** to create a named entry. Select it and export to **Downloads**, giving the file a unique name.
3. Confirm that exact file exists in Downloads. Inspect its ZIP: `theme.json` and every referenced asset must be present; compare asset SHA-256 hashes, not ZIP bytes (ZIP metadata can differ).
4. Export again into a user-created subfolder. Cancel one export and verify it does not report success. Record the destination shown by Android.
5. Move the exported file to a clean emulator or second install, then import it through the document picker. Confirm media survives without the original source file. Save as a new name, select another theme, then select the imported named theme and type.
6. In a disposable install, also test malformed ZIP and missing-asset failures; confirm the active theme remains usable.
7. With Drive installed and signed in on a test device, repeat export/import through Drive's document provider. A stock AOSP emulator cannot verify Drive integration. Provider availability, cancellation and offline errors need separate results.

Expected current scope: schema 3 carries backdrop, toolbar, key and suggestion styling plus referenced GIF/image/font bytes. Separate flash/fade, sound and action-effect preferences are outside the manifest. Export does not add a saved-theme tile or open a share sheet.

## GIF: actual press/release and fade

Use a short, real screen recording of the live keyboard. Open **Advanced themes → Tap effects** in the updated editor. Set an easily visible fade (for example 1000 ms) and record its configured colors separately; these are demo settings, not settings recovered from the theme package. Keep normal animations enabled and choose a power mode that permits motion. Record idle → press/hold → release → complete fade, then a second key. Include a still-frame alternative in user-facing docs.

Convert a reviewed recording with FFmpeg:

```sh
ffmpeg -i keywi-flash.mp4 -filter_complex "fps=20,scale=540:-1:flags=lanczos,split[a][b];[a]palettegen[p];[b][p]paletteuse" -loop 0 keywi-flash.gif
```

Inspect the recording and GIF before publishing. Software emulation can distort timing; do not present a slow emulator's fade as a phone performance measurement. GIF cannot carry audio.

## Video: sound customization

Android `screenrecord` creates a silent video. Use a recorder with actual playback-audio capture support or capture the emulator's audio output alongside video, then synchronize and mux those streams. Check that the resulting MP4 contains an audio track and listen to the key events. Do not add synthetic sound and call it captured app output.

Use test sounds with permission to redistribute them. Show the settings used and a short typing sequence. Record the APK/version and whether sound controls were reachable in that build; source-only screens are not proof of a working settings path.

## Capture record

For each session, retain APK hash/version, source ref where known, emulator API/profile, original package hash, asset hashes, import/export messages and per-step pass/fail/pending results. Keep raw logs and UI XML out of handbook illustrations. Review screenshots before copying them into `images/` and record provenance in [Screenshots](screenshots.md).
