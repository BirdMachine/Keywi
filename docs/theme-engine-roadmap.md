# Theme engine roadmap

## Theme vocabulary

- **Theme**: complete visual appearance of Keywi.
- **ThemeDocument**: versioned serialized representation used for local persistence/import/export.
- **Custom**: permanent scratch theme. Editing a saved theme first forks it into Custom so the source is never accidentally mutated.
- **Cut** *(later)*: physical key geometry/topology, independent from the symbols assigned to it.
- **Layout** *(later)*: symbols/actions assigned to a Cut.

This lets QWERTY and Dvorak share a Classic/staggered Cut, while T9 and the current MessageEase-like surface can be distinct Cuts. Working name for the current gem-like geometry: **Facet Cut**.

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

## Implementation sequence

1. ThemeDocument + ThemeEngine persistence/application layer.
2. Theme Manager UI and Custom lifecycle.
3. Import/export document picker and share flow.
4. Compact Advanced Look & Feel redesign on top of ThemeEngine.
5. Theme documentation/wiki and shareable asset bundles.
6. Cut/Layout model exploration (Facet, Classic, T9, Columnar/Split, etc.).
