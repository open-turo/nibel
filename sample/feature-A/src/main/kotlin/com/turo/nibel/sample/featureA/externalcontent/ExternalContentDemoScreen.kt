package com.turo.nibel.sample.featureA.externalcontent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turo.nibel.sample.common.TopAppBar
import com.turo.nibel.sample.navigation.FifthArgs
import com.turo.nibel.sample.navigation.FifthScreenDestination
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.runtime.LocalNavigationController
import nibel.runtime.Nibel

@UiEntry(type = ImplementationType.Composable)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExternalContentDemoScreen() {
    val navigationController = LocalNavigationController.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = "External Content Demo",
                onBack = { navigationController?.navigateBack() },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text("FifthScreen (feature-B) embedded directly via Nibel.ExternalContent:")

            // Cross-module composable embedded without a Fragment wrapper.
            // FifthScreenDestination is the only compile-time dependency on feature-B —
            // the actual implementation is resolved at runtime by Nibel.
            Nibel.ExternalContent(FifthScreenDestination(FifthArgs(label = "Hello from feature-A!")))
        }
    }
}
