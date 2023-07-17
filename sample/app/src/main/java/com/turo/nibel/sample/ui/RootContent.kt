package com.turo.nibel.sample.ui

import androidx.compose.runtime.Composable
import com.turo.nibel.runtime.RootDelegate
import com.turo.nibel.sample.ui.theme.NibelSampleTheme

class RootContent : RootDelegate {

    @Composable
    override fun Content(content: @Composable () -> Unit) {
        NibelSampleTheme {
            content()
        }
    }
}
