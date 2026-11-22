# Night flight · 2026-11-21

## Landed housekeeping

[PR #68](https://github.com/BirdMachine/Keywi/pull/68) merged into **Dev** at `74d78c585ae7a1809adc25acfb32ad0de8c723dd`. Its reviewed head `fd068a38` passed [Android validation](https://github.com/BirdMachine/Keywi/actions/runs/43890385300); release publication was correctly skipped. Main remains at `02e47ab8fe300337c460b4f6300446d0477bb2ab` / build 752. Post-merge Dev validation is tracked [separately](https://github.com/BirdMachine/Keywi/actions/runs/44971533901).

## Branch retirement completed

Re-read all 62 remote branches and ten open PRs before retirement (including the housekeeping PR). After #68 merged, GitHub automatically removed its topic branch. For each of the 42 `eligible-merged` rows in the [November 16 manifest](2026-11-16-branches.tsv):

- The remote tip still exactly matched its recorded SHA.
- The tip was an ancestor of immutable `02e47ab8`, which is main and an ancestor of current Dev.
- No open PR used the branch as its head; none was main, Dev, the active gallery or an unmerged experiment.

The authenticated GitHub branch page exposed deletion and Restore controls, resolving the earlier authentication blocker. Retired all 42 exact branch names through those controls. A fresh API read returned **19 branches**: exactly the manifest's retained set, with main unchanged and Dev advanced only by #68. No commits were removed from main/Dev history. The original manifest remains the name-to-SHA restoration record. The November 16 audit is preserved as historical evidence rather than rewritten.

## Redundant work reconciled

[PR #48](https://github.com/BirdMachine/Keywi/pull/48) targeted an obsolete `SettingsThemeBanner()` implementation. Current `SettingsHomeHeader()` already has the requested eyebrow, heading and supporting copy in order: “A little keyboard atmosphere.” / “KEYWI ✦” / “YOUR THEME, EVERYWHERE”. Closed #48 with that explanation. Its branch remains because its tip is unmerged; no patch-equivalence deletion was used.

## Eventuals and presentation

Birdie's name for feature planning is **Eventuals**. Renamed the idea page without moving its stable `ideas.md` path. Added [Presentation & polish](ideas.md#presentation--polish), including README links, rendered layout, fresh screenshots/live captures, handbook alignment and release-time rechecks. Existing wishlist bullets are preserved.

The README now explains that Keywi is an Android keyboard and Thumb-Key fork, gives the release asset name and setup steps, separates gem cut/layout/theme choices, and links to the handbook, troubleshooting, workbench and Eventuals. Mascot and original voice remain. Handbook screenshots are described as dated checkpoints.

Validation: **45 local link/anchor/asset references** across five changed Markdown files resolve, and all **20 existing wishlist/boundary bullets** are preserved exactly. Latest release metadata resolves to `keywi-build-752` with asset `Keywi-latest.apk`; the upstream repository also resolves through GitHub. GitHub's rendered README was inspected at a 1363px desktop viewport: the mascot loaded, no page-width overflow was present, and setup/navigation rendered. Adjusted the mascot from 768px to its native 384px width and changed decorative/download headings to bold text so the outline proceeds from H1 to H2. The Eventuals link opens the renamed page with its presentation checklist. Mobile/narrow review, current live captures and handbook alignment remain explicitly open. Whitespace validation passes. This pass changes documentation only; no pending feature implementation or release promotion is included.

## Next pickup

[PR #56](https://github.com/BirdMachine/Keywi/pull/56) is next for persistence/legacy-compatibility review and combined-Dev tests. #60/#61 follow with their ownership and provider/device checks. Settings architecture, Tap effects ownership and Chromatic UI Lab selection remain [open choices](decisions.md).
