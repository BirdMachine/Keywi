package com.dessalines.thumbkey.ui.components.keyboard

/**
 * Keywi's three independent customization axes.
 *
 * Theme = appearance. It owns no key positions or actions.
 * Facet = physical topology / geometry. A facet owns regions and key placement,
 *         but not the user's language/content mapping.
 * Layout = content/action mapping projected onto a compatible facet.
 *
 * Keeping these types separate is intentional: Neon Aviary should look like
 * Neon Aviary whether it is painted onto ME-Like or Machine Cut, and a layout
 * should be reusable without smuggling geometry into its identity.
 */
@JvmInline
value class ThemeId(val value: String)

@JvmInline
value class LayoutId(val value: String)

enum class FacetId {
    /** Existing MessageEase-inspired Keywi geometry. */
    ME_LIKE,

    /** Dense desktop/power-user geometry inspired by Hacker's Keyboard. */
    MACHINE_CUT,
}

data class GemCutSelection(
    val facet: FacetId = FacetId.ME_LIKE,
    val layout: LayoutId,
    val theme: ThemeId,
)

/** Geometry capability description; deliberately contains no theme values. */
data class FacetSpec(
    val id: FacetId,
    val displayName: String,
    val description: String,
    val supportsLayers: Boolean = true,
    val supportsCustomBoards: Boolean = true,
)

object KeywiFacets {
    val meLike = FacetSpec(
        id = FacetId.ME_LIKE,
        displayName = "ME-Like",
        description = "Keywi's MessageEase-like facet: a central 3×3 surface with persistent surrounding controls.",
    )

    val machineCut = FacetSpec(
        id = FacetId.MACHINE_CUT,
        displayName = "Machine Cut",
        description = "Dense Hacker's Keyboard-inspired power-user facet with desktop-like rows and controls.",
    )

    val all = listOf(meLike, machineCut)
}
