package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.util.Log
import android.text.InputType
import com.dessalines.thumbkey.emoji.EmojiShortcodeSearch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Arrangement
import android.view.HapticFeedbackConstants
import android.view.inputmethod.InputConnection.CURSOR_UPDATE_MONITOR
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import com.dessalines.thumbkey.IMEService
import com.dessalines.thumbkey.R
import com.dessalines.thumbkey.db.AppSettings
import com.dessalines.thumbkey.db.ClipboardRepository
import com.dessalines.thumbkey.db.DEFAULT_ANIMATION_HELPER_SPEED
import com.dessalines.thumbkey.db.DEFAULT_ANIMATION_SPEED
import com.dessalines.thumbkey.db.DEFAULT_AUTO_CAPITALIZE
import com.dessalines.thumbkey.db.DEFAULT_AUTO_SIZE_KEYS
import com.dessalines.thumbkey.db.DEFAULT_BACKDROP_ENABLED
import com.dessalines.thumbkey.db.DEFAULT_CIRCULAR_DRAG_ENABLED
import com.dessalines.thumbkey.db.DEFAULT_CLIPBOARD_HISTORY_ENABLED
import com.dessalines.thumbkey.db.DEFAULT_CLOCKWISE_DRAG_ACTION
import com.dessalines.thumbkey.db.DEFAULT_COUNTERCLOCKWISE_DRAG_ACTION
import com.dessalines.thumbkey.db.DEFAULT_DRAG_RETURN_ENABLED
import com.dessalines.thumbkey.db.DEFAULT_GHOST_KEYS_ENABLED
import com.dessalines.thumbkey.db.DEFAULT_HIDE_LETTERS
import com.dessalines.thumbkey.db.DEFAULT_HIDE_SYMBOLS
import com.dessalines.thumbkey.db.DEFAULT_IGNORE_BOTTOM_PADDING
import com.dessalines.thumbkey.db.DEFAULT_KEYBOARD_LAYOUT
import com.dessalines.thumbkey.db.DEFAULT_KEY_BORDER_WIDTH
import com.dessalines.thumbkey.db.DEFAULT_KEY_HEIGHT
import com.dessalines.thumbkey.db.DEFAULT_KEY_PADDING
import com.dessalines.thumbkey.db.DEFAULT_KEY_RADIUS
import com.dessalines.thumbkey.db.DEFAULT_KEY_WIDTH
import com.dessalines.thumbkey.db.DEFAULT_MIN_SWIPE_LENGTH
import com.dessalines.thumbkey.db.DEFAULT_NON_SQUARE_KEYS
import com.dessalines.thumbkey.db.DEFAULT_POSITION
import com.dessalines.thumbkey.db.DEFAULT_POSITION_PADDING
import com.dessalines.thumbkey.db.DEFAULT_PUSHUP_SIZE
import com.dessalines.thumbkey.db.DEFAULT_SLIDE_BACKSPACE_DEADZONE_ENABLED
import com.dessalines.thumbkey.db.DEFAULT_SLIDE_CURSOR_MOVEMENT_MODE
import com.dessalines.thumbkey.db.DEFAULT_SLIDE_ENABLED
import com.dessalines.thumbkey.db.DEFAULT_SLIDE_HOLD_ENABLED
import com.dessalines.thumbkey.db.DEFAULT_SLIDE_SENSITIVITY
import com.dessalines.thumbkey.db.DEFAULT_SLIDE_SPACEBAR_DEADZONE_ENABLED
import com.dessalines.thumbkey.db.DEFAULT_SOUND_ON_TAP
import com.dessalines.thumbkey.db.DEFAULT_SPACEBAR_MULTITAPS
import com.dessalines.thumbkey.db.DEFAULT_SWITCH_TO_LETTERS_AFTER_SPACE
import com.dessalines.thumbkey.db.DEFAULT_VIBRATE_ON_SLIDE
import com.dessalines.thumbkey.db.DEFAULT_VIBRATE_ON_TAP
import com.dessalines.thumbkey.keyboards.BACKSPACE_KEY_ITEM
import com.dessalines.thumbkey.keyboards.EMOJI_BACK_KEY_ITEM
import com.dessalines.thumbkey.keyboards.KB_EN_THUMBKEY_MAIN
import com.dessalines.thumbkey.keyboards.NUMERIC_KEY_ITEM
import com.dessalines.thumbkey.keyboards.RETURN_KEY_ITEM
import com.dessalines.thumbkey.utils.CircularDragAction
import com.dessalines.thumbkey.utils.ColorVariant
import com.dessalines.thumbkey.utils.KeyAction
import com.dessalines.thumbkey.utils.KeyboardLayout
import com.dessalines.thumbkey.utils.KeyboardMode
import com.dessalines.thumbkey.utils.KeyboardPosition
import com.dessalines.thumbkey.utils.SlideType
import com.dessalines.thumbkey.utils.TAG
import com.dessalines.thumbkey.utils.getAutoKeyWidth
import com.dessalines.thumbkey.utils.getKeyboardMode
import com.dessalines.thumbkey.utils.getModifiedKeyboardDefinition
import com.dessalines.thumbkey.utils.keyboardPositionToAlignment
import com.dessalines.thumbkey.utils.toBool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.sortedBy
import kotlin.time.TimeMark

@Composable
fun KeyboardScreen(
    settings: AppSettings?,
    clipboardRepository: ClipboardRepository,
    onSwitchLanguage: () -> Unit,
    onChangePosition: ((old: KeyboardPosition) -> KeyboardPosition) -> Unit,
    onToggleHideLetters: () -> Unit,
    onGoToClipboardSettings: () -> Unit,
    onTyped: () -> Unit = {},
) {
    val ctx = LocalContext.current as IMEService
    var emojiSuggestions by remember { mutableStateOf<EmojiShortcodeSearch.Suggestions?>(null) }
    fun refreshEmojiSuggestions() {
        val type = ctx.currentInputEditorInfo?.inputType ?: 0
        val variation = type and InputType.TYPE_MASK_VARIATION
        val secret = (type and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT &&
            variation in setOf(InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD, InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)
        emojiSuggestions = if (secret) null else
            EmojiShortcodeSearch.suggest(ctx.activeInputConnectionOrNull()?.getTextBeforeCursor(48, 0))
    }
    fun chooseEmoji(emoji: String) {
        val ic = ctx.activeInputConnectionOrNull() ?: return
        val current = EmojiShortcodeSearch.suggest(ic.getTextBeforeCursor(48, 0))
        val old = emojiSuggestions
        if (old != null && current?.trigger == old.trigger && current.matches.any { it.emoji == emoji }) {
            ic.beginBatchEdit()
            try {
                ic.deleteSurroundingText(old.trigger.length, 0)
                ic.commitText(emoji, 1)
            } finally {
                ic.endBatchEdit()
            }
            emojiSuggestions = null
            onTyped()
        } else refreshEmojiSuggestions()
    }

    val layout =
        KeyboardLayout.entries.sortedBy { it.ordinal }[
            settings?.keyboardLayout
                ?: DEFAULT_KEYBOARD_LAYOUT,
        ]

    val boards = rememberCustomBoards().filter { it.enabled }
    var boardId by remember { mutableStateOf<String?>(null) }
    val activeBoard = boards.firstOrNull { it.id == boardId }
    val keyMods = settings?.keyModifications
    val baseDefinition =
        remember(layout, keyMods) {
            if (!keyMods.isNullOrEmpty()) {
                getModifiedKeyboardDefinition(layout, keyMods)
                    ?: layout.keyboardDefinition
            } else {
                layout.keyboardDefinition
            }
        }
    val keyboardDefinition = remember(baseDefinition, activeBoard) { activeBoard?.definition() ?: baseDefinition }

    var mode by remember {
        val startMode =
            getKeyboardMode(
                ime = ctx,
                autoCapitalize = settings?.autoCapitalize?.toBool() == true && keyboardDefinition.settings.autoShift,
            )

        mutableStateOf(startMode)
    }

    var capsLock by remember {
        mutableStateOf(false)
    }

    // TODO get rid of this crap
    val lastAction = remember { mutableStateOf<Pair<KeyAction, TimeMark>?>(null) }

    val clipboardItems by clipboardRepository.allClipboardItems.observeAsState(initial = emptyList())

    val baseKeyboard =
        when (mode) {
            KeyboardMode.MAIN -> {
                keyboardDefinition.modes.main
            }

            KeyboardMode.SHIFTED -> {
                keyboardDefinition.modes.shifted
            }

            KeyboardMode.NUMERIC -> {
                keyboardDefinition.modes.numeric
            }

            KeyboardMode.CTRLED -> {
                keyboardDefinition.modes.ctrled ?: run {
                    val text = stringResource(R.string.warning_invalid_mode, mode, keyboardDefinition.title)
                    Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, text)
                    mode = KeyboardMode.MAIN
                    keyboardDefinition.modes.main
                }
            }

            KeyboardMode.ALTED -> {
                keyboardDefinition.modes.alted ?: run {
                    val text = stringResource(R.string.warning_invalid_mode, mode, keyboardDefinition.title)
                    Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, text)
                    mode = KeyboardMode.MAIN
                    keyboardDefinition.modes.main
                }
            }

            else -> {
                KB_EN_THUMBKEY_MAIN
            }
        }

    SideEffect { ctx.currentKeyboardDefinition = keyboardDefinition }
    LaunchedEffect(boardId, activeBoard?.id) {
        if (boardId != null && activeBoard == null) {
            boardId = null
            mode = KeyboardMode.MAIN
            capsLock = false
        }
    }
    val keyboard =
        remember(baseKeyboard, baseDefinition, activeBoard, mode, boards.isNotEmpty()) {
            if (activeBoard != null) {
                // The custom 3x3 replaces only the main letter grid, never the right tools
                // or the bottom spacebar/enter row from the selected host geometry.
                val host = baseDefinition.modes.main
                activeBoard.overlayOn(host, mode == KeyboardMode.NUMERIC).withBoardPaging()
            } else if (boards.isNotEmpty()) {
                baseKeyboard.withBoardPaging()
            } else {
                baseKeyboard
            }
        }

    fun boardEvent(action: KeyAction) {
        when (action) {
            KeyAction.CycleBoard -> {
                val index = boards.indexOfFirst { it.id == boardId }
                boardId = boards.getOrNull(index + 1)?.id
                mode = KeyboardMode.MAIN
                capsLock = false
                lastAction.value = null
                Toast.makeText(ctx, boards.firstOrNull { it.id == boardId }?.name ?: "ABC", Toast.LENGTH_SHORT).show()
            }

            KeyAction.BoardHome -> {
                boardId = null
                mode = KeyboardMode.MAIN
                capsLock = false
                lastAction.value = null
            }

            is KeyAction.CommitText -> {
                if (action.text.isNotEmpty()) onTyped()
                refreshEmojiSuggestions()
                if (activeBoard?.returnAfterInput == true) {
                    boardId = null
                    mode = KeyboardMode.MAIN
                    capsLock = false
                }
            }

            else -> {
                if (action is KeyAction.DeleteKeyAction || action is KeyAction.DeleteViaTextManipulation) refreshEmojiSuggestions()
            }
        }
    }

    val position = KeyboardPosition.entries[settings?.position ?: DEFAULT_POSITION]
    val positionPadding = settings?.positionPadding ?: DEFAULT_POSITION_PADDING
    val pushupSizeDp = (settings?.pushupSize ?: DEFAULT_PUSHUP_SIZE).dp
    val ignoreBottomPadding = (settings?.ignoreBottomPadding ?: DEFAULT_IGNORE_BOTTOM_PADDING).toBool()
    val autoCapitalize = activeBoard == null && (settings?.autoCapitalize ?: DEFAULT_AUTO_CAPITALIZE).toBool()
    val spacebarMultiTaps = activeBoard == null && (settings?.spacebarMultiTaps ?: DEFAULT_SPACEBAR_MULTITAPS).toBool()
    val switchToLettersAfterSpace = (settings?.switchToLettersAfterSpace ?: DEFAULT_SWITCH_TO_LETTERS_AFTER_SPACE).toBool()
    val slideEnabled = (settings?.slideEnabled ?: DEFAULT_SLIDE_ENABLED).toBool()
    val slideCursorMovementMode = settings?.slideCursorMovementMode ?: DEFAULT_SLIDE_CURSOR_MOVEMENT_MODE
    val slideSpacebarDeadzoneEnabled = (settings?.slideSpacebarDeadzoneEnabled ?: DEFAULT_SLIDE_SPACEBAR_DEADZONE_ENABLED).toBool()
    val slideBackspaceDeadzoneEnabled = (settings?.slideBackspaceDeadzoneEnabled ?: DEFAULT_SLIDE_BACKSPACE_DEADZONE_ENABLED).toBool()
    val keyBorderWidth = settings?.keyBorderWidth ?: DEFAULT_KEY_BORDER_WIDTH
    val feedbackMode = com.dessalines.thumbkey.effects.rememberFeedbackMode()
    val vibrateOnTap = activeBoard?.haptics != false && feedbackMode != com.dessalines.thumbkey.effects.FeedbackMode.SILENT &&
        (feedbackMode == com.dessalines.thumbkey.effects.FeedbackMode.VIBRATE || (settings?.vibrateOnTap ?: DEFAULT_VIBRATE_ON_TAP).toBool())
    val vibrateOnSlide = activeBoard?.haptics != false && (settings?.vibrateOnSlide ?: DEFAULT_VIBRATE_ON_SLIDE).toBool()
    val soundOnTap = (settings?.soundOnTap ?: DEFAULT_SOUND_ON_TAP).toBool()
    val hideLetters = (settings?.hideLetters ?: DEFAULT_HIDE_LETTERS).toBool()
    val hideSymbols = (settings?.hideSymbols ?: DEFAULT_HIDE_SYMBOLS).toBool()
    val keywiEnabled = KeywiAppearancePreferences.load(ctx)
    val backdropEnabled =
        keywiEnabled && (settings?.backdropEnabled ?: DEFAULT_BACKDROP_ENABLED).toBool()
    val backdropGradient = BackdropThemePreferences.load(ctx).toBackdrop()
    val backdropPadding = 6.dp
    val keyPadding = settings?.keyPadding ?: DEFAULT_KEY_PADDING
    val autoSizeKeys = (settings?.autoSizeKeys ?: DEFAULT_AUTO_SIZE_KEYS).toBool()
    val nonSquareKeys = (settings?.nonSquareKeys ?: DEFAULT_NON_SQUARE_KEYS).toBool()
    val legendWidth =
        if (autoSizeKeys) {
            val keyboardLayout = settings?.keyboardLayout ?: DEFAULT_KEYBOARD_LAYOUT
            getAutoKeyWidth(keyboardLayout, keyPadding, position, ctx)
        } else {
            settings?.keyWidth ?: DEFAULT_KEY_WIDTH
        }
    val legendHeight = if (!nonSquareKeys) legendWidth else settings?.keyHeight ?: DEFAULT_KEY_HEIGHT
    val keyRadius = settings?.keyRadius ?: DEFAULT_KEY_RADIUS
    val dragReturnEnabled = (settings?.dragReturnEnabled ?: DEFAULT_DRAG_RETURN_ENABLED).toBool()
    val circularDragEnabled = (settings?.circularDragEnabled ?: DEFAULT_CIRCULAR_DRAG_ENABLED).toBool()
    val clockwiseDragAction = CircularDragAction.entries[settings?.clockwiseDragAction ?: DEFAULT_CLOCKWISE_DRAG_ACTION]
    val counterclockwiseDragAction =
        CircularDragAction.entries[
            settings?.counterclockwiseDragAction ?: DEFAULT_COUNTERCLOCKWISE_DRAG_ACTION,
        ]
    val ghostKeysEnabled = (settings?.ghostKeysEnabled ?: DEFAULT_GHOST_KEYS_ENABLED).toBool()
    val slideHoldEnabled = (settings?.slideHoldEnabled ?: DEFAULT_SLIDE_HOLD_ENABLED).toBool()

    val keyBorderWidthFloat = keyBorderWidth / 10.0f
    val keyBorderColour = MaterialTheme.colorScheme.outline
    val keyHeight = legendHeight.toFloat()
    val keyWidth = legendWidth.toFloat()
    val cornerRadius = (keyRadius / 100.0f) * ((keyWidth + keyHeight) / 4.0f)

    if (mode == KeyboardMode.EMOJI) {
        val rowCount = keyboardDefinition.modes.main.arr.size
        val controllerKeys =
            if (rowCount <=
                3
            ) {
                listOf(EMOJI_BACK_KEY_ITEM, BACKSPACE_KEY_ITEM, RETURN_KEY_ITEM)
            } else {
                listOf(EMOJI_BACK_KEY_ITEM, NUMERIC_KEY_ITEM, BACKSPACE_KEY_ITEM, RETURN_KEY_ITEM)
            }
        val keyboardHeight = Dp((keyHeight * controllerKeys.size) - (keyPadding * 2))
        ctx.currentInputConnection.requestCursorUpdates(0)
        Box(
            modifier = Modifier.then(if (backdropEnabled) Modifier.keyboardGradientBackground(backdropGradient) else Modifier),
        ) {
            if (backdropEnabled) {
                Box(
                    modifier =
                        Modifier
                            .align(
                                Alignment.TopCenter,
                            ).fillMaxWidth()
                            .height(1.dp)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),
                )
            }
            val emojiModeLayout = keyboardDefinition.modes.emoji
            if (emojiModeLayout != null) {
                Column(
                    modifier =
                        Modifier
                            .then(
                                if (!ignoreBottomPadding) Modifier.safeDrawingPadding() else Modifier,
                            ).padding(
                                bottom = pushupSizeDp,
                            ).fillMaxWidth()
                            .then(if (backdropEnabled) Modifier.padding(top = backdropPadding) else Modifier),
                ) {
                    val emojiPickerHeight = Dp((keyHeight * (emojiModeLayout.arr.size - 1)) - (keyPadding * 2))
                    Box(
                        modifier =
                            Modifier
                                .padding(keyPadding.dp)
                                .clip(RoundedCornerShape(cornerRadius.dp))
                                .then(
                                    if (keyBorderWidthFloat >
                                        0.0
                                    ) {
                                        Modifier.border(
                                            keyBorderWidthFloat.dp,
                                            keyBorderColour,
                                            shape = RoundedCornerShape(cornerRadius.dp),
                                        )
                                    } else {
                                        Modifier
                                    },
                                ).background(MaterialTheme.colorScheme.surface),
                    ) {
                        val view = LocalView.current

                        AndroidView(
                            factory = { context ->
                                EmojiPickerView(context).also { picker ->
                                    picker.setOnEmojiPickedListener {
                                        if (vibrateOnTap && com.dessalines.thumbkey.effects.FeedbackPreferences.hapticsAllowed(ctx)) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        com.dessalines.thumbkey.effects.ActionSoundEngine.fire(ctx)
                                        ctx.currentInputConnection.commitText(it.emoji, 1)
                                        onTyped()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(emojiPickerHeight),
                        )
                    }
                    Row {
                        val bottomRow = emojiModeLayout.arr.last()
                        bottomRow.forEachIndexed { effectCol, key ->
                            Column {
                                KeyboardKey(
                                    key = key,
                                    effectKeyId = "key${emojiModeLayout.arr.lastIndex}_$effectCol",
                                    lastAction = lastAction,
                                    legendHeight = legendHeight,
                                    legendWidth = legendWidth,
                                    keyHeight = keyHeight,
                                    keyWidth = keyWidth,
                                    keyPadding = keyPadding,
                                    keyBorderWidth = keyBorderWidthFloat,
                                    keyRadius = cornerRadius,
                                    autoCapitalize = autoCapitalize,
                                    keyboardSettings = keyboardDefinition.settings,
                                    spacebarMultiTaps = spacebarMultiTaps,
                                    vibrateOnTap = vibrateOnTap,
                                    vibrateOnSlide = vibrateOnSlide,
                                    soundOnTap = soundOnTap,
                                    hideLetters = hideLetters,
                                    hideSymbols = hideSymbols,
                                    hideSwipeHints = activeBoard?.showHints == false,
                                    capsLock = capsLock,
                                    animationSpeed = settings?.animationSpeed ?: DEFAULT_ANIMATION_SPEED,
                                    animationHelperSpeed = settings?.animationHelperSpeed ?: DEFAULT_ANIMATION_HELPER_SPEED,
                                    minSwipeLength = settings?.minSwipeLength ?: DEFAULT_MIN_SWIPE_LENGTH,
                                    slideSensitivity = settings?.slideSensitivity ?: DEFAULT_SLIDE_SENSITIVITY,
                                    slideEnabled = slideEnabled,
                                    slideCursorMovementMode = slideCursorMovementMode,
                                    slideSpacebarDeadzoneEnabled = slideSpacebarDeadzoneEnabled,
                                    slideBackspaceDeadzoneEnabled = slideBackspaceDeadzoneEnabled,
                                    onToggleShiftMode = { enable ->
                                        mode =
                                            if (enable) {
                                                KeyboardMode.SHIFTED
                                            } else {
                                                capsLock = false
                                                KeyboardMode.MAIN
                                            }
                                    },
                                    onToggleCtrlMode = { enable -> mode = if (enable) KeyboardMode.CTRLED else KeyboardMode.MAIN },
                                    onToggleAltMode = { enable -> mode = if (enable) KeyboardMode.ALTED else KeyboardMode.MAIN },
                                    onToggleNumericMode = { enable ->
                                        mode =
                                            if (enable) {
                                                KeyboardMode.NUMERIC
                                            } else {
                                                capsLock = false
                                                KeyboardMode.MAIN
                                            }
                                    },
                                    onToggleEmojiMode = { enable -> mode = if (enable) KeyboardMode.EMOJI else KeyboardMode.KAOMOJI },
                                    onToggleClipboardMode = { enable -> mode = if (enable) KeyboardMode.CLIPBOARD else KeyboardMode.MAIN },
                                    onToggleCapsLock = {
                                        capsLock = !capsLock
                                        if (capsLock) mode = KeyboardMode.SHIFTED
                                    },
                                    onToggleHideLetters = onToggleHideLetters,
                                    onAutoCapitalize = { enable ->
                                        if (activeBoard == null && mode !==
                                            KeyboardMode.NUMERIC
                                        ) {
                                            mode =
                                                if (enable) {
                                                    KeyboardMode.SHIFTED
                                                } else if (!capsLock) {
                                                    KeyboardMode.MAIN
                                                } else {
                                                    mode
                                                }
                                        }
                                    },
                                    onSwitchLanguage = {
                                        onSwitchLanguage()
                                        mode = KeyboardMode.MAIN
                                    },
                                    onChangePosition = onChangePosition,
                                    onKeyEvent = { action ->
                                        boardEvent(action)
                                        when (mode) {
                                            KeyboardMode.CTRLED, KeyboardMode.ALTED -> {
                                                if (action is KeyAction.SendEvent) {
                                                    mode =
                                                        KeyboardMode.MAIN
                                                }
                                            }

                                            KeyboardMode.NUMERIC -> {
                                                if (activeBoard == null &&
                                                    switchToLettersAfterSpace &&
                                                    action is KeyAction.CommitText &&
                                                    action.text == " "
                                                ) {
                                                    capsLock = false
                                                    mode = KeyboardMode.MAIN
                                                }
                                            }

                                            else -> {}
                                        }
                                    },
                                    dragReturnEnabled = dragReturnEnabled,
                                    circularDragEnabled = circularDragEnabled,
                                    clockwiseDragAction = clockwiseDragAction,
                                    counterclockwiseDragAction = counterclockwiseDragAction,
                                    slideHoldEnabled = slideHoldEnabled,
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier =
                        Modifier
                            .then(
                                if (!ignoreBottomPadding) Modifier.safeDrawingPadding() else Modifier,
                            ).padding(
                                bottom = pushupSizeDp,
                            ).fillMaxWidth()
                            .then(if (backdropEnabled) Modifier.padding(top = backdropPadding) else Modifier),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(keyPadding.dp)
                                .clip(RoundedCornerShape(cornerRadius.dp))
                                .then(
                                    if (keyBorderWidthFloat >
                                        0.0
                                    ) {
                                        Modifier.border(
                                            keyBorderWidthFloat.dp,
                                            keyBorderColour,
                                            shape = RoundedCornerShape(cornerRadius.dp),
                                        )
                                    } else {
                                        Modifier
                                    },
                                ).background(MaterialTheme.colorScheme.surface),
                    ) {
                        val view = LocalView.current

                        AndroidView(
                            factory = { context ->
                                EmojiPickerView(context).also { picker ->
                                    picker.setOnEmojiPickedListener {
                                        if (vibrateOnTap && com.dessalines.thumbkey.effects.FeedbackPreferences.hapticsAllowed(ctx)) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        com.dessalines.thumbkey.effects.ActionSoundEngine.fire(ctx)
                                        ctx.currentInputConnection.commitText(it.emoji, 1)
                                        onTyped()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(keyboardHeight),
                        )
                    }
                    Column {
                        controllerKeys.forEachIndexed { effectRow, key ->
                            Column {
                                KeyboardKey(
                                    key = key,
                                    effectKeyId = "emoji_control_$effectRow",
                                    lastAction = lastAction,
                                    legendHeight = legendHeight,
                                    legendWidth = legendWidth,
                                    keyHeight = keyHeight,
                                    keyWidth = keyWidth,
                                    keyPadding = keyPadding,
                                    keyBorderWidth = keyBorderWidthFloat,
                                    keyRadius = cornerRadius,
                                    autoCapitalize = autoCapitalize,
                                    keyboardSettings = keyboardDefinition.settings,
                                    spacebarMultiTaps = spacebarMultiTaps,
                                    vibrateOnTap = vibrateOnTap,
                                    vibrateOnSlide = vibrateOnSlide,
                                    soundOnTap = soundOnTap,
                                    hideLetters = hideLetters,
                                    hideSymbols = hideSymbols,
                                    hideSwipeHints = activeBoard?.showHints == false,
                                    capsLock = capsLock,
                                    animationSpeed = settings?.animationSpeed ?: DEFAULT_ANIMATION_SPEED,
                                    animationHelperSpeed = settings?.animationHelperSpeed ?: DEFAULT_ANIMATION_HELPER_SPEED,
                                    minSwipeLength = settings?.minSwipeLength ?: DEFAULT_MIN_SWIPE_LENGTH,
                                    slideSensitivity = settings?.slideSensitivity ?: DEFAULT_SLIDE_SENSITIVITY,
                                    slideEnabled = slideEnabled,
                                    slideCursorMovementMode = slideCursorMovementMode,
                                    slideSpacebarDeadzoneEnabled = slideSpacebarDeadzoneEnabled,
                                    slideBackspaceDeadzoneEnabled = slideBackspaceDeadzoneEnabled,
                                    onToggleShiftMode = { enable ->
                                        mode =
                                            if (enable) {
                                                KeyboardMode.SHIFTED
                                            } else {
                                                capsLock = false
                                                KeyboardMode.MAIN
                                            }
                                    },
                                    onToggleCtrlMode = { enable -> mode = if (enable) KeyboardMode.CTRLED else KeyboardMode.MAIN },
                                    onToggleAltMode = { enable -> mode = if (enable) KeyboardMode.ALTED else KeyboardMode.MAIN },
                                    onToggleNumericMode = { enable ->
                                        mode =
                                            if (enable) {
                                                KeyboardMode.NUMERIC
                                            } else {
                                                capsLock = false
                                                KeyboardMode.MAIN
                                            }
                                    },
                                    onToggleEmojiMode = { enable -> mode = if (enable) KeyboardMode.EMOJI else KeyboardMode.KAOMOJI },
                                    onToggleClipboardMode = { enable -> mode = if (enable) KeyboardMode.CLIPBOARD else KeyboardMode.MAIN },
                                    onToggleCapsLock = {
                                        capsLock = !capsLock
                                        if (capsLock) mode = KeyboardMode.SHIFTED
                                    },
                                    onToggleHideLetters = onToggleHideLetters,
                                    onAutoCapitalize = { enable ->
                                        if (activeBoard == null && mode !==
                                            KeyboardMode.NUMERIC
                                        ) {
                                            mode =
                                                if (enable) {
                                                    KeyboardMode.SHIFTED
                                                } else if (!capsLock) {
                                                    KeyboardMode.MAIN
                                                } else {
                                                    mode
                                                }
                                        }
                                    },
                                    onSwitchLanguage = {
                                        onSwitchLanguage()
                                        mode = KeyboardMode.MAIN
                                    },
                                    onChangePosition = onChangePosition,
                                    onKeyEvent = { action ->
                                        boardEvent(action)
                                        when (mode) {
                                            KeyboardMode.CTRLED, KeyboardMode.ALTED -> {
                                                if (action is KeyAction.SendEvent) {
                                                    mode =
                                                        KeyboardMode.MAIN
                                                }
                                            }

                                            KeyboardMode.NUMERIC -> {
                                                if (activeBoard == null &&
                                                    switchToLettersAfterSpace &&
                                                    action is KeyAction.CommitText &&
                                                    action.text == " "
                                                ) {
                                                    capsLock = false
                                                    mode = KeyboardMode.MAIN
                                                }
                                            }

                                            else -> {}
                                        }
                                    },
                                    dragReturnEnabled = dragReturnEnabled,
                                    circularDragEnabled = circularDragEnabled,
                                    clockwiseDragAction = clockwiseDragAction,
                                    counterclockwiseDragAction = counterclockwiseDragAction,
                                    slideHoldEnabled = slideHoldEnabled,
                                )
                            }
                        }
                    }
                }
            }
        }
    } else if (mode == KeyboardMode.KAOMOJI) {
        val rowCount = keyboardDefinition.modes.main.arr.size
        val keyboardHeight = Dp(keyHeight * rowCount)
        val view = LocalView.current

        ctx.currentInputConnection.requestCursorUpdates(0)
        Box(
            modifier = Modifier.then(if (backdropEnabled) Modifier.keyboardGradientBackground(backdropGradient) else Modifier),
        ) {
            if (backdropEnabled) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),
                )
            }
            KaomojiRoom(
                onCommit = { text ->
                    if (vibrateOnTap && com.dessalines.thumbkey.effects.FeedbackPreferences.hapticsAllowed(ctx)) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    com.dessalines.thumbkey.effects.ActionSoundEngine.fire(ctx)
                    ctx.currentInputConnection.commitText(text, 1)
                },
                onBackToLetters = {
                    capsLock = false
                    mode = KeyboardMode.MAIN
                },
                onGoToEmoji = { mode = KeyboardMode.EMOJI },
                modifier =
                    Modifier
                        .then(if (!ignoreBottomPadding) Modifier.safeDrawingPadding() else Modifier)
                        .padding(bottom = pushupSizeDp)
                        .fillMaxWidth()
                        .height(keyboardHeight)
                        .then(if (backdropEnabled) Modifier.padding(top = backdropPadding) else Modifier),
            )
        }
    } else if (mode == KeyboardMode.CLIPBOARD) {
        val scope = CoroutineScope(Dispatchers.IO)
        val clipboardHistoryEnabled = (settings?.clipboardHistoryEnabled ?: DEFAULT_CLIPBOARD_HISTORY_ENABLED).toBool()
        val rowCount = keyboardDefinition.modes.main.arr.size
        val keyboardHeight = Dp(keyHeight * rowCount)
        LaunchedEffect(Unit) {
            if (clipboardHistoryEnabled) clipboardRepository.clearExpired() else clipboardRepository.clearAll()
        }
        Box(modifier = Modifier.then(if (backdropEnabled) Modifier.keyboardGradientBackground(backdropGradient) else Modifier)) {
            if (backdropEnabled) {
                Box(
                    modifier =
                        Modifier
                            .align(
                                Alignment.TopCenter,
                            ).fillMaxWidth()
                            .height(1.dp)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),
                )
            }
            Box(
                modifier =
                    Modifier
                        .then(
                            if (!ignoreBottomPadding) Modifier.safeDrawingPadding() else Modifier,
                        ).padding(
                            bottom = pushupSizeDp,
                        ).fillMaxWidth()
                        .height(keyboardHeight)
                        .then(if (backdropEnabled) Modifier.padding(top = backdropPadding) else Modifier),
            ) {
                ClipboardHistoryScreen(
                    clipboardItems = clipboardItems,
                    isEnabled = clipboardHistoryEnabled,
                    onItemClick = { item ->
                        ctx.currentInputConnection.commitText(item.text, 1)
                        mode = KeyboardMode.MAIN
                    },
                    onItemPaste = { item -> ctx.currentInputConnection.commitText(item.text, 1) },
                    onItemDelete = { item -> scope.launch { clipboardRepository.deleteItem(item) } },
                    onItemTogglePin = { item -> scope.launch { clipboardRepository.togglePin(item) } },
                    onBack = { mode = KeyboardMode.MAIN },
                    onClearAll = { scope.launch { clipboardRepository.clearUnpinned() } },
                    onGoToClipboardSettings = onGoToClipboardSettings,
                    keyHeight = keyHeight,
                    keyPadding = keyPadding,
                    cornerRadius = cornerRadius,
                    vibrateOnTap = vibrateOnTap,
                )
            }
        }
    } else {
        LaunchedEffect(ctx) {
            if (ctx.currentInputConnection.requestCursorUpdates(CURSOR_UPDATE_MONITOR)) {
                Log.d(TAG, "request for cursor updates succeeded, cursor updates will be provided")
            } else {
                Log.d(TAG, "request for cursor updates failed, cursor updates will not be provided")
            }
        }

        val gradientCanvasWidth =
            keyboard.arr.maxOfOrNull { row -> row.sumOf { key -> (key.widthMultiplier * keyWidth).toDouble() }.toFloat() } ?: keyWidth
        val gradientCanvasHeight = keyboard.arr.size * keyHeight
        val keyRectangles =
            keyCanvasRects(
                keyboard.arr.map { row -> row.map { it.widthMultiplier * keyWidth } },
                List(keyboard.arr.size) { keyHeight },
                padding = keyPadding.toFloat(),
            )
        val keyTheme = KeyThemePreferences.current
        val keyGradient = if (keywiEnabled && keyTheme.surfaceStyle == KeySurfaceStyle.GRADIENT) keyTheme.surfaceGradient else null

        val drawKeyboard = @Composable { alignment: Alignment, drawBackdrop: Boolean, positionPaddingValue: Int ->
            val modifierPositionPadding =
                if (positionPaddingValue >
                    0
                ) {
                    Modifier.padding(start = positionPaddingValue.dp)
                } else {
                    Modifier.padding(end = -positionPaddingValue.dp)
                }
            Box(
                contentAlignment = alignment,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .then(
                            if (drawBackdrop) Modifier.keyboardGradientBackground(backdropGradient) else Modifier,
                        ).then(
                            if (!ignoreBottomPadding) Modifier.safeDrawingPadding() else Modifier,
                        ).padding(bottom = pushupSizeDp)
                        .then(if (backdropEnabled) Modifier.padding(top = backdropPadding) else Modifier),
            ) {
                if (drawBackdrop) {
                    Box(
                        modifier =
                            Modifier
                                .align(
                                    Alignment.TopCenter,
                                ).fillMaxWidth()
                                .height(1.dp)
                                .background(color = MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
                Column(modifier = modifierPositionPadding) {
                    if (emojiSuggestions != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            emojiSuggestions?.matches?.take(5)?.forEach { match ->
                                TextButton(onClick = { chooseEmoji(match.emoji) }) { Text(match.emoji) }
                            }
                        }
                    }
                    keyboard.arr.forEachIndexed { i, row ->
                        Row {
                            row.forEachIndexed { j, key ->
                                val gradientOffsetX =
                                    row
                                        .take(j)
                                        .sumOf { previousKey ->
                                            (previousKey.widthMultiplier * keyWidth).toDouble()
                                        }.toFloat()
                                val gradientOffsetY = i * keyHeight
                                Column {
                                    val ghostKey =
                                        if (ghostKeysEnabled && activeBoard == null) {
                                            when (mode) {
                                                KeyboardMode.MAIN, KeyboardMode.SHIFTED, KeyboardMode.CTRLED, KeyboardMode.ALTED -> {
                                                    keyboardDefinition.modes.numeric
                                                }

                                                else -> {
                                                    null
                                                }
                                            }?.arr?.getOrNull(i)?.getOrNull(j)
                                        } else {
                                            null
                                        }
                                    KeyboardKey(
                                        key = key,
                                        effectKeyId = "key${i}_$j",
                                        ghostKey = ghostKey,
                                        lastAction = lastAction,
                                        legendHeight = legendHeight,
                                        legendWidth = legendWidth,
                                        keyHeight = keyHeight,
                                        keyWidth = keyWidth,
                                        keyPadding = keyPadding,
                                        keyBorderWidth = keyBorderWidthFloat,
                                        keyRadius = cornerRadius,
                                        autoCapitalize = autoCapitalize,
                                        keyboardSettings = keyboardDefinition.settings,
                                        spacebarMultiTaps = spacebarMultiTaps,
                                        vibrateOnTap = vibrateOnTap,
                                        vibrateOnSlide = vibrateOnSlide,
                                        soundOnTap = soundOnTap,
                                        hideLetters = hideLetters,
                                        hideSymbols = hideSymbols,
                                        hideSwipeHints = activeBoard?.showHints == false,
                                        capsLock = capsLock,
                                        animationSpeed = settings?.animationSpeed ?: DEFAULT_ANIMATION_SPEED,
                                        animationHelperSpeed = settings?.animationHelperSpeed ?: DEFAULT_ANIMATION_HELPER_SPEED,
                                        minSwipeLength = settings?.minSwipeLength ?: DEFAULT_MIN_SWIPE_LENGTH,
                                        slideSensitivity = settings?.slideSensitivity ?: DEFAULT_SLIDE_SENSITIVITY,
                                        slideEnabled = slideEnabled,
                                        slideCursorMovementMode = slideCursorMovementMode,
                                        slideSpacebarDeadzoneEnabled = slideSpacebarDeadzoneEnabled,
                                        slideBackspaceDeadzoneEnabled = slideBackspaceDeadzoneEnabled,
                                        onToggleShiftMode = { enable ->
                                            mode =
                                                if (enable) {
                                                    KeyboardMode.SHIFTED
                                                } else {
                                                    capsLock = false
                                                    KeyboardMode.MAIN
                                                }
                                        },
                                        onToggleCtrlMode = { enable -> mode = if (enable) KeyboardMode.CTRLED else KeyboardMode.MAIN },
                                        onToggleAltMode = { enable -> mode = if (enable) KeyboardMode.ALTED else KeyboardMode.MAIN },
                                        onToggleNumericMode = { enable ->
                                            mode =
                                                if (enable) {
                                                    KeyboardMode.NUMERIC
                                                } else {
                                                    capsLock = false
                                                    KeyboardMode.MAIN
                                                }
                                        },
                                        onToggleEmojiMode = { enable -> mode = if (enable) KeyboardMode.EMOJI else KeyboardMode.KAOMOJI },
                                        onToggleClipboardMode = { enable ->
                                            mode = if (enable) KeyboardMode.CLIPBOARD else KeyboardMode.MAIN
                                        },
                                        onToggleCapsLock = {
                                            capsLock = !capsLock
                                            if (capsLock) mode = KeyboardMode.SHIFTED
                                        },
                                        onToggleHideLetters = onToggleHideLetters,
                                        onKeyEvent = { action ->
                                            boardEvent(action)
                                            when (mode) {
                                                KeyboardMode.CTRLED, KeyboardMode.ALTED -> {
                                                    if (action is KeyAction.SendEvent) {
                                                        mode =
                                                            KeyboardMode.MAIN
                                                    }
                                                }

                                                KeyboardMode.NUMERIC -> {
                                                    if (activeBoard == null &&
                                                        switchToLettersAfterSpace &&
                                                        action is KeyAction.CommitText &&
                                                        action.text == " "
                                                    ) {
                                                        capsLock = false
                                                        mode = KeyboardMode.MAIN
                                                    }
                                                }

                                                else -> {}
                                            }
                                        },
                                        onAutoCapitalize = { enable ->
                                            if (activeBoard == null && mode !==
                                                KeyboardMode.NUMERIC
                                            ) {
                                                mode =
                                                    if (enable) {
                                                        KeyboardMode.SHIFTED
                                                    } else if (!capsLock) {
                                                        KeyboardMode.MAIN
                                                    } else {
                                                        mode
                                                    }
                                            }
                                        },
                                        onSwitchLanguage = {
                                            onSwitchLanguage()
                                            mode = KeyboardMode.MAIN
                                        },
                                        onChangePosition = onChangePosition,
                                        oppositeCaseKey =
                                            when (mode) {
                                                KeyboardMode.MAIN -> keyboardDefinition.modes.shifted
                                                KeyboardMode.SHIFTED -> keyboardDefinition.modes.main
                                                else -> null
                                            }?.arr?.getOrNull(i)?.getOrNull(j),
                                        numericKey =
                                            when (mode) {
                                                KeyboardMode.MAIN, KeyboardMode.SHIFTED, KeyboardMode.CTRLED, KeyboardMode.ALTED -> {
                                                    keyboardDefinition.modes.numeric.arr
                                                        .getOrNull(
                                                            i,
                                                        )?.getOrNull(j)
                                                }

                                                else -> {
                                                    null
                                                }
                                            },
                                        dragReturnEnabled = dragReturnEnabled,
                                        circularDragEnabled = circularDragEnabled,
                                        clockwiseDragAction = clockwiseDragAction,
                                        counterclockwiseDragAction = counterclockwiseDragAction,
                                        slideHoldEnabled = slideHoldEnabled,
                                        keyGradient = keyGradient,
                                        keyGradientCanvasWidth = gradientCanvasWidth,
                                        keyGradientCanvasHeight = gradientCanvasHeight,
                                        keyDesignId = "$i:$j",
                                        keyRectangles = keyRectangles,
                                        keySequenceIndex = keyboard.arr.take(i).sumOf { it.size } + j,
                                        keyGradientOffsetX = gradientOffsetX,
                                        keyGradientOffsetY = gradientOffsetY,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        drawKeyboard(keyboardPositionToAlignment(position), backdropEnabled, positionPadding)
        if (position == KeyboardPosition.Dual) {
            drawKeyboard(keyboardPositionToAlignment(KeyboardPosition.Right), false, positionPadding)
        }
    }
}
