package com.turo.nibel.sample.featureB.sizepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turo.nibel.sample.common.TopAppBar
import com.turo.nibel.sample.navigation.SizePickerDestination
import com.turo.nibel.sample.navigation.SizeResult
import nibel.annotations.ImplementationType
import nibel.annotations.UiExternalEntry
import nibel.runtime.NavigationController

private val pickableSizes = listOf("S", "M", "L")

/**
 * Picker in feature-B, opened for a result from feature-A through [SizePickerDestination].
 */
@UiExternalEntry(
    type = ImplementationType.Composable,
    destination = SizePickerDestination::class,
    result = SizeResult::class,
)
@Composable
fun SizePickerScreen(navigator: NavigationController) {
    Scaffold(
        topBar = { TopAppBar(title = "Size Picker (feature-B, external)", onBack = navigator::navigateBack) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            for (size in pickableSizes) {
                Button(
                    onClick = { navigator.setResultAndNavigateBack(SizeResult(size)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Pick size $size")
                }
            }
            OutlinedButton(
                onClick = navigator::cancelResultAndNavigateBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cancel")
            }
        }
    }
}
