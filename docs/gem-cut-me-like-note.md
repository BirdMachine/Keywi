# ME-Like compatibility boundary

The existing `KeyboardScreen` remains the authoritative live ME-Like implementation during the first Gem Cut milestone. No current layout, board, swipe, clipboard, emoji, position, haptic, sound or appearance behavior is removed merely to introduce the Facet abstraction.

The later live facet host should wrap/adapt this renderer before attempting deeper extraction. That migration order makes ME-Like the compatibility baseline rather than rewriting it to look like Machine Cut.
