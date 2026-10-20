package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class KeyGroupGradientCanvasTest {
    private val rectangles =
        keyCanvasRects(
            listOf(listOf(80f, 160f, 80f), listOf(120f, 200f)),
            listOf(60f, 50f),
            columnGap = 3f,
            rowGap = 5f,
            padding = 2f,
        )
    private val group = KeyDesignGroup("Navigation", setOf("0:1", "1:0", "absent"), KeyDesignOverride(), true)

    @Test fun weightedKeysAndPaddingProducePhysicalCoordinates() {
        assertEquals(KeyCanvasRect(85f, 2f, 156f, 56f), rectangles["0:1"])
        assertEquals(KeyCanvasRect(125f, 67f, 196f, 46f), rectangles["1:1"])
    }

    @Test fun boundingCanvasIncludesGapsButIgnoresAbsentLayoutMembers() {
        val theme = KeyThemeState(keyGroups = listOf(group))
        assertEquals(KeyCanvasRect(2f, 2f, 239f, 111f), theme.groupCanvasForKey("0:1", KeyGradientLayer.BORDER, rectangles))
        assertEquals(
            theme.groupCanvasForKey("0:1", KeyGradientLayer.SURFACE, rectangles),
            theme.groupCanvasForKey("1:0", KeyGradientLayer.SURFACE, rectangles),
        )
        assertNull(theme.groupCanvasForKey("0:0", KeyGradientLayer.BORDER, rectangles))
        assertNull(theme.groupCanvasForKey("absent", KeyGradientLayer.BORDER, rectangles))
        assertEquals(KeyCanvasRect(4f, 4f, 478f, 222f), theme.groupCanvasForKey("0:1", KeyGradientLayer.BORDER, rectangles)?.scaled(2f))
    }

    @Test fun perKeyPrecedenceAndOverlappingGroupsResolveEachLayerIndependently() {
        val second = KeyDesignGroup("Border only", setOf("0:1"), KeyDesignOverride(borderColor = Color.Red))
        val theme =
            KeyThemeState(
                keyGroups = listOf(group, second),
                keyOverrides = mapOf("1:0" to KeyDesignOverride(surfaceGradient = BIRDIE_KEY_GRADIENT)),
            )
        assertNull(theme.groupCanvasForKey("0:1", KeyGradientLayer.BORDER, rectangles))
        assertNotNull(theme.groupCanvasForKey("0:1", KeyGradientLayer.SURFACE, rectangles))
        assertNull(theme.groupCanvasForKey("1:0", KeyGradientLayer.SURFACE, rectangles))
        assertNotNull(theme.groupCanvasForKey("1:0", KeyGradientLayer.BORDER, rectangles))
        val replacement = group.copy(id = "Newest", keyIds = setOf("0:1", "0:2"))
        assertEquals(
            KeyCanvasRect(85f, 2f, 239f, 56f),
            theme.copy(keyGroups = listOf(group, replacement)).groupCanvasForKey("0:1", KeyGradientLayer.BORDER, rectangles),
        )
    }

    @Test fun legacyGroupsAndWholeKeyboardDistributionKeepTheirExistingCanvas() {
        val theme =
            KeyThemeState(borderDistribution = GradientDistribution.SHARED, keyGroups = listOf(group.copy(sharedGradientCanvas = false)))
        assertNull(theme.groupCanvasForKey("0:1", KeyGradientLayer.BORDER, rectangles))
        assertEquals(GradientDistribution.SHARED, theme.forKey("0:1").borderDistribution)
    }
}
