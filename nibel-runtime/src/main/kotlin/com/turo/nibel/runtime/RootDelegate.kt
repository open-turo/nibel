package com.turo.nibel.runtime

import androidx.compose.runtime.Composable

/**
 * A delegated composable function that is called in [ComposableFragment] when setting
 */
interface RootDelegate {

    @Composable
    fun Content(content: @Composable () -> Unit)
}

/**
 *
 */
object EmptyRootDelegate : RootDelegate {

    @Composable
    override fun Content(content: @Composable () -> Unit) {
        content()
    }
}
