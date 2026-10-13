# Gem Cut — next slice

1. Add persisted `FacetId`, defaulting/migrating to `ME_LIKE`.
2. Add a compact facet selector with ME-Like and Machine Cut previews.
3. Replace Machine Cut's placeholder strings with typed cells carrying `KeyAction` assignments.
4. Reuse existing key-surface/theme primitives rather than styling Machine Cut independently.
5. Route the live IME through a facet host only after ME-Like behavior remains unchanged.
6. Add Machine Cut layers/modifiers incrementally: alpha → shift → numeric/symbol → ctrl/alt/function/navigation.

Acceptance rule: changing Facet must change geometry without changing the selected Theme; changing Theme must repaint either Facet without changing its Layout.
