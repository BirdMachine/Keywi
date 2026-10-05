# Gem Cut acceptance criteria

Milestone 1 is accepted when:

1. `ThemeId`, `FacetId`, and `LayoutId` are distinct types.
2. Existing behavior is explicitly identified as `ME_LIKE` and remains unchanged/default.
3. Machine Cut can render a visibly different dense geometry.
4. Machine Cut reads appearance from the ambient theme rather than storing colors/effects in its facet model.
5. The generic facet vocabulary has no 3×3 assumption.
6. Tests protect the two facet identities and independent selection axes.

Milestone 2 will make Machine Cut interactive and user-selectable.
