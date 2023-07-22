package nibel.runtime

import android.os.Parcelable
import androidx.compose.runtime.Composable

interface NavigationDelegate<T> {

    @Composable
    fun rememberNavigationController(args: T): NavigationController

    @Composable
    fun Content(rootArgs: Parcelable?, content: @Composable () -> Unit)
}
