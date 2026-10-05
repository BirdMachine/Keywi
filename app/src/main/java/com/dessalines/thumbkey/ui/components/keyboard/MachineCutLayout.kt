package com.dessalines.thumbkey.ui.components.keyboard

/**
 * Geometry/content bridge for Machine Cut. These cells carry semantic slots;
 * visual appearance remains owned by Theme.
 */
enum class MachineCutKeyRole {
    CHARACTER,
    MODIFIER,
    NAVIGATION,
    SPACE,
    SYSTEM,
}

data class MachineCutCell(
    val label: String,
    val role: MachineCutKeyRole = MachineCutKeyRole.CHARACTER,
    val width: Float = 1f,
)

data class MachineCutLayout(
    val id: LayoutId,
    val rows: List<List<MachineCutCell>>,
)

object MachineCutLayouts {
    val qwertySkeleton = MachineCutLayout(
        id = LayoutId("machine-cut-qwerty"),
        rows = listOf(
            listOf("Esc", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "−", "=", "⌫").map { MachineCutCell(it) },
            listOf("↹", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]", "\\").map { MachineCutCell(it) },
            listOf("Ctrl", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "↵").map { MachineCutCell(it) },
            listOf("⇧", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/", "△", "⇧").map { MachineCutCell(it) },
            listOf(
                MachineCutCell("Esc", MachineCutKeyRole.SYSTEM, 1.35f),
                MachineCutCell("◉", MachineCutKeyRole.SYSTEM),
                MachineCutCell("Alt", MachineCutKeyRole.MODIFIER, 1.35f),
                MachineCutCell("◆", MachineCutKeyRole.SYSTEM),
                MachineCutCell("Space", MachineCutKeyRole.SPACE, 4.4f),
                MachineCutCell("Fn", MachineCutKeyRole.MODIFIER, 1.35f),
                MachineCutCell("○", MachineCutKeyRole.SYSTEM),
                MachineCutCell("◁", MachineCutKeyRole.NAVIGATION),
                MachineCutCell("▽", MachineCutKeyRole.NAVIGATION),
                MachineCutCell("▷", MachineCutKeyRole.NAVIGATION),
            ),
        ),
    )
}
