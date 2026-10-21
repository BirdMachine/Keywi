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
    /** The touch topology consumed by both the live renderer and settings thumbnails. */
    val touchMain =
        touchLayout(
            "touch-main",
            listOf(
                "1234567890".map { it.toString() },
                "qwertyuiop".map { it.toString() },
                "asdfghjkl".map { it.toString() },
                listOf("⇧") + "zxcvbnm".map { it.toString() } + listOf("⌫"),
                listOf("?123", ",", "☺", "Space", ".", "⚙", "↵", "⌘"),
            ),
        )
    val touchSymbols =
        touchLayout(
            "touch-symbols",
            listOf(
                "1234567890".map { it.toString() },
                listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
                listOf("*", "\"", "'", ":", ";", "!", "?", "=", "_"),
                listOf("ABC", "[", "]", "{", "}", "<", ">", "⌫"),
                listOf("ABC", ",", "☺", "Space", ".", "⚙", "↵", "⌘"),
            ),
        )
    val touchAdvanced =
        touchLayout(
            "touch-advanced",
            listOf(
                (1..12).map { "F$it" },
                listOf("Esc", "Tab", "Ctrl", "Alt", "Win", "Del", "Home", "End"),
                listOf("PgUp", "PgDn", "Ins", "↑", "←", "↓", "→", "⌫"),
                listOf("⇧") + "qwertyuiop".map { it.toString() },
                listOf("ABC", "☺", "Space", "⚙", "↵", "⌘"),
            ),
        )

    private fun touchLayout(
        id: String,
        rows: List<List<String>>,
    ) = MachineCutLayout(
        LayoutId(id),
        rows.map { row ->
            row.map { label ->
                MachineCutCell(
                    label,
                    width =
                        when (label) {
                            "Space" -> 4.4f
                            "⇧", "⌫", "↵", "⌘", "?123", "ABC" -> 1.5f
                            else -> 1f
                        },
                )
            }
        },
    )

    val qwertySkeleton =
        MachineCutLayout(
            id = LayoutId("machine-cut-qwerty"),
            rows =
                listOf(
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
