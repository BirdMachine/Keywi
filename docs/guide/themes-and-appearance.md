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

## Colors and gradients

Open **Edit color** to expose the color picker. The large field controls saturation horizontally and brightness vertically. The hue strip chooses the color family. **Opacity** controls alpha; **RGB channels** exposes individual channel sliders.

For gradients, choose a **GRADIENT PRESET** or **＋ New gradient**, then expand **Edit gradient**. Adjust the angle and expand the color-stop list to change each stop's position and color. Add stops with **＋ Add stop**; delete is available while more than two stops remain. **Save as** in this section saves a gradient preset, not a complete keyboard theme.

## Keyboard tuning

From Theme manager, open **Keyboard tuning** for display mode, base palette, position, padding, key sizing, border width, radius, label visibility, animation speeds, vibration and tap sound. Some size controls become available only after changing automatic sizing or non-square-key options.

Use the theme editor for the border's style and color; use tuning for its width and key corner radius. For example, choose a solid border in the editor and a visible width in tuning before judging its contrast in a text field.

## Media and motion

Restricted power mode turns GIFs into still images and disables Keywi animations. Check [Power options](power-backup-diagnostics.md) before troubleshooting a frozen animated theme.

The editor's one-shot key-press overlay note is marked as planned. A typing-overlay screen exists in the source but has no visible navigation entry in this checkpoint; this guide does not present it as an available editor control. Advanced Sound likewise has source code but no routed settings entry here.
