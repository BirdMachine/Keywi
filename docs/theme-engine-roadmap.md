# Keywi appearance & geometry roadmap

This document is the durable design reference for the customization architecture. Keep structural decisions here rather than relying on chat history.

## Vocabulary

- **Theme**: complete visual appearance of Keywi. Themes are geometry-independent: the same theme/effects should render across every Facet.
- **ThemeDocument**: versioned serialized representation used for local persistence/import/export.
- **Custom**: permanent scratch theme. Editing a saved theme first forks it into Custom so the source is never accidentally mutated.
- **Facet**: the complete physical key geometry/topology of a keyboard surface. A Facet is bigger than a symbol layout and may have structural regions/features unique to that geometry.
- **Layout**: symbols/actions/layers assigned within a Facet. Layout content should not redefine the Facet's geometry.
- **Layer** *(later)*: alternate assignments on the same Facet/geometry. Do not block today's work on generalized layering.

### Important boundary: ME-Like

The current MessageEase-inspired geometry is the **ME-Like Facet**. Its persistent surrounding controls and its central 3×3 customizable surface are all part of the ME-Like Facet.

Within ME-Like, Alpha, Numeric, and Custom Boards should swap the **central 3×3 surface only**. They are not separate Facets. The surrounding ME-Like control geometry stays in place so Custom behaves like the existing ABC/# surfaces. Space/split-space belongs to the persistent ME-Like structure and can become configurable separately.

Do not generalize the 3×3 assumption into the Facet API: other Facets may have no analogous central surface.

### Planned Facets

- **ME-Like** — current MessageEase-inspired geometry, including its persistent control region and swappable 3×3 surface.
- **Machine Cut** — dense desktop/power-user geometry inspired by Hacker's Keyboard: number row, staggered/desktop-style alpha rows, modifiers/navigation/function controls, while retaining Keywi's theme, effects, settings, and eventual layer system.
- **T9** — phone keypad geometry.
- **Typewriter / staggered** — conventional QWERTY/Dvorak-like geometry; QWERTY and Dvorak are layouts on this Facet, not separate Facets.
- Further columnar/split geometries can be introduced without assuming ME-Like structure.

`Machine Cut` is the working product name; implementation identifiers should stay descriptive enough that a rename is cheap.

## Theme manager behavior

1. Every theme exposes the same appearance settings.
2. Selecting a saved theme applies its complete ThemeDocument.
3. Editing a saved theme immediately forks it into **Custom** and selects Custom.
4. Custom supports **Import**, **Save as theme**, **Export**, and **Reset**.
5. **Save as theme** asks for a name, creates/selects a new local theme, then resets the dormant Custom document to defaults.
6. Imported themes enter Custom first; importing should never silently overwrite a named theme.
7. Export format is schema-versioned JSON so future builds can migrate old themes safely.
8. Media references may be device-local; a later portable bundle format can embed assets for sharing.
9. Add a Theme Guide/Wiki link once the public documentation exists.

## UI direction

Move Advanced Look & Feel toward the compact Control Console / Layered Workspace concepts:

- compact theme selector/header and live keyboard preview;
- tabs/sections for Surface, Toolbar, Keys, Type, and FX;
- avoid full-width controls where a compact value/control row is sufficient;
- expand only complex editors such as gradients/media/key surfaces;
- preserve Keywi's chromatic/cyber/software-toy personality rather than generic Material settings cards.

## Nearby navigation idea

Add a quick shortcut to **Advanced Key & Word Selection** near the keyboard itself, tentatively down-right of the top-right key. It should be discoverable without stealing a normal key gesture; finalize hit target/gesture after the Theme Manager work is stable.

## Ordered implementation plan

### Phase 1 — finish the theme engine

1. Complete ThemeDocument coverage for Advanced Look & Feel state.
2. Finish Theme Manager UI and Custom lifecycle.
3. Finish Android import/export document picker/share flow.
4. Wire theme selection/editing cleanly into Advanced Look & Feel and keyboard rendering.
5. Compact the Advanced Look & Feel UI after persistence is stable.
6. Keep themes independent from keyboard geometry so one ThemeDocument applies to ME-Like, Machine Cut, T9, and later Facets.

### Phase 2 — ME-Like customizable surface

1. Refactor ME-Like rendering so its persistent surrounding controls are shared by Alpha, Numeric, and Custom Boards.
2. Make a Custom Board provide the same central 3×3 surface contract used by Alpha/Numeric rather than constructing a whole keyboard of its own.
3. Preserve the persistent right/bottom controls, sizing, one-handed placement, theme/effects, and keyboard settings when a Custom Board is active.
4. Make board traversal visibly discoverable using the existing ME-Like control area; gestures may remain accelerators but must not be the only path.
5. Preserve active board/surface state across rotation/configuration recreation.
6. Consider optional split-space once the shared ME-Like chassis/surface boundary is stable.

### Phase 3 — Facet architecture

1. Introduce an explicit Facet model for complete keyboard geometry, without baking in ME-Like's 3×3 assumptions.
2. Separate geometry (Facet) from symbol/action assignment (Layout) and appearance (Theme).
3. Route common effects/settings/theme rendering through Facet-independent primitives where possible.
4. Keep Facet-specific structural controls inside each Facet implementation.
5. Add a Facet selector/management path without disrupting existing users/migrations.

### Phase 4 — Machine Cut

1. Implement **Machine Cut** as the first new Facet to prove the abstraction.
2. Use a Hacker's Keyboard-inspired dense desktop geometry as the interaction reference, not as a pixel-for-pixel copy.
3. Include the expected desktop/power-user regions: number row, alpha rows, modifiers, navigation/function controls, space, Backspace, and Enter.
4. Apply the same Keywi ThemeDocument, key effects, backgrounds, typography, borders, and applicable behavior settings.
5. Use the same Layout/Layer concepts so alternate mappings do not require a second theming system.

## Guardrails

- Finish Phase 1 before changing Custom Board geometry.
- Finish the ME-Like shared-surface work before introducing generalized Facets.
- Do not call Alpha/Numeric/Custom separate Facets inside ME-Like.
- Do not force future Facets into a 3×3-plus-controls model.
- Theme is orthogonal to Facet and Layout: appearance should survive geometry changes.
- Prefer migrations/adapters over destructive preference changes; preserve existing user appearance and board data.