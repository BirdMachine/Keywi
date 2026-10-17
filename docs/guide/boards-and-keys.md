# Boards & key assignments

[Guide home](README.md) · [Next: typing & clipboard](typing-and-clipboard.md)

## Create a custom board

Open **Settings → Board management → + Add board**. Choose a blank board, Emoji or Unicode template, enter a name, then tap **Create board**.

Each custom board has **Side A** and **Side B**, each with nine editable keys. Tap a cell, then choose **Tap**, **Swipes** or **Hold**. For a swipe, choose its direction. Select the action in the action dropdown; for text insertion, enter **Text to insert**. These changes save automatically.

The navigation row stays available. In ME-Like, swipe right on **#/ABC** to visit enabled boards in order. Tap **A/B** to switch sides; swipe left on it or hold it to return to ABC. Swipe up on **⌫** for Enter. These are the board system's documented gestures; HK-Like's prototype rows do not provide the same board navigation in this checkpoint.

## Manage boards

| Control | Effect |
| --- | --- |
| Board-list switch | Includes or excludes the board from navigation |
| Reorder | Hold a grip and drag, or use Move up / Move down, then Done |
| Board Settings | Rename; enable; return to ABC after input; key vibration; show swipe hints |
| Duplicate board | Creates a separate copy of both sides |
| Delete board | Removes both sides and assignments after confirmation; Undo appears in the board list |

Toolbar panels such as Kaomoji are separate from custom boards.

## Remap a character-layout key

Open **Settings → Key & Word Selection**. The matrix remapper operates on the selected saved character layout.

1. Choose the layer and a key in the matrix.
2. Edit the text overrides for its slots.
3. Tap **SAVE KEY** to commit the overrides.

**CLEAR** empties the editor fields; tap **SAVE KEY** afterward to restore that key's original text assignments. It does not immediately clear saved assignments.

**OPEN RAW YAML KEY EDITOR** exposes the underlying modifications. Use it for changes the visual text editor cannot express; keep a database backup before larger edits. The visual matrix follows the saved layout, not HK-Like's independent prototype character rows.

## Current word and personal dictionary

Below the matrix, **Show unfinished word in suggestions** adds your in-progress word to the suggestion strip. Enabling **Long-press unfinished word → add to personal dictionary** lets a long-press use Android's user dictionary provider.

The long-press option requires the unfinished-word chip to be enabled. To change its colors, use **Themes & appearance → Advanced theme editor → Suggestion bar → Current / unfinished word chip**. Background alpha changes transparency; the highlight colors its text and outline.
