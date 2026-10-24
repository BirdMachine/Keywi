# Incoming Tasker theme updates

Enable **Themes → Tasker theme updates** first, then copy your pairing token.
This feature receives Tasker broadcasts; it does not require a Tasker plugin.
Existing key-to-Tasker-task assignments are a separate feature.

In Tasker use **System → Send Intent**:

- Action: `com.birdmachine.keywi.action.APPLY_THEME_COMMAND`
- Package: the package displayed in Keywi's setup screen (debug and release differ).
- Target: Broadcast Receiver
- String extra: `token:<pairing token copied from Keywi>`
- String extra: `command:<one JSON command>`

Keywi's setup screen copies the complete configuration, individual token, red-toolbar JSON,
and clear JSON. The token is a per-install secret stored outside Android backups. Replace it
to unpair old routines. Commands are ignored while disabled; disabling restores an active
temporary override. Never publish the token in screenshots, exports or shared Tasker tasks.

## Red toolbar while battery is low

Make a Battery Level profile. Its entry task sends:

```json
{"version":1,"operation":"apply","mode":"temporary","updates":[{"target":"toolbar","style":"color","color":"#FFFF3030"}]}
```

Its Exit Task sends:

```json
{"version":1,"operation":"clear"}
```

Temporary commands keep one snapshot of the live theme before the first override.
Further temporary commands layer over the live override. Clear restores the original
values only for targeted surfaces that still match the last automated value. Manual changes
to those surfaces, and all unrelated controls, are preserved. If no manual differences remain,
it restores the previously selected saved theme too. Selecting another saved theme supersedes
the override. The saved Custom manifest is not overwritten by temporary commands.
Tracking survives process restarts. Only one override layer is supported.

Use `"mode":"custom"` for a lasting edit to the current unsaved Custom theme.
This removes untouched temporary values first, preserves intervening manual edits, applies
only the requested changes and stores them through the ordinary theme engine. It also clears the temporary
snapshot. Saved themes remain unchanged. Clearing later does not undo Custom edits.

## Typed updates

Every apply command has exactly `version`, `operation`, `mode`, and `updates`.
Choose one to five distinct targets. Unsupported fields, styles, malformed colors, duplicate
targets or invalid stops reject the whole command before any live preference changes.

| Target | Color | Gradient | Image |
| --- | --- | --- | --- |
| `background` | Yes | Yes | Yes |
| `toolbar` | Yes | Yes | Yes |
| `key_border` | Yes | Yes | No |
| `key_surface` | Yes | Yes | No |
| `toolbar_border` | Yes | No | No |

Color values are strings in `#RRGGBB` or Android `#AARRGGBB` form.
A background or toolbar update may include numeric `opacity` from 0 to 1.
Other existing controls, including individual-key/group overrides, are preserved.
Therefore a per-key override can continue to override a global border update.
No command accepts preference names, arbitrary file paths, executable actions or web URLs.

```json
{"version":1,"operation":"apply","mode":"custom","updates":[{"target":"key_border","style":"gradient","angle":45,"stops":[{"position":0,"color":"#FF00AA"},{"position":0.35,"color":"#22DDFF"},{"position":1,"color":"#FF00AA"}]}]}
```

Gradients need 2–64 strictly increasing numeric stops, beginning at 0 and ending at 1.
Angle is numeric degrees from -360 to 360. Command payloads are limited to 32 KiB.
Border commands change color/style, not border thickness or keyboard layout.

## Images

```json
{"version":1,"operation":"apply","mode":"custom","updates":[{"target":"background","style":"image","uri":"content://your.image.provider/example.png","opacity":0.8}]}
```

Tasker must already have access to the image. Grant Keywi temporary read access using
the same content URI in the Intent's Data or ClipData and the read-grant flag
(`Intent.FLAG_GRANT_READ_URI_PERMISSION`, numeric value 1).
Android providers differ; if Tasker's Send Intent cannot grant that provider's URI,
copy the image through a provider that supports URI grants. A string extra alone does not
grant Android file access.

Keywi validates image headers and copies the bytes into app-owned theme storage before
applying anything. PNG/JPEG/WebP/GIF inputs up to 8 MiB, dimensions up to 8192 per axis and
32 million pixels are accepted. Incoming artwork is rendered as an image (a GIF supplies a
static image here). Moving or deleting the source afterward does not break the copied theme.
Transient URI access need not survive the command. An unreadable or invalid image rejects
the update without changing live theme values.

Open the setup screen's **Last result** / **Refresh command status** to inspect acceptance.
The receiver does not log tokens, command contents or provider details.
