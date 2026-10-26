package com.dessalines.thumbkey.emoji

/**
 * Non-destructive Slack-style emoji lookup. This parser never modifies editor contents.
 * Call [suggest] with the text immediately before the cursor; replace the returned trigger
 * only when the user explicitly selects a suggestion.
 */
object EmojiShortcodeSearch {
    data class Match(val emoji: String, val alias: String)
    data class Suggestions(val trigger: String, val matches: List<Match>)

    private val aliases = listOf(
        Match("🐟", "fish"), Match("🐠", "tropical_fish"), Match("🐡", "blowfish"),
        Match("🎣", "fishing_pole"), Match("🦈", "shark"), Match("🪼", "jellyfish"),
        Match("🐬", "dolphin"), Match("🦜", "parrot"), Match("🐦", "bird"),
        Match("🔥", "fire"), Match("🦊", "fox"), Match("🩷", "pink_heart"),
        Match("💕", "two_hearts"), Match("✨", "sparkles"), Match("🎶", "notes"),
        Match("🌈", "rainbow"), Match("🌙", "crescent_moon"), Match("💎", "gem"),
        Match("😊", "blush"), Match("😂", "joy"), Match("❤️", "heart"),
        Match("👍", "thumbsup"), Match("👀", "eyes"), Match("🎉", "tada"),
    )

    fun suggest(beforeCursor: CharSequence?, limit: Int = 8): Suggestions? {
        if (beforeCursor.isNullOrEmpty() || limit <= 0) return null
        val text = beforeCursor.toString()
        val colon = text.lastIndexOf(':')
        if (colon < 0) return null
        val trigger = text.substring(colon)
        // Avoid consuming an existing URL, ordinary punctuation, or giant tokens.
        if (trigger.length !in 2..36 || !trigger.drop(1).all {
            it in 'a'..'z' || it in 'A'..'Z' || it == '_'
        }) return null
        if (colon > 0 && !text[colon - 1].isWhitespace() &&
            text[colon - 1] !in "([{") return null
        val prefix = trigger.substring(1).lowercase()
        val candidates = aliases.filter { it.alias.startsWith(prefix) || it.alias.contains("_$prefix") }
            .take(limit)
        return if (candidates.isEmpty()) null else Suggestions(trigger, candidates)
    }
}
