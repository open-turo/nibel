package com.turo.nibel.sample.ui

import androidx.compose.runtime.Composable
import com.turo.nibel.sample.ui.theme.NibelSampleTheme
import nibel.runtime.RootDelegate

class RootContent : RootDelegate {

    @Composable
    override fun Content(content: @Composable () -> Unit) {
        NibelSampleTheme {
            content()
        }
    }
}
