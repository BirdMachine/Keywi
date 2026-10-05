package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Developer/settings preview used to iterate on facet geometry before the
 * live IME switches away from its safe ME-Like default. */
@Composable
fun GemCutPreviewScreen(
    facet: FacetId = FacetId.MACHINE_CUT,
    modifier: Modifier = Modifier,
) {
    val spec = KeywiFacets.all.first { it.id == facet }
    Column(
        modifier = modifier.fillMaxWidth().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(spec.displayName, style = MaterialTheme.typography.titleMedium)
        Text(spec.description, style = MaterialTheme.typography.bodySmall)
        FacetPreview(facet = facet, modifier = Modifier.fillMaxWidth()) { meModifier ->
            Column(meModifier.padding(12.dp)) {
                Text("ME-Like")
                Text("Live ME-Like renderer remains unchanged while Gem Cut is introduced.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
