# Machine Cut skeleton status

The first Machine Cut renderer now exists as `MachineCutSkeleton` and is reachable through the generic `FacetPreview` rendering boundary.

This milestone is intentionally narrow:

- ME-Like remains the default/live IME facet.
- Machine Cut has its own dense five-row geometry.
- Machine Cut consumes the ambient `MaterialTheme`, so it inherits the same active Keywi theme path instead of owning appearance values.
- The skeleton does not yet emit `KeyAction`s; input wiring comes next.
- The generic facet model does not mention 3×3 surfaces.

Next implementation slice: add a facet preference/selector and project real Keywi layout/action cells into Machine Cut, then move the live IME renderer behind the same facet boundary once behavior parity is sufficient.
