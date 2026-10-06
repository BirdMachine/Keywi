package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dessalines.thumbkey.ui.components.keyboard.FacetId
import com.dessalines.thumbkey.ui.components.keyboard.GemCutPreferences
import com.dessalines.thumbkey.ui.components.keyboard.GemCutPreviewScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GemCutSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var facet by remember { mutableStateOf(GemCutPreferences.load(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gem cuts") },
                navigationIcon = {
                    OutlinedButton(onClick = { navController.popBackStack() }) { Text("←") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Cut controls physical key geometry. Theme and layout stay independent.")
            Button(
                onClick = {
                    facet = FacetId.ME_LIKE
                    GemCutPreferences.save(context, facet)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (facet == FacetId.ME_LIKE) "✓ ME-Like" else "ME-Like") }
            Button(
                onClick = {
                    facet = FacetId.HK_LIKE
                    GemCutPreferences.save(context, facet)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (facet == FacetId.HK_LIKE) "✓ HK-Like" else "HK-Like") }
            GemCutPreviewScreen(facet = facet)
        }
    }
}
