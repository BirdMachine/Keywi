package com.dessalines.thumbkey.effects

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

enum class NoteScale(val title: String, val intervals: List<Int>) {
    MAJOR("Major", listOf(0, 2, 4, 5, 7, 9, 11)),
    MINOR("Natural minor", listOf(0, 2, 3, 5, 7, 8, 10)),
}

val noteRoots = listOf("C", "C♯ / D♭", "D", "D♯ / E♭", "E", "F", "F♯ / G♭", "G", "G♯ / A♭", "A", "A♯ / B♭", "B")

/** Original soft instrument: warm sine partials, a rounded onset and a click-free short release. */
object ScaleToneSynth {
    const val SAMPLE_RATE = 22050
    const val DRY_DURATION_MS = 170
    const val DURATION_MS = 310

    fun notes(root: Int, scale: NoteScale): List<Int> = scale.intervals.map { 60 + Math.floorMod(root + it, 12) }

    fun frequency(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)

    fun wave(midi: Int): ByteArray {
        require(midi in 60..71)
        val count = SAMPLE_RATE * DURATION_MS / 1000
        val bytes = ByteBuffer.allocate(44 + count * 2).order(ByteOrder.LITTLE_ENDIAN)
        bytes.put("RIFF".toByteArray()).putInt(36 + count * 2).put("WAVEfmt ".toByteArray())
            .putInt(16).putShort(1).putShort(1).putInt(SAMPLE_RATE).putInt(SAMPLE_RATE * 2)
            .putShort(2).putShort(16).put("data".toByteArray()).putInt(count * 2)
        val dryCount = SAMPLE_RATE * DRY_DURATION_MS / 1000
        val dry = DoubleArray(dryCount)
        for (i in dry.indices) {
            val t = i.toDouble() / SAMPLE_RATE
            val attack = .5 - .5 * cos(PI * (t / .012).coerceAtMost(1.0))
            val release = .5 - .5 * cos(PI * ((dryCount - 1 - i).toDouble() / (SAMPLE_RATE * .035)).coerceIn(0.0, 1.0))
            val phase = 2 * PI * frequency(midi) * t
            val warm = sin(phase) + .10 * sin(phase * 2) + .025 * sin(phase * 3)
            dry[i] = warm * attack * release * exp(-t * 13) * 4800
        }
        // Quiet, irregular early reflections: a small room, not a distinct echo.
        val delays = intArrayOf(19, 31, 43, 59, 79, 107).map { SAMPLE_RATE * it / 1000 }
        val gains = doubleArrayOf(.045, -.035, .028, .020, -.014, .009)
        for (i in 0 until count) {
            var sample = dry.getOrElse(i) { 0.0 }
            delays.forEachIndexed { tap, delay -> sample += dry.getOrElse(i - delay) { 0.0 } * gains[tap] }
            bytes.putShort(sample.toInt().coerceIn(-32768, 32767).toShort())
        }
        return bytes.array()
    }
}
