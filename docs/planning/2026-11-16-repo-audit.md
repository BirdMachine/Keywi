# Repository audit · 2026-11-16

Baseline: `main` = `Dev` = `02e47ab8fe300337c460b4f6300446d0477bb2ab`. Fresh full clone plus GitHub branch, PR and check-run reads; 61 remote branches, 9 open PRs and 4 open standalone issues. This dated evidence is a snapshot; [the workbench](README.md) owns current coordination.

## Branch result

**42 merged topic branches are eligible for retirement; 19 existing branches are retained. No remote branch was deleted in this pass.** Git command-line write access failed with “could not read Username”; the GitHub connector exposes no branch-delete operation, and the browser is signed out. The cleanup branch created for this audit is additional to this 61-branch baseline.

Every eligible tip is an ancestor of the immutable baseline SHA. Each retained integration branch, open-PR head, active gallery and unmerged tip is excluded. Full SHAs, ahead/behind counts and classifications are in [the branch manifest](2026-11-16-branches.tsv). No patch-equivalence or age heuristic is used to justify deletion.

Before a later deletion, fetch fresh refs/open PRs and reject any name whose remote SHA differs from the manifest, becomes protected, is an open-PR head, or is no longer merged into both current integration/release branches. Delete only exact eligible names with an expected-tip lease. Never use a broad wildcard delete. A recorded SHA identifies the commit to restore if needed; all eligible commits remain reachable through the baseline.

| Retained branch | Reason |
| --- | --- |
| `Dev` | Integration/release branch |
| `Keywi-Fishwi` | Unmerged history (15 commits ahead) |
| `Keywi-Penguin` | Unmerged history (8 commits ahead) |
| `birdie/gradient-backgrounds` | Unmerged history (1 commits ahead) |
| `birdie/stable-updates-advanced-selection` | Unmerged history (2 commits ahead) |
| `docs/settings-architecture-exploration-63` | Open PR #65 |
| `docs/slyk-theme-roundtrip-20261108` | Open PR #61 |
| `docs/svg-guide-laboratory` | Open PR #50 |
| `docs/theme-roundtrip-showcase` | Open PR #60 |
| `feature/advanced-sound-engine` | Unmerged history (2 commits ahead) |
| `feature/chromatic-ui-lab-2026-10-27` | Open PR #57 |
| `feature/offline-unicode-library` | Open PR #58 |
| `feature/settings-header-copy` | Open PR #48 |
| `feature/shadertoy-renderer-layers` | Unmerged history (57 commits ahead) |
| `feature/theme-gallery` | Active theme-gallery work, even though merged |
| `feature/theme-library-management` | Open PR #56 |
| `feature/thumbkey-upstream-harvest-2026-10` | Open PR #51 |
| `main` | Integration/release branch |
| `waitonme-chroma-overdrive` | Unmerged history (3 commits ahead) |

## PR targets and evidence

All nine existing work PRs (#48, #50, #51, #56, #57, #58, #60, #61, #65) were retargeted from `main` to `Dev` using the established integration policy. Neither branch tip moved. No pending work was merged, closed or deleted. Historical exact-head check evidence is summarized below; retargeting does not certify the combined result.

| PR | Head | Recorded checks |
| --- | --- | --- |
| [#48](https://github.com/BirdMachine/Keywi/pull/48) | `c71ceff1` | [Validate & build Keywi APK: success](https://github.com/BirdMachine/Keywi/actions/runs/40223828724/job/121261233229); [Validate & build Keywi APK: success](https://github.com/BirdMachine/Keywi/actions/runs/40223833070/job/121260927430) |
| [#50](https://github.com/BirdMachine/Keywi/pull/50) | `7b062551` | [Validate & build Keywi APK: success](https://github.com/BirdMachine/Keywi/actions/runs/40258213761/job/121364569149); [build: failure](https://github.com/BirdMachine/Keywi/actions/runs/40258213757/job/121364568839) |
| [#51](https://github.com/BirdMachine/Keywi/pull/51) | `4f377370` | [Validate & build Keywi APK: success](https://github.com/BirdMachine/Keywi/actions/runs/40256685386/job/121359934367); [Validate & build Keywi APK: success](https://github.com/BirdMachine/Keywi/actions/runs/40256680873/job/121359921334) |
| [#56](https://github.com/BirdMachine/Keywi/pull/56) | `733dc84f` | [Validate & build Keywi APK: success](https://github.com/BirdMachine/Keywi/actions/runs/40451732402/job/122015788817) |
| [#57](https://github.com/BirdMachine/Keywi/pull/57) | `385967c4` | [Validate & build Keywi APK: success](https://github.com/BirdMachine/Keywi/actions/runs/40508993338/job/122205602304) |
| [#58](https://github.com/BirdMachine/Keywi/pull/58) | `544c1312` | [Validate & build Keywi APK: failure](https://github.com/BirdMachine/Keywi/actions/runs/40592326238/job/122499519835) |
| [#60](https://github.com/BirdMachine/Keywi/pull/60) | `9f8f1f5c` | [Validate & build Keywi APK: success](https://github.com/BirdMachine/Keywi/actions/runs/42499517406/job/128789064958) |
| [#61](https://github.com/BirdMachine/Keywi/pull/61) | `6324d17e` | [Validate & build Keywi APK: success](https://github.com/BirdMachine/Keywi/actions/runs/43599113449/job/132459925259) |
| [#65](https://github.com/BirdMachine/Keywi/pull/65) | `d33380db` | [Validate & build Keywi APK: success](https://github.com/BirdMachine/Keywi/actions/runs/43599657792/job/132461484565) |

## Important reconciliations

- Oklab Broad/Tight already exist, with regression coverage. #39 remains for visual acceptance rather than another implementation.
- #62/#64 and channel naming #66 are already in the baseline; #65's earlier exploration must preserve them.
- Current ThemeEngine schema is 3; #56 proposes schema 4 flash/fade/trail support. #60 relocates UI; it does not add sound/artwork portability. #61's documentation must follow the integrated schema.
- #57's final work extends past the session-1 PR summary; staged selection/import logic overlaps #56 and theme navigation overlaps #60.
- #51 still includes declined Compose behavior. Keep it draft; any later integration must remove that scope. Native IME previous/next toggles remain declined.
- Unicode #58 failed `:app:lintKotlinMain` in [run 40592326238](https://github.com/BirdMachine/Keywi/actions/runs/40592326238), so compile/tests/device acceptance cannot be inferred.
- The [main showcase failure](https://github.com/BirdMachine/Keywi/actions/runs/43610279877) is specifically `HK settings shortcut missing in live IME` in `scripts/capture-keywi-showcase.py:95`, after successful APK installation. This is separate from successful main/Dev Android CI.
- #50's separate handbook build failed even though Android validation passed. Its visual/link acceptance remains pending.

## Scope and verification of this cleanup

The cleanup adds a documentation index, one work queue, one wishlist, a decision sheet, this audit/manifest and a PR template. Earlier prose/artwork is retained under historical banners, with IMMINENT's broken relative release URL corrected; contributor instructions match current channel names and CI JDK. App code, preferences, renderer, workflows and published releases are untouched.

PR metadata was also reconciled: #51 now explicitly excludes declined Compose scope from future integration; #57 summarizes its final experiments; #50/#58 record their failing checks; #60/#61 record later successful CI and overlapping work. #39 now records its implemented presets and remaining visual acceptance without closing the issue.

Verify local links/anchors in changed Markdown, exact preservation of wishlist bullets and original historical prose, ancestry/classification for all 61 manifest rows, and `git diff --check`. Publish the cleanup as a topic PR targeting Dev. No fresh Android/device verification is claimed for this documentation-only pass.
