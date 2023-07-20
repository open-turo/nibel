package nibel.runtime

import androidx.compose.runtime.Composable

/**
 * A root composable function that wraps the content of every [ComposableFragment].
 */
interface RootDelegate {

    @Composable
    fun Content(content: @Composable () -> Unit)
}

object EmptyRootDelegate : RootDelegate {

    @Composable
    override fun Content(content: @Composable () -> Unit) {
        content()
    }
}
