# Troubleshooting

[Guide home](README.md)

| Symptom | Check |
| --- | --- |
| Another keyboard opens | Select Keywi in Android's input-method picker; enabling an IME and selecting it are separate steps |
| Theme editor controls are disabled | Enable custom keyboard backgrounds at the top of Advanced theme editor |
| Key borders disappeared | In the editor choose a visible border style/color; in Keyboard tuning check border width; reopen the keyboard |
| Wrong outline changed | Toolbar, lozenges and keys have separate controls; the suggestion toggle follows the key border |
| GIF stopped moving | Check Keywi's Restricted power mode and the selected media; a still preview alone does not prove GIF failure |
| Imported theme has missing media | Re-select missing files in Advanced theme editor; export the packaged theme again |
| Theme edits seemed to change the original | Editing starts in Custom; select the named original from the saved-theme strip to return to it |
| New theme did not become a named entry | Use Save as… in Theme manager; editor Save and Export have different jobs |
| Key edit did not stick | The matrix needs SAVE KEY; CLEAR only clears the editor until saved |
| Board does not appear | Enable it in Board management; use ME-Like's board navigation; HK prototype rows differ |
| Dictionary long-press is unavailable | Enable Show unfinished word in suggestions first |
| Send/Enter does the wrong thing | Check Context engine → adaptation and Smart Enter; the receiving app controls exposed editor actions |
| On-screen keyboard hides with a physical keyboard | Open Advanced input → Hardware keyboard compatibility |
| Restore did not bring back boards or media | Database backup is partial; read [backup scope](power-backup-diagnostics.md#what-a-backup-includes) |

For a reproducible bug, include your Keywi build (**About** or **Debug & diagnostics**), Android version, selected cut, affected app, exact steps and a diagnostic export. A screenshot or short recording is useful for visual issues.

The **About** page distinguishes Keywi's repository and releases from Thumb-Key's upstream links. Its Patreon and Liberapay links support Thumb-Key's developer.
