package com.turo.nibel.sample.featureA.results

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.runtime.NavigationController
import nibel.runtime.rememberResultLauncher

/**
 * Picker opened compose → compose. It can open another picker for a result itself, which covers
 * chained requests.
 */
@UiEntry(
    type = ImplementationType.Composable,
    args = PickerArgs::class,
    result = ColorResult::class,
)
@Composable
fun ColorPickerScreen(args: PickerArgs, navigator: NavigationController) {
    var nestedResult by rememberSaveable { mutableStateOf("none") }
    val nestedLauncher = rememberResultLauncher<ColorResult> { nestedResult = it?.color ?: "cancelled" }

    ColorPicker(title = "Color Picker (type.composable)", navigator = navigator) {
        Text("Picker level: ${args.level}")
        Text("Nested result: $nestedResult")
        OutlinedButton(
            onClick = {
                navigator.navigateForResult(
                    ColorPickerScreenEntry.newInstanceForResult(PickerArgs(args.level + 1)),
                    nestedLauncher,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Open nested picker")
        }
    }
}

/**
 * Picker opened fragment → compose.
 */
@UiEntry(type = ImplementationType.Fragment, result = ColorResult::class)
@Composable
fun ColorPickerFragmentScreen(navigator: NavigationController) {
    ColorPicker(title = "Color Picker (type.fragment)", navigator = navigator)
}

@Composable
private fun ColorPicker(
    title: String,
    navigator: NavigationController,
    extraContent: @Composable ColumnScope.() -> Unit = {},
) {
    Scaffold(
        topBar = { TopAppBar(title = title, onBack = navigator::navigateBack) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            for (color in pickableColors) {
                Button(
                    onClick = { navigator.setResultAndNavigateBack(ColorResult(color)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Pick $color")
                }
            }
            OutlinedButton(
                onClick = navigator::cancelResultAndNavigateBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cancel")
            }
            extraContent()
        }
    }
}
