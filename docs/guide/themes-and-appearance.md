# Themes & appearance

[Guide home](README.md) · [Next: boards & keys](boards-and-keys.md)

Open **Settings → Themes & appearance**. This opens **Theme manager**, with the editor entry, saved-theme strip, test keyboard and **Keyboard tuning**.

## Edit, save and export a theme

1. Select a saved theme in the strip, or work in **Custom**.
2. Open **Advanced theme editor**. Editing a saved theme copies it into Custom; the named original stays intact.
3. Adjust the appearance. Controls apply as you edit, so inspect the test keyboard as you go.
4. **Save** captures the current appearance into Custom. **Revert** restores the appearance captured when that editor session opened; it is not a global history undo.
5. Return to Theme manager. With Custom active, choose **Save as…**, enter a **Theme name**, and save a named library entry.
6. Choose **Export** to write the active theme as a `.keywi-theme.zip` through Android's file picker. It includes the theme's app-owned image, GIF and font assets.

**Import** accepts a packaged theme or a supported JSON document and loads it into Custom. Inspect it, then use **Save as…** if you want a named entry. **Reset Custom** resets the scratch theme.

Saving a named theme and exporting a theme are separate actions in this version. Export does not automatically create a named library entry, request an author name, or open a share sheet. You can share the exported ZIP from your file manager.

### Choose where the exported file goes

**Export** opens Android's **Create document** picker. Use its location menu to choose a folder and confirm the filename before saving. A theme saved only inside Keywi's named-theme strip is still private app data; exporting creates the portable file. In your file manager, browse the destination you chose rather than relying on its recent-files or category view.

**Import** opens Android's **Open document** picker. Local folders and cloud locations, including Google Drive, depend on which document providers are installed, signed in and exposed by Android. Keywi reads/writes the selected document through Android; it does not have a separate Drive login or sync service. Drive and Samsung My Files behavior still need device testing. The current completion message says only that export succeeded; showing the destination and offering Share afterward are requested improvements.

### What a theme restores

The package includes background images/GIFs, key and toolbar styling, suggestion appearance and an imported font when one is selected. Key flash colors/fade timing, sound packs/assignments, typing overlays, keyboard geometry and character layouts are separate settings in the current schema. Importing a theme does not reproduce all of the original device's feedback or input configuration.

## Advanced theme editor

Enable **custom keyboard backgrounds** to use the appearance controls. Turning it off uses the original Thumb-Key styling; the disabled sections cannot be edited there.

| Section | Controls and effect |
| --- | --- |
| Keyboard background | Main backdrop mode, opacity, media selection and gradients |
| Suggestion bar | Toolbar backdrop, toolbar border and gap above keys; lozenge fills and borders; current-word background and highlight |
| Keys | Transparent, solid or gradient faces; none, solid, gradient or shadow border styles |
| Typography | Import a font or return to System default; choose suggestion motion style |

Expand a section to edit its details. **Choose media / Change media** copies selected background assets into Keywi-managed storage. If Theme manager reports missing original assets from an older theme, reselect them in the editor and export again.

The toolbar backdrop, suggestion lozenges, current-word chip and key borders are separate settings. The suggestion toggle uses the key border styling; changing a lozenge outline alone will not change that border.

For the main backdrop and toolbar backdrop, choose a mode:

| Mode | What appears |
| --- | --- |
| Colorful | A gradient; opens the gradient controls |
| Image | The selected still image |
| Gif | The selected animation, subject to power mode |
| None | No custom backdrop on that surface |

Backdrop opacity changes that surface independently. Transparent key faces and transparent lozenge faces have their own **None** settings; neither removes the main backdrop.

## Colors and gradients

Open **Edit color** to expose the color picker. The large field controls saturation horizontally and brightness vertically. The hue strip chooses the color family. **Opacity** controls alpha; **RGB channels** exposes individual channel sliders.

For backdrop gradients, first select **Colorful**; for key or lozenge gradients, select **Gradient**. Choose a **GRADIENT PRESET** or **＋ New gradient**, then expand **Edit gradient**. Adjust the angle and expand the color-stop list to change each stop's position and color. Add stops with **＋ Add stop**; delete is available while more than two stops remain. **Save as** in this section saves a gradient preset, not a complete keyboard theme.

## Keyboard tuning

<img src="images/keyboard-tuning.png" width="360" alt="Keyboard tuning showing display mode, palette, position, padding and label visibility" />

From Theme manager, open **Keyboard tuning** for display mode, base palette, position, padding, key sizing, border width, radius, label visibility, animation speeds, vibration and tap sound. Some size controls become available only after changing automatic sizing or non-square-key options.

Use the theme editor for the border's style and color; use tuning for its width and key corner radius. For example, choose a solid border in the editor and a visible width in tuning before judging its contrast in a text field.

## Media and motion

Restricted power mode turns GIFs into still images and disables Keywi animations. Check [Power options](power-backup-diagnostics.md) before troubleshooting a frozen animated theme.

The older screenshots in this guide predate the current Dev navigation. In the Dev source reviewed on 8 November 2026, **Settings → Keypress effects** opens **Key flash & drag trails**, plus **Sounds & sound packs** and **GIF / PNG overlays**. These are routed controls; updated emulator screenshots are still pending.

**Key flash & drag trails** offers a fade duration from 0 to 5000 ms (0 turns it off), up to 12 ordered color frames, an optional trail and **Preview fade**. “Frames” are color stops visited during one fade, not a recording frame rate. The press starts the color sequence; lifting the finger does not cancel its remaining fade. An empty color list uses the active theme's accent. These preferences are separate from the portable theme package.

For documentation, capture a silent GIF for the visible flash/fade and a video with an actual audio track for sound examples. Keep the GIF background's motion distinct from the key feedback. A silent screen recording does not verify sound playback.
