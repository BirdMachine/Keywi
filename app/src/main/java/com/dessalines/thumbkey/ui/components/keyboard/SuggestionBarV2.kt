package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import android.provider.UserDictionary
import android.text.InputType
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.IMEService
import com.dessalines.thumbkey.diagnostics.KeywiDiagnostics
import com.dessalines.thumbkey.effects.ActionSoundEngine
import com.dessalines.thumbkey.effects.SoundEvent
import com.dessalines.thumbkey.effects.SoundTrigger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import java.util.Locale

private const val BAR_HEIGHT_DP = 42
private const val MOTION_PREFS = "suggestion_motion_preferences"
private const val MOTION_STYLE = "motion_style"
private const val MAX_VISIBLE_SUGGESTIONS = 5
private val TOKEN_PATTERN_V2 = Regex("\\S+$")
private val SUGGESTION_PREFIX_PATTERN_V2 = Regex("[A-Za-z']+$")

enum class SuggestionMotionStyle {
    NONE,
    SPRINGY,
    GOOEY,
}

object SuggestionMotionPreferences {
    fun load(context: Context): SuggestionMotionStyle {
        val stored =
            context
                .getSharedPreferences(MOTION_PREFS, Context.MODE_PRIVATE)
                .getString(MOTION_STYLE, SuggestionMotionStyle.SPRINGY.name)
        return runCatching { SuggestionMotionStyle.valueOf(stored!!) }
            .getOrDefault(SuggestionMotionStyle.SPRINGY)
    }

    fun save(
        context: Context,
        style: SuggestionMotionStyle,
    ) {
        context
            .getSharedPreferences(MOTION_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(MOTION_STYLE, style.name)
            .apply()
    }
}

private object LocalSuggestionEngineV2 {
    private val commonWords =
        """
        about above absolutely actually add added after again all almost already also always amazing and android animation another answer any anything app application are around ask asked awesome back background bad bar because become been before best better between big bird birds black blue border both build button called can change changes clean clear clipboard close code color colors come commit complete completion computer control controls cool correct correction create created current cursor custom data day default definitely delete device dictionary different disable disabled does easy edit editor email emoji enable enabled english enough enter especially even every everything exactly example feature features feel feels file files finally find first fix fixed font found friend friends full fun funny fuzzy game games get give go gold good gradient great guess happen happened happy hard have hello help here hey hide how idea image images input install installed issue just keep keyboard key keys keywi kind know language last layer learn learning left less light like little live load loaded local long look love lovely made make many match matching maybe mean means message messagease middle might more most move moved much name need needed never new next nice night nope nothing number of offline okay old on online only opacity open option options other output overlay part persist persistent phone photo photos picture pink place play please point position possible possibly prefer preset pretty preview private probably problem public purple question quick rainbow read reading really reason red remember remove removed right run running same samsung save saved screen setting settings shiny short should show side simple single size slow small software something sometimes space spell spelling start still stop stops strip style suggestion suggestions sure swipe system take tap test testing text than thank thanks that the their them then there these thing things think this thought through thumbkey time today together toggle top touch transparent transparency try tried trying type typing under understand update use used using version very want wanted water way we week well what when where which while white why width will wish with word words work working works world worse would write writing yeah year yes yesterday you your
        """.trimIndent()
            .split(Regex("\\s+"))
            .map { it.trim().lowercase(Locale.US) }
            .filter { it.length > 1 }
            .distinct()

    fun suggest(
        prefix: String,
        limit: Int = MAX_VISIBLE_SUGGESTIONS,
    ): List<String> {
        if (prefix.length < 2) return emptyList()
        val normalized = prefix.lowercase(Locale.US)
        val matches =
            commonWords
                .asSequence()
                .filter { it.startsWith(normalized) && it != normalized }
                .take(limit)
                .toList()
        return if (prefix.firstOrNull()?.isUpperCase() == true) {
            matches.map { word -> word.replaceFirstChar { it.titlecase(Locale.US) } }
        } else {
            matches
        }
    }
}

private fun lozengeTransform(style: SuggestionMotionStyle): ContentTransform =
    when (style) {
        SuggestionMotionStyle.NONE -> {
            fadeIn(tween(1)) togetherWith fadeOut(tween(1))
        }

        SuggestionMotionStyle.SPRINGY -> {
            val enter =
                fadeIn(tween(70)) +
                    scaleIn(
                        initialScale = 0.90f,
                        animationSpec =
                            keyframes {
                                durationMillis = 190
                                0.90f at 0
                                1.055f at 70
                                0.975f at 125
                                1f at 190
                            },
                    )
            val exit =
                fadeOut(tween(120)) +
                    scaleOut(
                        targetScale = 0.86f,
                        animationSpec =
                            keyframes {
                                durationMillis = 155
                                1f at 0
                                1.045f at 48
                                0.965f at 92
                                0.86f at 155
                            },
                    )
            enter togetherWith exit
        }

        SuggestionMotionStyle.GOOEY -> {
            val enter =
                fadeIn(tween(95)) +
                    scaleIn(
                        initialScale = 0.58f,
                        animationSpec =
                            keyframes {
                                durationMillis = 300
                                0.58f at 0
                                1.14f at 105
                                0.92f at 175
                                1.055f at 235
                                1f at 300
                            },
                    )
            val exit =
                fadeOut(tween(190)) +
                    scaleOut(
                        targetScale = 0.48f,
                        animationSpec =
                            keyframes {
                                durationMillis = 245
                                1f at 0
                                1.09f at 65
                                0.91f at 125
                                0.48f at 245
                            },
                    )
            enter togetherWith exit
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SuggestionLozengeSlot(
    suggestion: String?,
    slotIndex: Int,
    displayedCount: Int,
    motionStyle: SuggestionMotionStyle,
    theme: SuggestionLozengeThemeState,
    isCurrentWord: Boolean = false,
    newWordHighlightColor: Color,
    displayText: String? = null,
    onSuggestionClick: (String) -> Unit,
    onSuggestionLongClick: ((String) -> Unit)? = null,
) {
    AnimatedContent(
        targetState = suggestion,
        transitionSpec = { lozengeTransform(motionStyle) },
        contentAlignment = Alignment.Center,
        label = "suggestion-lozenge-$slotIndex",
    ) { currentSuggestion ->
        if (currentSuggestion == null) {
            Box(modifier = Modifier)
        } else {
            val isBest = !isCurrentWord && ((displayedCount > 1 && slotIndex == 1) || displayedCount == 1)
            Box(modifier = Modifier.padding(end = 6.dp)) {
                val shape = RoundedCornerShape(18.dp)
                val themedModifier =
                    Modifier
                        .widthIn(min = 58.dp, max = 180.dp)
                        .clip(shape)
                        .then(
                            if (isCurrentWord) {
                                Modifier
                            } else {
                                when (theme.surfaceStyle) {
                                    SuggestionLozengeSurfaceStyle.GRADIENT -> {
                                        Modifier.keyboardGradientBackground(theme.surfaceGradient)
                                    }

                                    SuggestionLozengeSurfaceStyle.SOLID,
                                    SuggestionLozengeSurfaceStyle.NONE,
                                    -> {
                                        Modifier
                                    }
                                }
                            },
                        ).then(
                            if (isCurrentWord) {
                                Modifier
                            } else {
                                when (theme.borderStyle) {
                                    SuggestionLozengeBorderStyle.GRADIENT -> {
                                        Modifier.keyboardGradientBorder(
                                            backdrop = theme.borderGradient,
                                            width = theme.borderWidth.dp,
                                            radius = 18.dp,
                                        )
                                    }

                                    SuggestionLozengeBorderStyle.SOLID,
                                    SuggestionLozengeBorderStyle.NONE,
                                    -> {
                                        Modifier
                                    }
                                }
                            },
                        )
                Surface(
                    shape = shape,
                    color =
                        if (isCurrentWord) {
                            theme.currentWordBackgroundColor
                        } else {
                            when (theme.surfaceStyle) {
                                SuggestionLozengeSurfaceStyle.SOLID -> {
                                    theme.surfaceColor
                                }

                                SuggestionLozengeSurfaceStyle.GRADIENT,
                                SuggestionLozengeSurfaceStyle.NONE,
                                -> {
                                    Color.Transparent
                                }
                            }
                        },
                    tonalElevation = if (isBest) 2.dp else 0.dp,
                    shadowElevation = if (isBest) 2.dp else 1.dp,
                    border =
                        if (isCurrentWord) {
                            BorderStroke(1.dp, theme.currentWordHighlightColor ?: newWordHighlightColor)
                        } else {
                            when (theme.borderStyle) {
                                SuggestionLozengeBorderStyle.SOLID -> {
                                    BorderStroke(theme.borderWidth.dp, theme.borderColor)
                                }

                                SuggestionLozengeBorderStyle.GRADIENT,
                                SuggestionLozengeBorderStyle.NONE,
                                -> {
                                    null
                                }
                            }
                        },
                    modifier =
                        themedModifier
                            .animateContentSize(
                                animationSpec =
                                    when (motionStyle) {
                                        SuggestionMotionStyle.NONE -> {
                                            spring(stiffness = Spring.StiffnessHigh)
                                        }

                                        SuggestionMotionStyle.SPRINGY -> {
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow,
                                            )
                                        }

                                        SuggestionMotionStyle.GOOEY -> {
                                            spring(
                                                dampingRatio = 0.30f,
                                                stiffness = Spring.StiffnessLow,
                                            )
                                        }
                                    },
                            ).combinedClickable(
                                onClick = { onSuggestionClick(currentSuggestion) },
                                onLongClick = { onSuggestionLongClick?.invoke(currentSuggestion) },
                            ),
                ) {
                    Text(
                        text = displayText ?: currentSuggestion,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color =
                            if (isCurrentWord) {
                                theme.currentWordHighlightColor ?: newWordHighlightColor
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = if (isBest) 1f else 0.88f,
                                )
                            },
                        fontWeight = if (isCurrentWord || isBest) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GemToolbarAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Use the same gem material as keys, without a separate opaque rectangle.
    PaletteKeySurface(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        radius = 0,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
fun SuggestionBarV2(
    ime: IMEService,
    onKaomoji: () -> Unit = {},
    onFancyOpen: () -> Unit = {},
    page: ToolbarPage = ToolbarPage.CHARACTERS,
    onPage: (ToolbarPage) -> Unit = {},
) {
    val suggestionScroll = rememberScrollState()
    LaunchedEffect(suggestionScroll, page) {
        if (page != ToolbarPage.CHARACTERS) return@LaunchedEffect
        var previous = suggestionScroll.value
        var lastSoundAt = 0L
        snapshotFlow { suggestionScroll.value }.collect { position ->
            val now = SystemClock.uptimeMillis()
            if (kotlin.math.abs(position - previous) >= 8 && now - lastSoundAt >= 70L) {
                ActionSoundEngine.fire(ime, event = SoundEvent.SUGGESTION_SCROLL, phase = SoundTrigger.PRESS)
                lastSoundAt = now
            }
            previous = position
        }
    }
    var enabled by remember { mutableStateOf(SuggestionPreferences.enabled(ime)) }
    DisposableEffect(ime) {
        val preferences = ime.getSharedPreferences("suggestion_preferences", Context.MODE_PRIVATE)
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                enabled = SuggestionPreferences.enabled(ime)
            }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    var currentToken by remember { mutableStateOf("") }
    var prefix by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(emptyList<String>()) }
    var showCurrentWord by remember { mutableStateOf(AdvancedKeyWordPreferences.showCurrentWord(ime)) }
    var longPressAddWord by remember { mutableStateOf(AdvancedKeyWordPreferences.longPressAddWord(ime)) }
    val motionStyle = remember { SuggestionMotionPreferences.load(ime) }
    val lozengeTheme = SuggestionLozengeThemePreferences.load(ime)
    var newWordHighlightColor by remember { mutableStateOf(AdvancedKeyWordPreferences.newWordHighlightColor(ime)) }
    var justAddedWord by remember { mutableStateOf<String?>(null) }
    var connectionWasMissing by remember { mutableStateOf(false) }

    val inputType = ime.currentInputEditorInfo?.inputType ?: 0
    val variation = inputType and InputType.TYPE_MASK_VARIATION
    val privateField =
        variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD

    LaunchedEffect(justAddedWord) {
        if (justAddedWord != null) {
            delay(1100)
            justAddedWord = null
        }
    }

    SuggestionPollingEffect(
        page = page,
        enabled = enabled,
        privateField = privateField,
        onInactive = {
            currentToken = ""
            prefix = ""
            suggestions = emptyList()
            justAddedWord = null
            connectionWasMissing = false
        },
    ) {
        showCurrentWord = AdvancedKeyWordPreferences.showCurrentWord(ime)
        longPressAddWord = AdvancedKeyWordPreferences.longPressAddWord(ime)
        newWordHighlightColor = AdvancedKeyWordPreferences.newWordHighlightColor(ime)
        val connection = ime.activeInputConnectionOrNull()
        if (connection == null) {
            if (!connectionWasMissing) {
                KeywiDiagnostics.event("IME", "suggestion polling paused: no active input connection")
                connectionWasMissing = true
            }
            return@SuggestionPollingEffect
        } else if (connectionWasMissing) {
            KeywiDiagnostics.event("IME", "suggestion polling resumed: input connection available")
            connectionWasMissing = false
        }

        val cursorReadStarted = SystemClock.uptimeMillis()
        val beforeCursor =
            connection
                .getTextBeforeCursor(64, 0)
                ?.toString()
                .orEmpty()
        KeywiDiagnostics.performance(
            "PERF_INPUT",
            "read text-before-cursor",
            SystemClock.uptimeMillis() - cursorReadStarted,
        )

        val nextToken = TOKEN_PATTERN_V2.find(beforeCursor)?.value.orEmpty()
        val nextPrefix = SUGGESTION_PREFIX_PATTERN_V2.find(nextToken)?.value.orEmpty()
        if (nextToken != currentToken || nextPrefix != prefix) {
            currentToken = nextToken
            prefix = nextPrefix
            val suggestStarted = SystemClock.uptimeMillis()
            suggestions = KeywiSuggestionEngine.suggest(ime, nextToken, nextPrefix, MAX_VISIBLE_SUGGESTIONS)
            KeywiDiagnostics.performance(
                "PERF_INPUT",
                "suggestion lookup",
                SystemClock.uptimeMillis() - suggestStarted,
            )
        }
    }

    val orderedSuggestions =
        when (suggestions.size) {
            0, 1 -> {
                suggestions
            }

            2 -> {
                listOf(suggestions[1], suggestions[0])
            }

            else -> {
                listOf(suggestions[1], suggestions[0]) + suggestions.drop(2)
            }
        }
    val currentWordVisible = page == ToolbarPage.CHARACTERS && enabled && !privateField && showCurrentWord && currentToken.isNotEmpty()
    val displayed =
        ((if (currentWordVisible) listOf(currentToken) else emptyList()) + orderedSuggestions)
            .take(MAX_VISIBLE_SUGGESTIONS)

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = if (enabled) 0.30f else 0.20f),
            modifier = Modifier.fillMaxWidth().height(BAR_HEIGHT_DP.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(start = 6.dp, end = 0.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SuggestionToolbarContent(
                    page = page,
                    modifier = Modifier.weight(1f).fillMaxSize(),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .horizontalScroll(suggestionScroll)
                                .padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(MAX_VISIBLE_SUGGESTIONS) { slotIndex ->
                            val isCurrentWord = currentWordVisible && slotIndex == 0
                            SuggestionLozengeSlot(
                                suggestion = displayed.getOrNull(slotIndex),
                                slotIndex = slotIndex,
                                displayedCount = displayed.size,
                                motionStyle = motionStyle,
                                theme = lozengeTheme,
                                isCurrentWord = isCurrentWord,
                                newWordHighlightColor = newWordHighlightColor,
                                displayText =
                                    if (isCurrentWord && justAddedWord == currentToken) {
                                        "✓ Added"
                                    } else {
                                        null
                                    },
                                onSuggestionClick = { suggestion ->
                                    val replacementLength = if (isCurrentWord) currentToken.length else prefix.length
                                    if (replacementLength > 0) {
                                        val connection = ime.activeInputConnectionOrNull()
                                        connection?.deleteSurroundingText(replacementLength, 0)
                                        connection?.commitText("$suggestion ", 1)
                                        if (connection != null) {
                                            ActionSoundEngine.fire(ime, event = SoundEvent.SUGGESTION_SELECTED, phase = SoundTrigger.PRESS)
                                        }
                                        currentToken = ""
                                        prefix = ""
                                        suggestions = emptyList()
                                    }
                                },
                                onSuggestionLongClick =
                                    if (isCurrentWord && longPressAddWord) {
                                        { word ->
                                            runCatching {
                                                UserDictionary.Words.addWord(
                                                    ime,
                                                    word,
                                                    250,
                                                    null,
                                                    Locale.getDefault(),
                                                )
                                            }.onSuccess {
                                                KeywiPersonalDictionary.add(ime, word)
                                                suggestions =
                                                    KeywiSuggestionEngine.suggest(
                                                        ime,
                                                        currentToken,
                                                        prefix,
                                                        MAX_VISIBLE_SUGGESTIONS,
                                                    )
                                                justAddedWord = word
                                                Toast
                                                    .makeText(
                                                        ime,
                                                        "Added “$word” to personal dictionary",
                                                        Toast.LENGTH_SHORT,
                                                    ).show()
                                            }.onFailure {
                                                Toast
                                                    .makeText(
                                                        ime,
                                                        "Could not add “$word”",
                                                        Toast.LENGTH_SHORT,
                                                    ).show()
                                            }
                                        }
                                    } else {
                                        null
                                    },
                            )
                        }
                    }
                }

                ToolbarCycler(
                    page = page,
                    enabled = enabled,
                    onToggle = {
                        enabled = !enabled
                        SuggestionPreferences.setEnabled(ime, enabled)
                    },
                    onPage = onPage,
                )
            }
        }
    }
}

/** The toggle follows the key border, including its metallic gradient, above the active fill. */
private fun Modifier.suggestionToggleKeyBorder(theme: KeyThemeState): Modifier =
    when (theme.borderStyle) {
        KeyBorderStyle.GRADIENT -> {
            keyboardGradientBorder(theme.borderGradient, 2.dp, 18.dp)
        }

        KeyBorderStyle.SOLID -> {
            drawWithContent {
                drawContent()
                val stroke = 2.dp.toPx()
                val inset = stroke / 2f
                drawRoundRect(
                    color = theme.borderColor,
                    topLeft =
                        androidx.compose.ui.geometry
                            .Offset(inset, inset),
                    size =
                        androidx.compose.ui.geometry
                            .Size((size.width - stroke).coerceAtLeast(0f), (size.height - stroke).coerceAtLeast(0f)),
                    cornerRadius =
                        androidx.compose.ui.geometry
                            .CornerRadius((18.dp.toPx() - inset).coerceAtLeast(0f)),
                    style = Stroke(stroke),
                )
            }
        }

        KeyBorderStyle.SHADOW, KeyBorderStyle.NONE -> {
            this
        }
    }

/** The character workspace is absent from the composition while a tool occupies the toolbar. */
@Composable
internal fun SuggestionToolbarContent(
    page: ToolbarPage,
    modifier: Modifier = Modifier,
    characterContent: @Composable () -> Unit,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (page == ToolbarPage.CHARACTERS) {
            characterContent()
        } else {
            Text(
                text = page.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = .78f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .semantics {
                            heading()
                            liveRegion = LiveRegionMode.Polite
                        },
            )
        }
    }
}

/** Page changes cancel the editor-reading job before a tool view starts its own interaction. */
@Composable
internal fun SuggestionPollingEffect(
    page: ToolbarPage,
    enabled: Boolean,
    privateField: Boolean,
    onInactive: () -> Unit,
    onPoll: () -> Unit,
) {
    val poll by rememberUpdatedState(onPoll)
    val clear by rememberUpdatedState(onInactive)
    LaunchedEffect(page, enabled, privateField) {
        if (page != ToolbarPage.CHARACTERS || !enabled || privateField) {
            clear()
            return@LaunchedEffect
        }
        while (isActive) {
            poll()
            delay(80)
        }
    }
}
