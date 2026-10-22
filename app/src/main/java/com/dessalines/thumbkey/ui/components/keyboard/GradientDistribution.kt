package com.dessalines.thumbkey.ui.components.keyboard

/** SAME preserves existing themes; SHARED samples a single keyboard-space canvas. */
enum class GradientDistribution { SAME, STABLE_RANDOM, INCREMENTAL, SHARED }

/** Pure, deterministic phase selection: typing and recomposition never reshuffle colors. */
fun gradientPhaseDegrees(
    distribution: GradientDistribution,
    keyId: String,
    sequenceIndex: Int,
    stepDegrees: Float,
    seed: Int,
): Float {
    val degrees =
        when (distribution) {
            GradientDistribution.SAME, GradientDistribution.SHARED -> {
                0.0
            }

            GradientDistribution.INCREMENTAL -> {
                sequenceIndex.toDouble() * stepDegrees.toDouble()
            }

            GradientDistribution.STABLE_RANDOM -> {
                // Mix the persisted seed and stable visual position, independent of key labels/case.
                var hash = keyId.hashCode() xor seed
                hash = (hash xor (hash ushr 16)) * -2048144789
                hash = (hash xor (hash ushr 13)) * -1028477387
                hash = hash xor (hash ushr 16)
                (hash.toLong() and 0xffffffffL).toDouble() / 4294967296.0 * 360.0
            }
        }
    if (!degrees.isFinite()) return 0f
    return (((degrees % 360.0) + 360.0) % 360.0).toFloat()
}
