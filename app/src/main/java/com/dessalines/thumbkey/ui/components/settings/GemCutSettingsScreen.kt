package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dessalines.thumbkey.ui.components.keyboard.FacetId
import com.dessalines.thumbkey.ui.components.keyboard.GemCutPreferences
import com.dessalines.thumbkey.ui.components.keyboard.GemCutPreviewScreen
import com.dessalines.thumbkey.ui.components.keyboard.KeywiFacets
import com.dessalines.thumbkey.ui.components.keyboard.FacetPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GemCutSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var facet by remember { mutableStateOf(GemCutPreferences.load(context)) }
    val accent = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("◇ Gem cuts") },
                navigationIcon = {
                    OutlinedButton(onClick = { navController.popBackStack() }) { Text("←") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Choose the physical geometry of your keyboard. Themes and character layouts stay independent.",
                style = MaterialTheme.typography.bodyMedium,
            )

            KeywiFacets.all.forEach { spec ->
                val selected = facet == spec.id
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        facet = spec.id
                        GemCutPreferences.save(context, facet)
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(if (selected) 2.dp else 1.dp,
                        if (selected) accent else MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(if (selected) "◉" else "○", color = accent)
                            Text(spec.displayName, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            if (selected) Text("Selected", color = accent,
                                style = MaterialTheme.typography.labelMedium)
                        }
                        Text(spec.description, style = MaterialTheme.typography.bodySmall)
                        // Reuse the existing facet preview boundary; never invent
                        // a second keyboard geometry just for the settings UI.
                        FacetPreview(facet = spec.id, modifier = Modifier.fillMaxWidth()) { meModifier ->
                            Column(meModifier.padding(8.dp)) {
                                Text("3 × 3 center · surrounding controls",
                                    style = MaterialTheme.typography.labelMedium)
                                Text("ME-Like uses the existing live keyboard renderer.",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            Text("Live preview", style = MaterialTheme.typography.titleMedium)
            Text("Preview the selected cut. HK-Like is still experimental; the live IME remains ME-Like until integration is complete.",
                style = MaterialTheme.typography.bodySmall)
            GemCutPreviewScreen(facet = facet)

            Text("About Gem Cuts", style = MaterialTheme.typography.titleMedium)
            Text("A cut changes key geometry, not your colors, theme, or character assignments. You can change cuts without resetting your other settings.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
