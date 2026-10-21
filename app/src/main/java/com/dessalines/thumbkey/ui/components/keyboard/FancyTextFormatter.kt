package com.dessalines.thumbkey.ui.components.keyboard

/**
 * Pure Unicode formatting logic shared by palette previews and editor commits.
 * Unrepresentable characters are left intact rather than silently discarded.
 */
internal object FancyTextFormatter {
    enum class Category(
        val title: String,
    ) {
        CASE("Case"),
        SCRIPTS("Scripts"),
        TYPE("Typography"),
    }

    data class Style(
        val id: String,
        val title: String,
        val category: Category,
    )

    val styles =
        listOf(
            Style("uppercase", "UPPERCASE", Category.CASE),
            Style("lowercase", "lowercase", Category.CASE),
            Style("title", "Title Case", Category.CASE),
            Style("alternating", "aLtErNaTiNg", Category.CASE),
            Style("superscript", "ˢᵘᵖᵉʳ", Category.SCRIPTS),
            Style("subscript", "ₛᵤᵦ", Category.SCRIPTS),
            Style("bold", "𝗕𝗼𝗹𝗱", Category.TYPE),
            Style("italic", "𝘐𝘵𝘢𝘭𝘪𝘤", Category.TYPE),
            Style("monospace", "𝙼𝚘𝚗𝚘", Category.TYPE),
            Style("circled", "ⓒⓘⓡⓒⓛⓔ", Category.TYPE),
            Style("fullwidth", "Ｆｕｌｌ", Category.TYPE),
            Style("fraktur", "𝕱𝖗𝖆𝖐𝖙𝖚𝖗", Category.TYPE),
        )

    // These Unicode alphabets have gaps. ASCII is retained for unavailable glyphs.
    private val superLower =
        listOf(
            "ᵃ",
            "ᵇ",
            "ᶜ",
            "ᵈ",
            "ᵉ",
            "ᶠ",
            "ᵍ",
            "ʰ",
            "ⁱ",
            "ʲ",
            "ᵏ",
            "ˡ",
            "ᵐ",
            "ⁿ",
            "ᵒ",
            "ᵖ",
            "q",
            "ʳ",
            "ˢ",
            "ᵗ",
            "ᵘ",
            "ᵛ",
            "ʷ",
            "ˣ",
            "ʸ",
            "ᶻ",
        )
    private val subLower =
        listOf(
            "ₐ",
            "ᵦ",
            "c",
            "ₔ",
            "ₑ",
            "f",
            "₉",
            "ₕ",
            "ᵢ",
            "ⱼ",
            "ₖ",
            "ₗ",
            "ₘ",
            "ₙ",
            "ₒ",
            "ₚ",
            "q",
            "ᵣ",
            "ₛ",
            "ₜ",
            "ᵤ",
            "ᵥ",
            "w",
            "ₓ",
            "ᵧ",
            "z",
        )

    fun transform(
        text: String,
        style: String,
    ): String =
        when (style) {
            "uppercase" -> {
                text.uppercase()
            }

            "lowercase" -> {
                text.lowercase()
            }

            "title" -> {
                Regex("\\p{L}+").replace(text) { match ->
                    match.value.replaceFirstChar { it.titlecase() }
                }
            }

            "alternating" -> {
                buildString {
                    var position = 0
                    text.forEach { char ->
                        if (char.isLetter()) {
                            append(if (position % 2 == 0) char.lowercaseChar() else char.uppercaseChar())
                            position++
                        } else {
                            append(char)
                            position = 0
                        }
                    }
                }
            }

            "superscript", "subscript" -> {
                buildString {
                    val glyphs = if (style == "superscript") superLower else subLower
                    text.forEach { char ->
                        val index = char.lowercaseChar() - 'a'
                        append(if (index in 0..25 && char.isLetter()) glyphs[index] else char.toString())
                    }
                }
            }

            "bold", "italic", "monospace", "circled", "fullwidth", "fraktur" -> {
                buildString {
                    text.forEach { char ->
                        val upper = char in 'A'..'Z'
                        val lower = char in 'a'..'z'
                        val index = if (upper) char - 'A' else char - 'a'
                        val code =
                            when (style) {
                                "bold" -> {
                                    if (upper) {
                                        0x1D5D4 + index
                                    } else if (lower) {
                                        0x1D5EE + index
                                    } else {
                                        char.code
                                    }
                                }

                                "italic" -> {
                                    if (upper) {
                                        0x1D608 + index
                                    } else if (lower) {
                                        0x1D622 + index
                                    } else {
                                        char.code
                                    }
                                }

                                "monospace" -> {
                                    if (upper) {
                                        0x1D670 + index
                                    } else if (lower) {
                                        0x1D68A + index
                                    } else {
                                        char.code
                                    }
                                }

                                "circled" -> {
                                    if (upper) {
                                        0x24B6 + index
                                    } else if (lower) {
                                        0x24D0 + index
                                    } else {
                                        char.code
                                    }
                                }

                                "fullwidth" -> {
                                    if (char.code in 33..126) char.code + 0xFEE0 else char.code
                                }

                                else -> {
                                    if (upper) {
                                        0x1D56C + index
                                    } else if (lower) {
                                        0x1D586 + index
                                    } else {
                                        char.code
                                    }
                                }
                            }
                        appendCodePoint(code)
                    }
                }
            }

            else -> {
                text
            }
        }
}
