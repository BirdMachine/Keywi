# Eventuals 🥝✨


Birdie's feature plans, consolidated wishlist and 🌱 **Someday / Idea Incubator**. These are goals and experiments; listing an idea does not mean it has shipped or entered the active implementation queue.

## Presentation & polish

Requested for the night flight on **2026-11-21**: make Keywi presentable and check the README links. Keep this as an ongoing release-readiness goal.

- [x] Give the README a clear Android introduction, upstream credit, download path and setup steps while keeping Keywi's mascot and voice.
- [x] Check README file links, heading anchors and artwork against the source tree; verify the release destination and APK asset through GitHub.
- [x] Review the rendered README on GitHub at desktop width; check heading hierarchy, image sizing and useful link labels. Keep the mascot at its native 384px width rather than upscaling it.
- [ ] Review the README at narrow/mobile widths and check wrapping, image sizing and navigation.
- [ ] Refresh real keyboard screenshots/GIFs after integrations settle, with build/source provenance and useful alt text. Fix the HK live-showcase assertion before claiming current live captures.
- [ ] Reconcile handbook instructions with the shipped settings and theme schema, then build the site and check its links/assets. Track #50/#60/#61 overlaps in the workbench.
- [ ] Recheck README downloads, relative links, screenshots and release claims before each main promotion; keep proposals clearly separate from shipped behavior.

## Themes, sharing and everyday use

- **New-release LED:** add a little visual/faux LED near Keywi’s title/top. When a newer GitHub release published from `main` is available than the installed version, turn the light on green and show **New release**; otherwise keep it unlit. A new branch commit alone should not count as a release. **Tap** downloads that release’s installable APK directly. **Tap and hold** opens the corresponding GitHub release page for details. Make the hold action discoverable with a tooltip/popup label reading **Hold to open release deets**, or temporarily replace the **New release** text with that hint.
- **Save to…:** make theme exports easy to find, with a chosen folder and filename and useful completion feedback.
- **Share after export:** offer Google Drive and other Android share targets; keep saving and sharing separately usable.
- **Portable themes and feedback:** improve portability of theme assets, sound packs and flash/fade preferences. Sound packs and flash/fade preferences are currently separate from schema 3 theme packages.
- **Provider round trips:** exercise theme import/export through local storage, Drive and other Android document providers, then verify export → fresh install/import → re-export.
- **Real showcases:** capture keyboard GIFs showing on-tap and post-tap flash/color fade, plus videos with audible sound-customization examples.
- **A happier emoji life:** add emoji search and a better emoji pager, viewer and selector. 🩷
- **Typing follow-ups:** check ASCII quotes, suggestions following Shift, and strikethrough in the formatter; confirm what is already available before implementing more.

## Someday / Idea Incubator

- **Exotic sliders:** explore patterned and animated Android sliders with ticks, dots, crosshatching, diagonal stripes and other playful textures. Build on the existing slider-texture experiments rather than treating all patterned sliders as new work.
- **Chronomusical sound synth:** let keyboard sounds evolve with time of day, day of the week or other configurable musical mappings.
- **Keywi Everywhere — external API:** expose selected settings, actions and non-sensitive state/events to Tasker and other integrations. Never expose typed text or content.
- **Tasker scenes as custom tool pages:** let the user choose a Tasker scene and use it as an interactive custom page inside Keywi's tool-page area, alongside the built-in tools. Explore Tasker/Android support for hosting the scene there, page sizing, touch/focus behavior, lifecycle and returning to typing; feasibility is still unverified. Opening a scene separately would not fulfill the in-page goal.
- **Nova-style page transitions:** explore [selectable tool-page effects and accessible navigation](https://github.com/BirdMachine/Keywi/issues/59), with previews, ordinary tap access, optional holds, customization and reduced motion.
- **Navigation design experiments:** revisit Pocket Index, Glass Deck, Thumb Fan, Tool Orrery and the petal/flower directions as alternatives to explore, not a commitment to implement every design.

## Related work and boundaries

See [the workbench](README.md) for current status and integration order. This file preserves the wishlist consolidated in PR #61; it does not mark pending work as shipped.

- [#39 · Oklab Broad/Tight](https://github.com/BirdMachine/Keywi/issues/39): implemented in the current source, with visual acceptance still to confirm. Do not rebuild them as a new feature.
- [#47 · Emoji repertoire/font/cache audit](https://github.com/BirdMachine/Keywi/issues/47): verify the exact published AndroidX artifact and official repertoire before making coverage claims. A Unicode scalar browser is a separate tool from an emoji sequence picker.
- [#62 / #64 · Gesture labels and collapsible editors](https://github.com/BirdMachine/Keywi/issues/62): merged. Preserve this behavior during later redesigns.
- [#63 / #65 · Settings architecture](https://github.com/BirdMachine/Keywi/issues/63): exploration delivered; architecture and implementation scope await a choice.
- [#59 · Nova-style transitions](https://github.com/BirdMachine/Keywi/issues/59): still an idea; preserve ordinary tap access and reduced/off motion.
- [#61 · Real-package round-trip evidence](https://github.com/BirdMachine/Keywi/pull/61): provider and live-capture checks remain open. Its schema-3 findings must be reconciled if #56 lands.

Themes apply across geometry; ME-Like, Machine Cut and other geometry work keep their architectural references under [documentation](../README.md). Shader rendering remains a separate experiment. None of these ideas promises a release date.
