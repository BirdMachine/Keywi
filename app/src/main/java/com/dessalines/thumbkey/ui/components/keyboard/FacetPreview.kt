package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Small rendering boundary used by settings/previews while the live IME is
 * migrated incrementally. ME-Like remains the live/default facet; HK-Like
 * can now be rendered without creating a parallel theme system.
 */
@Composable
fun FacetPreview(
    facet: FacetId,
    modifier: Modifier = Modifier,
    meLike: @Composable (Modifier) -> Unit,
) {
    when (facet) {
        FacetId.ME_LIKE -> meLike(modifier)
        FacetId.HK_LIKE -> MachineCutSkeleton(modifier)
    }
}
