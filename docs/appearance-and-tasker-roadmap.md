# Key appearance and action extensions

## Appearance controls

The theme editor supports same, stable random, incremental and shared-canvas border gradients.
The matrix remapper selects individual physical keys or multiple keys as a named group.
Each target can override its border/highlight and fill using a solid color or saved/custom gradient.
Enable a group's shared gradient canvas to normalize its gradient across the bounding rectangle of only its selected keys. Unselected keys retain their own design; gaps remain gaps.
Blank/inherit fields fall back to the underlying group or theme. Positions are zero-based row:column,
shared across board/mode changes. Individual designs win over groups; later groups win over earlier groups.

Further design scope: layout/board-specific targeting and richer per-key media/shapes/typography overrides.

## Tasker actions

The key matrix supports explicitly selected Tasker tasks for a physical key's tap, hold, or directional swipe space. Assignments can replace the ordinary key action or run alongside it. Task names remain separate from text remapping; arbitrary text is never interpreted as an intent.

Install Tasker, grant Keywi's Run Tasks permission, and enable Tasker's Allow External Access. The assignment screen shows setup status and offers Tasker's task picker plus an exact-name fallback. On continuous cursor/delete keys, a replacement swipe resolves on release so it can replace the ordinary edit without an earlier text change. Directional slots follow each key's four/eight-way topology.

Implementation follows Tasker's official invocation protocol:
https://tasker.joaoapps.com/invoketasks.html
https://tasker.joaoapps.com/code/TaskerIntent.java

Tests use a mock receiver; a real Tasker task still needs verification on Kestrel.

## Toolbar pages and gestures

The right-side cycler scrolls through Characters, Formatting, Kaomoji, and Quick Settings with vertical swipes. Horizontal swipes scroll the toolbar. Four page dots identify the current page. Double tap defaults to the configured home page (Characters by default); triple tap opens Quick Settings; hold opens gesture configuration. All three gestures can be reassigned, including disabled actions. Quick Settings keeps configuration reachable and exposes suggestions, sound, GIF, power mode, and settings links.

## Settings artwork

Shared settings artwork is clipped to its own surface, including short app bars. Portrait images and animated drawable frames cannot paint a rectangular overlay over neighboring settings rows.
