# Gem Cut architecture

Keywi now treats keyboard customization as three orthogonal axes:

- **Theme** — visual appearance only: backdrop, toolbar, key surfaces, gradients, borders, typography, suggestion styling, and effects. A Theme must not own key geometry or symbol assignments.
- **Facet** — the complete physical topology/geometry of a keyboard. A Facet defines regions, rows/cells, sizing relationships, and structural controls. It must not own a color palette.
- **Layout** — symbols/actions/layers projected onto a compatible Facet. A Layout must not silently redefine the Facet's geometry.

This gives the useful identity:

`rendered keyboard = Theme × Facet × Layout (+ active Layer)`

## Existing facet: ME-Like

The current keyboard is preserved as **ME-Like**. Its MessageEase-inspired central 3×3 surface *and* persistent surrounding controls are one facet. Alpha, Numeric, and Custom Board content are surfaces/layout state within ME-Like; they are not facets themselves.

Nothing in the generic Facet API may assume a 3×3 center because later facets do not necessarily have one.

## First proof facet: Machine Cut

**Machine Cut** is a dense desktop/power-user facet inspired by Hacker's Keyboard. Its initial skeleton establishes five desktop-like rows with number, alpha, modifier, navigation and space regions. It deliberately reads colors from `MaterialTheme`, which is already supplied by Keywi's active theme path, rather than introducing Machine-Cut-specific colors.

The first skeleton is intentionally non-interactive. The next step is to project Keywi actions/layout/layers into those cells while retaining the same ThemeDocument, backdrop/effects pipeline, typography, borders and applicable behavior settings used by ME-Like.

## Migration rule

Existing installations remain on `FacetId.ME_LIKE`. Introducing facet selection must therefore be additive and default-safe; no existing keyboard layout or custom board data should be rewritten merely to adopt the Gem Cut model.
