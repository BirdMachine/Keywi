package com.dessalines.thumbkey

import com.dessalines.thumbkey.effects.NoteScale
import com.dessalines.thumbkey.effects.ScaleToneSynth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

class ScaleToneSynthTest {
    @Test fun everyRootProducesOnlyItsSevenScaleDegrees() {
        for (root in 0..11) for (scale in NoteScale.entries) {
            val notes = ScaleToneSynth.notes(root, scale)
            assertEquals(7, notes.distinct().size)
            assertTrue(notes.all { it in 60..71 })
            assertEquals(scale.intervals.toSet(), notes.map { Math.floorMod(it - root, 12) }.toSet())
        }
        assertEquals(setOf(1, 3, 4, 6, 8, 10, 11), ScaleToneSynth.notes(11, NoteScale.MAJOR).map { it % 12 }.toSet())
        assertEquals(setOf(1, 2, 4, 6, 8, 9, 11), ScaleToneSynth.notes(6, NoteScale.MINOR).map { it % 12 }.toSet())
        assertEquals(440.0, ScaleToneSynth.frequency(69), .001)
    }

    @Test fun pcmIsShortQuietAndClickFreeAtBothEnds() {
        for (note in 60..71) {
            val wave = ScaleToneSynth.wave(note)
            val bytes = ByteBuffer.wrap(wave).order(ByteOrder.LITTLE_ENDIAN)
            assertEquals("RIFF", String(wave.copyOfRange(0, 4)))
            assertEquals(wave.size - 8, bytes.getInt(4))
            assertEquals(ScaleToneSynth.SAMPLE_RATE, bytes.getInt(24))
            assertEquals(0, bytes.getShort(44).toInt())
            assertEquals(0, bytes.getShort(wave.size - 2).toInt())
            val pcm = (44 until wave.size step 2).map { bytes.getShort(it).toInt() }
            assertEquals(ScaleToneSynth.SAMPLE_RATE * ScaleToneSynth.DURATION_MS / 1000, pcm.size)
            val tail = pcm.drop(ScaleToneSynth.SAMPLE_RATE * ScaleToneSynth.DRY_DURATION_MS / 1000)
            assertTrue("Room tail must survive the dry note", tail.any { abs(it) > 2 })
            assertTrue("Reverb must stay subtle", tail.maxOf { abs(it) } < 500)
            assertTrue(pcm.any { abs(it) > 100 })
            assertTrue(pcm.all { abs(it) < 6000 })
            assertTrue(abs(pcm.zipWithNext().maxOf { (a, b) -> abs(a - b) }) < 1000)
        }
        val file = File("build/outputs/tool-page-review/soft-scale-note-A4.wav")
        file.parentFile!!.mkdirs()
        file.writeBytes(ScaleToneSynth.wave(69))
    }
}
