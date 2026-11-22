# Choices for Birdie 🩷

These are design decisions, not blockers for repository housekeeping. Defaults below are recommendations for review, not selected behavior. Existing approved requests and compatibility requirements do not need to be decided again.

## Settings map

The [#63 exploration / PR #65](https://github.com/BirdMachine/Keywi/pull/65) contains the source inventory, three complete trees and mobile wireframes. Its baseline predates merged #64, so any implementation must retain the fitted gesture labels and independent folds.

| Option | What the home page emphasizes | Tradeoff |
| --- | --- | --- |
| Creative Studio | Seven verb-led creative destinations | Inviting language, but appearance/response and styling/geometry journeys cross categories. |
| Instrument & Controls | Eight precise subsystem destinations | Predictable component ownership, but more of an instrument panel and more top-level entries. |
| Hybrid · recommended | Look & feel; Layout & keys; Typing; Feel & sound; Tools & connections; App & care | Six stable doors with contextual shared editors; needs deliberate links where appearance and feedback meet. |

**Decision:** choose the architecture and the first implementation slice. Recommendation: a reversible home/directory shell using current screens and route aliases; shared editor extraction and discovery follow separately. Search, favorites and recents remain proposals, not prerequisites or existing features.

## Where Tap effects live

#60 gathers flash/fade, trails and GIF/PNG artwork under Advanced themes. Hybrid also gives sound/haptics their own family.

**Decision:** accept one canonical Tap effects editor under Look & feel with a clear shortcut from Feel & sound, or make Feedback the owning family and cross-link from appearance. Recommendation: keep #60's visual-effects ownership and cross-links; avoid a second writable copy of the controls. Theme package scope remains a separate decision/implementation.

## How much Chromatic UI Lab to adopt

[PR #57's final journal](https://github.com/BirdMachine/Keywi/blob/feature/chromatic-ui-lab-2026-10-27/CHROMATIC_UI_LAB_PROGRESS.md) extends beyond the initial recipes/jump rail into slider textures, staged selection and non-destructive imports.

**Decision:** select experiments individually after seeing their current visuals. Recommendation: extract desired recipes/slider styling separately after #56/#60 settle their state and navigation contracts. Historical green builds do not replace visual review. No automatic whole-lab merge.

## Scope that is already settled

- Keep all unique advanced controls, raw YAML, stored IDs and route compatibility during any settings redesign.
- Header copy in #48 was requested already; it needs normal integration review, not another architecture decision.
- Tasker scenes are intended to be **interactive pages inside Keywi**. A separately opened scene does not satisfy that goal; feasibility still needs research.
- Upstream Compose and previous/next IME controls were declined. Colon-triggered emoji lookup must be non-destructive. Whole-word handwriting is preferred; a glyph prototype is not a completed replacement.
- The [release LED](ideas.md) follows published releases from main, with direct APK download on tap and release details on hold. A new branch commit alone does not light it.
- #65 is a proposal. Reviewing/merging its documentation never authorizes a UI rewrite or closes #63 automatically.
