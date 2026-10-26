package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.select.Selector
import java.net.URI
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

data class KaomojiSearchSource(
    val id: String,
    val name: String,
    val url: String,
    val selector: String = "",
) {
    fun searchUrl(query: String): String {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8").replace("+", "%20")
        return url.replace("%s", encoded)
    }

    fun isEmojiCombos(): Boolean = runCatching {
        validateSearchUri(searchUrl("test")).host.lowercase() in setOf("emojicombos.com", "www.emojicombos.com")
    }.getOrDefault(false)

    /** The provider flag follows the user-selected Keywi search mode. */
    fun withUncensoredSearch(enabled: Boolean): KaomojiSearchSource {
        if (!isEmojiCombos()) return this
        val base = url.substringBefore('#')
        val fragment = if ('#' in url) "#${url.substringAfter('#')}" else ""
        val query = base.substringAfter('?', "").split('&').filter { it.isNotBlank() && it.substringBefore('=') != "showSensitive" }
        val flag = "showSensitive=${if (enabled) 0 else 1}"
        return copy(url = base.substringBefore('?') + "?" + (query + flag).joinToString("&") + fragment)
    }

    fun uncensoredSearch(): Boolean = url.substringBefore('#').substringAfter('?', "").split('&').any { it == "showSensitive=0" }

    fun validate() {
        require(name.isNotBlank() && name.length <= 80) { "Enter a name (up to 80 characters)." }
        require(url.length <= 2048 && url.contains("%s")) { "Add %s where the search term belongs." }
        validateSearchUri(searchUrl("test"))
        require(selector.length <= 256) { "The result selector is too long." }
        if (selector.isNotBlank()) Selector.evaluatorOf(selector)
    }
}

internal fun validateSearchUri(url: String): URI {
    val uri = URI(url)
    require(uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null) { "Use an HTTPS URL without login details." }
    return uri
}

object KaomojiSearchPreferences {
    val emojiCombos = KaomojiSearchSource("emoji-combos", "Emoji Combos", "https://emojicombos.com/%s?showSensitive=1", "#comboListEl .emojis")

    private fun prefs(context: Context) = context.getSharedPreferences("kaomoji_search_sources", Context.MODE_PRIVATE)

    fun sources(context: Context): List<KaomojiSearchSource> =
        runCatching {
            val array = JSONArray(prefs(context).getString("sources", "[]"))
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                upgradeLegacySource(KaomojiSearchSource(item.getString("id"), item.getString("name"), item.getString("url"), item.optString("selector")))
            }.ifEmpty { listOf(emojiCombos) }
        }.getOrDefault(listOf(emojiCombos))

    internal fun upgradeLegacySource(source: KaomojiSearchSource): KaomojiSearchSource =
        if (source.id == emojiCombos.id && source.url == "https://emojicombos.com/%s") source.copy(url = emojiCombos.url) else source

    fun active(context: Context): KaomojiSearchSource {
        val sources = sources(context)
        return sources.firstOrNull { it.id == prefs(context).getString("active", null) } ?: sources.first()
    }

    fun select(context: Context, source: KaomojiSearchSource) {
        prefs(context).edit().putString("active", source.id).apply()
    }

    fun save(context: Context, source: KaomojiSearchSource) {
        source.validate()
        val updated = sources(context).filterNot { it.id == source.id } + source
        val array = JSONArray()
        updated.forEach { entry ->
            array.put(JSONObject().put("id", entry.id).put("name", entry.name).put("url", entry.url).put("selector", entry.selector))
        }
        prefs(context).edit().putString("sources", array.toString()).putString("active", source.id).apply()
    }

    fun remove(context: Context, id: String) {
        val remaining = sources(context).filterNot { it.id == id }.ifEmpty { listOf(emojiCombos) }
        val array = JSONArray()
        remaining.forEach { entry ->
            array.put(JSONObject().put("id", entry.id).put("name", entry.name).put("url", entry.url).put("selector", entry.selector))
        }
        prefs(context).edit().putString("sources", array.toString()).putString("active", remaining.first().id).apply()
    }
}

object KaomojiWebSearch {
    /** Only returned text is used. Scripts, images, ads and page event handlers never run. */
    fun parse(html: String, source: KaomojiSearchSource): List<StringPaletteItem> {
        require(source.selector.isNotBlank()) { "Add a result selector to show results inside Keywi." }
        return Jsoup.parse(html).select(source.selector).asSequence()
            .map { it.wholeText().trim() }
            .filter { it.isNotEmpty() && it.length <= 512 && it.none { ch -> ch.isISOControl() && ch != '\n' && ch != '\t' } }
            .distinct()
            .take(100)
            .map { StringPaletteItem(it, source.name) }
            .toList()
    }

    suspend fun search(source: KaomojiSearchSource, query: String): List<StringPaletteItem> = withContext(Dispatchers.IO) {
        source.validate()
        require(query.isNotBlank() && query.length <= 200) { "Use a search term up to 200 characters." }
        var uri = validateSearchUri(source.searchUrl(query))
        repeat(4) { attempt ->
            val connection = uri.toURL().openConnection() as HttpsURLConnection
            try {
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("User-Agent", "Keywi/1.0 (kaomoji search)")
                val status = connection.responseCode
                if (status in listOf(301, 302, 303, 307, 308)) {
                    require(attempt < 3) { "Too many redirects. Try opening the website." }
                    val location = connection.getHeaderField("Location") ?: error("Missing redirect address.")
                    uri = validateSearchUri(uri.resolve(location).toString())
                } else {
                    require(status == 200) { "Search website returned HTTP $status. Try opening the website." }
                    val bytes = connection.inputStream.use { it.readBytesBounded() }
                    return@withContext parse(String(bytes, Charsets.UTF_8), source)
                }
            } finally {
                connection.disconnect()
            }
        }
        error("Search could not complete.")
    }

    private fun java.io.InputStream.readBytesBounded(): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = read(buffer)
            if (count < 0) break
            require(output.size() + count <= 4 * 1024 * 1024) { "Search page is too large. Try opening the website." }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }
}
