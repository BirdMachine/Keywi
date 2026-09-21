package com.dessalines.thumbkey.textprocessors

/** Shared, prefix-free desktop-style compose sequences. */
object ComposeComboTable {
    val sequences: Map<String, String> = buildMap {
        // Typography / legal
        put("oc", "©"); put("OC", "©"); put("CO", "©"); put("co", "©")
        put("or", "®"); put("OR", "®"); put("RO", "®"); put("ro", "®")
        put("tm", "™"); put("TM", "™"); put("so", "§"); put("p!", "¶")
        put("%o", "‰"); put("..", "…"); put("--", "—"); put("-m", "—"); put("m-", "—")
        put("-n", "–"); put("n-", "–"); put("<<", "«"); put(">>", "»"); put("!!", "¡"); put("??", "¿"); put(".-", "·")

        // Math / programming
        put("+-", "±"); put("-:", "÷"); put("xx", "×")
        put("/=", "≠"); put("=/", "≠"); put("!=", "≠")
        put("=_", "≡"); put("==", "≡"); put("<=", "≤"); put(">=", "≥"); put("~~", "≈")
        put("88", "∞"); put("00", "∞"); put("oo", "°"); put("{}", "∅"); put("/v", "√"); put("mu", "µ")
        put("->", "→"); put("<-", "←"); put("<>", "⋄")

        // Fractions
        put("12", "½"); put("13", "⅓"); put("23", "⅔"); put("14", "¼"); put("34", "¾")

        // Super/subscripts
        "⁰¹²³⁴⁵⁶⁷⁸⁹".forEachIndexed { i, c -> put("^$i", c.toString()) }
        put("^n", "ⁿ")
        "₀₁₂₃₄₅₆₇₈₉".forEachIndexed { i, c -> put("_$i", c.toString()) }

        // Currency
        put("e=", "€"); put("E=", "€"); put("l-", "£"); put("L-", "£")
        put("y=", "¥"); put("Y=", "¥"); put("c/", "¢"); put("/c", "¢"); put("C/", "₡")

        // Letters / ligatures
        put("i.", "ı"); put("ii", "ı"); put("I.", "İ"); put("II", "İ")
        put("ss", "ß"); put("sz", "ß"); put("SS", "ẞ")
        put("ae", "æ"); put("AE", "Æ"); put("oe", "œ"); put("OE", "Œ")
        put("o/", "ø"); put("O/", "Ø"); put("aa", "å"); put("AA", "Å"); put(",c", "ç"); put(",C", "Ç")

        putDiacritic("\"", "aäAÄeëEËiïIÏoöOÖuüUÜyÿ")
        putDiacritic("'", "aáAÁeéEÉiíIÍoóOÓuúUÚyýYÝ")
        putDiacritic("`", "aàAÀeèEÈiìIÌoòOÒuùUÙ")
        putDiacritic("^", "aâAÂeêEÊiîIÎoôOÔuûUÛ")
        putDiacritic("~", "aãAÃnñNÑoõOÕ")
        putDiacritic(".", "aȧbḃcċdḋeėfḟgġhḣiımṁnṅoȯpṗrṙsṡtṫwẇxẋyẏzżAȦBḂCĊDḊEĖFḞGĠHḢIİMṀNṄOȮPṖRṘSṠTṪWẆXẊYẎZŻ")
        putDiacritic("_", "aāeēgḡiīoōuūyȳAĀEĒGḠIĪOŌUŪYȲ")
        putDiacritic("!", "aạbḅdḍeẹhḥiịkḳlḷmṃnṇoọrṛsṣtṭuụvṿwẉyỵzẓAẠBḄDḌEẸHḤIỊKḲLḶMṂNṆOỌRṚSṢTṬUỤVṾWẈYỴZẒ")
    }

    private val prefixes: Set<String> = buildSet {
        sequences.keys.forEach { seq -> for (i in 1 until seq.length) add(seq.substring(0, i)) }
    }

    fun isPrefix(buffer: String): Boolean = prefixes.contains(buffer)
    fun match(buffer: String): String? = sequences[buffer]
}

private fun MutableMap<String, String>.putDiacritic(mark: String, pairs: String) {
    pairs.chunked(2).forEach { pair -> put(mark + pair[0], pair[1].toString()) }
}
