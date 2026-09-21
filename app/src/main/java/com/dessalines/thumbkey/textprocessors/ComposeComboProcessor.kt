package com.dessalines.thumbkey.textprocessors

import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import com.dessalines.thumbkey.IMEService
import com.dessalines.thumbkey.utils.TAG

/** Shown underlined while a sequence is being typed, so the armed state is visible. */
private const val COMPOSE_HINT = "♫"

/**
 * Implements a desktop-style compose key: press the compose key, then type a short sequence
 * which is replaced by a single character.
 *
 * The pending sequence is held in Android's composing-text region. Unknown sequences are
 * committed literally rather than swallowed.
 */
class ComposeComboProcessor : TextProcessor {
    private var composing = false
    private val buffer = StringBuilder()
    private var display = ""
    private var base = 0

    override fun handleComposeStart(ime: IMEService) {
        val ic = ime.currentInputConnection ?: return
        if (composing) {
            flushLiterally(ic)
            return
        }
        syncBase(ime)
        composing = true
        buffer.clear()
        show(ic, COMPOSE_HINT)
        Log.d(TAG, "ComposeCombo: armed at $base")
    }

    override fun handleCommitText(ime: IMEService, input: CharSequence) {
        val ic = ime.currentInputConnection ?: return
        if (!composing) {
            ic.commitText(input, 1)
            base += input.length
            return
        }
        buffer.append(input)
        val seq = buffer.toString()
        val match = ComposeComboTable.match(seq)
        if (match != null) {
            show(ic, match)
            ic.finishComposingText()
            base += match.length
            reset()
            return
        }
        if (ComposeComboTable.isPrefix(seq)) {
            show(ic, seq)
            return
        }
        flushLiterally(ic)
    }

    override fun handleKeyEvent(ime: IMEService, ev: KeyEvent) {
        val ic = ime.currentInputConnection ?: return
        if (!composing) {
            ic.sendKeyEvent(ev)
            return
        }
        if (ev.keyCode == KeyEvent.KEYCODE_DEL) {
            if (buffer.isNotEmpty()) {
                buffer.deleteCharAt(buffer.length - 1)
                show(ic, buffer.ifEmpty { COMPOSE_HINT }.toString())
            } else {
                cancel(ic)
            }
            return
        }
        flushLiterally(ic)
        ic.sendKeyEvent(ev)
    }

    override fun handleFinishInput(ime: IMEService) {
        if (composing) flushLiterally(ime.currentInputConnection)
    }

    override fun handleCursorUpdate(
        ime: IMEService,
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
    ) {
        if (!composing) {
            base = newSelStart
            return
        }
        if (newSelStart == base + display.length && newSelStart == newSelEnd) return
        cancel(ime.currentInputConnection)
        base = newSelStart
    }

    override fun updateCursorPosition(ime: IMEService) = syncBase(ime)

    private fun syncBase(ime: IMEService) {
        val extracted = ime.currentInputConnection?.getExtractedText(ExtractedTextRequest(), 0) ?: return
        base = extracted.selectionStart
    }

    private fun show(ic: InputConnection, text: String) {
        display = text
        ic.setComposingText(text, 1)
    }

    private fun flushLiterally(ic: InputConnection?) {
        val typed = buffer.toString()
        ic?.let {
            it.setComposingText(typed, 1)
            it.finishComposingText()
        }
        base += typed.length
        reset()
    }

    private fun cancel(ic: InputConnection?) {
        ic?.let {
            it.setComposingText("", 1)
            it.finishComposingText()
        }
        reset()
    }

    private fun reset() {
        composing = false
        buffer.clear()
        display = ""
    }
}
