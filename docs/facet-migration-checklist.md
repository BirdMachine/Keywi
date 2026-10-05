# Facet migration checklist

- [x] Name the existing geometry ME-Like without changing its renderer.
- [x] Model Theme, Facet and Layout as independent concepts.
- [x] Keep the generic Facet contract free of ME-Like's 3×3 assumption.
- [x] Add Machine Cut as a second geometry identity.
- [x] Add a first Machine Cut layout model and themed skeleton renderer.
- [x] Add a facet preview boundary so both geometries can share ambient theme machinery.
- [ ] Persist selected Facet, defaulting old installs to ME-Like.
- [ ] Add Facet selector UI.
- [ ] Convert Machine Cut cells from labels to real KeyAction assignments.
- [ ] Route the live IME through the facet host.
- [ ] Verify backdrop, key surface gradients, borders, typography, effects and layers across both facets.
