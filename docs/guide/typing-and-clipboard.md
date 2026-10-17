# Typing, tools & clipboard

[Guide home](README.md) · [Next: power, backup & diagnostics](power-backup-diagnostics.md)

## Gestures and typing behavior

Open **Settings → Advanced input → Gestures & typing behavior**. These controls include auto-capitalization, spacebar multi-taps, returning to letters after a space, minimum swipe length, slide gestures, cursor movement mode and sensitivity, dead zones, slide-hold, drag-and-return, circular drag actions and ghost keys.

| If you want to… | Start with… |
| --- | --- |
| Reduce accidental swipes | Increase minimum swipe length a little and test |
| Change how sliding moves the cursor | Slide gestures, cursor movement mode and slide sensitivity |
| Keep ordinary space/backspace swipes available | The corresponding normal-swipe/dead-zone options |
| Repeat movement or deletion while holding a slide | Slide-hold cursor movement |
| Access hidden symbols without switching to numbers | Ghost keys |

Some subordinate controls require their parent gesture switch. These behavior options come from the swipe keyboard system; do not assume every gesture maps to HK-Like's tap rows.

## Context engine

Open **Advanced input → Context engine**.

| Switch | Effect |
| --- | --- |
| Adapt to text field | Uses the editor's type, multiline state, action, selection and sensitivity |
| Smart Enter | Inserts newlines in multiline editors; prefers actions such as Send, Search, Done or Next in single-line fields when exposed by Android |
| Suppress suggestions in sensitive fields | Avoids suggestion-dictionary queries for password-style fields |
| Keep @ and . inside structured tokens | Treats addresses, URLs, handles and dotted identifiers as working tokens rather than splitting them at each punctuation mark |

The three subordinate switches are enabled when adaptation is on. Editor apps differ in the information and actions they expose.

## Character tools and physical keyboards

**Advanced input → Advanced characters** explains Kaomoji, Unicode/Fancy Text and string palettes. It is a help screen in this checkpoint, not a comprehensive character-settings editor.

Use the keyboard's **𝕱** tool to open Fancy Text; the **(｡•̀ᴗ-)✧** tab accesses Kaomoji and **✕** closes the panel. Some text transforms require an actual selection in an editable field. Unicode styling changes characters; how they render depends on the receiving app and font.

**Hardware keyboard compatibility** opens the option controlling whether Keywi stays on screen with a physical keyboard connected.

## Clipboard history

Open **Settings → Clipboard history**.

| Setting | Effect |
| --- | --- |
| Enable clipboard history | Stores copied text for later access |
| Auto-cleanup old clips | Removes old unpinned clips after the selected duration |
| Limit history size | Removes oldest unpinned clips when the chosen maximum is reached |
| Use private clipboard | Copies into the internal clipboard instead of the system clipboard |

Cleanup and size controls depend on history being enabled; their sliders also depend on their respective switches. History is persistent copied content: disable it if you do not want Keywi retaining clips.

In the clipboard panel, clips can be pasted, pinned/unpinned or deleted. Pinning exempts clips from age-based auto-cleanup and size-based trimming. Clip contents live in a separate clipboard database and are not included in **Backup database**.
