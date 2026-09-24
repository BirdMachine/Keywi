package com.dessalines.thumbkey.ui.components.keyboard

/** Leading-edge throttle: dropped input does not postpone the next allowed trigger. */
class OverlayTriggerGate {
    private var lastAccepted: Long? = null

    fun accept(nowMs: Long, cooldownMs: Int): Boolean {
        val last = lastAccepted
        if (last != null && nowMs >= last && nowMs - last < cooldownMs.coerceAtLeast(0)) return false
        lastAccepted = nowMs
        return true
    }
}
