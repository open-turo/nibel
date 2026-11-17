package com.turo.nibel.sample.featureA.results

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turo.nibel.sample.common.TopAppBar
import com.turo.nibel.sample.navigation.SizePickerDestination
import com.turo.nibel.sample.navigation.SizeResult
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.runtime.NavigationController
import nibel.runtime.rememberResultLauncher

/**
 * Compose caller of the navigate for result demo.
 */
@UiEntry(type = ImplementationType.Fragment)
@Composable
fun ResultsDemoScreen(navigator: NavigationController) {
    var lastResult by rememberSaveable { mutableStateOf("none") }
    val launcher = rememberResultLauncher<ColorResult> { lastResult = it?.color ?: "cancelled" }
    var lastSize by rememberSaveable { mutableStateOf("none") }
    val sizeLauncher = rememberResultLauncher<SizeResult> { lastSize = it?.size ?: "cancelled" }

    Scaffold(
        topBar = { TopAppBar(title = "Results Demo", onBack = navigator::navigateBack) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            Text("Last result: $lastResult")
            Text("Last size: $lastSize")
            Button(
                onClick = {
                    navigator.navigateForResult(
                        ColorPickerScreenEntry.newInstanceForResult(PickerArgs(level = 1)),
                        launcher,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Pick color (compose → compose)")
            }
            Button(
                onClick = {
                    navigator.navigateForResult(
                        LegacyColorPickerFragmentEntry.newInstanceForResult(),
                        launcher,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Pick color (compose → fragment)")
            }
            Button(
                onClick = { navigator.navigateForResult(SizePickerDestination, sizeLauncher) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Pick size (feature-B, external)")
            }
        }
    }
}
