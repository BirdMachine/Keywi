package com.dessalines.thumbkey.ui.components.keyboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID

class KaomojiSearchSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                var sources by remember { mutableStateOf(KaomojiSearchPreferences.sources(this)) }
                var editing by remember { mutableStateOf(KaomojiSearchPreferences.active(this)) }
                var name by remember(editing) { mutableStateOf(editing.name) }
                var url by remember(editing) { mutableStateOf(editing.url) }
                var selector by remember(editing) { mutableStateOf(editing.selector) }
                var message by remember { mutableStateOf<String?>(null) }
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.windowInsetsPadding(WindowInsets.safeDrawing).padding(20.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TextButton(onClick = { finish() }) { Text("← Back to keyboard") }
                        Text("Kaomoji search sources", style = MaterialTheme.typography.headlineSmall)
                        Text("Choose a source, or add your own search URL. " +
                            "Only tapping Search web sends your search term to that website.")
                        sources.forEach { source ->
                            TextButton(onClick = {
                                KaomojiSearchPreferences.select(this@KaomojiSearchSettingsActivity, source)
                                editing = source
                                message = null
                            }) {
                                Text(if (source.id == editing.id) "● ${source.name}" else source.name)
                            }
                        }
                        TextButton(onClick = {
                            editing = KaomojiSearchSource(UUID.randomUUID().toString(), "", "https://", "")
                            message = null
                        }) { Text("+ Add search source") }
                        OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(url, { url = it }, label = { Text("Search URL") }, modifier = Modifier.fillMaxWidth())
                        Text("Replace the search term with %s. Example: https://emojicombos.com/%s")
                        OutlinedTextField(selector, { selector = it }, label = { Text("Result selector (optional)") }, modifier = Modifier.fillMaxWidth())
                        Text("For results inside Keywi, select the HTML elements containing each kaomoji. " +
                            "Emoji Combos: #comboListEl .emojis. Leave blank to open searches in your browser. " +
                            "Websites that require JavaScript may need the browser.")
                        message?.let { Text(it) }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = {
                                val source = editing.copy(name = name.trim(), url = url.trim(), selector = selector.trim())
                                runCatching { KaomojiSearchPreferences.save(this@KaomojiSearchSettingsActivity, source) }
                                    .onSuccess {
                                        editing = source
                                        sources = KaomojiSearchPreferences.sources(this@KaomojiSearchSettingsActivity)
                                        message = "Saved and selected."
                                    }.onFailure { message = it.message ?: "Check the URL and result selector." }
                            }) { Text("Save & use") }
                            TextButton(onClick = {
                                KaomojiSearchPreferences.remove(this@KaomojiSearchSettingsActivity, editing.id)
                                sources = KaomojiSearchPreferences.sources(this@KaomojiSearchSettingsActivity)
                                editing = KaomojiSearchPreferences.active(this@KaomojiSearchSettingsActivity)
                                message = "Source removed."
                            }) { Text("Remove") }
                        }
                        TextButton(onClick = {
                            editing = KaomojiSearchPreferences.emojiCombos
                            name = editing.name
                            url = editing.url
                            selector = editing.selector
                            message = "Emoji Combos defaults loaded. Tap Save & use to keep them."
                        }) { Text("Restore Emoji Combos defaults") }
                    }
                }
            }
        }
    }
}
