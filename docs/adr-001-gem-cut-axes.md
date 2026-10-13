# ADR-001: Theme, Facet and Layout are orthogonal

**Status:** accepted

Keywi must support radically different keyboard shapes without cloning its theming system or conflating QWERTY-like mappings with geometry.

Decision:

- Theme owns appearance.
- Facet owns complete geometry/topology.
- Layout owns symbols/actions assigned to a facet.
- Layer is alternate assignment state on the same facet and is deliberately compatible with this split.

The current MessageEase-inspired keyboard is named **ME-Like** and remains the default facet. Its central 3×3 surface is an internal ME-Like feature, not a universal keyboard abstraction.

**Machine Cut** is the first second facet and exists specifically to pressure-test this boundary with a geometry that has no 3×3 center.

Consequences:

- Themes can be switched without moving keys.
- Facets can be switched without inventing a new appearance model.
- QWERTY/Dvorak-like differences belong to Layout when they share a physical facet.
- Facet-specific structural controls remain inside the facet implementation.
