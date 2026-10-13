# Theme reuse across facets

A selected Keywi Theme is global appearance state. Facets do not get private theme documents.

Machine Cut's first renderer therefore reads `MaterialTheme.colorScheme.surface`, `outline`, and `onSurface` supplied by the existing Keywi theme path. As shared key-surface primitives are extracted, Machine Cut should consume those directly so gradients, typography, border modes, backdrop media and effects match ME-Like automatically.
