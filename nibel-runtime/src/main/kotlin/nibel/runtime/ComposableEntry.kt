package nibel.runtime

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.annotations.UiExternalEntry

/**
 * Base class for generated screen entries when [ImplementationType.Composable] is used.
 *
 * See [UiEntry] and [UiExternalEntry].
 */
abstract class ComposableEntry<A : Parcelable>(
    open val args: A?,
    open val name: String,
) : Entry, Parcelable {

    /**
     * [Composable] content of the screen.
     */
    @Composable
    abstract fun ComposableContent()

    @SuppressLint("NotConstructor")
    @Suppress("MemberNameEqualsClassName")
    @Composable
    fun ComposableEntry() {
        CompositionLocalProvider(
            LocalImplementationType provides ImplementationType.Composable
        ) {
            ComposableContent()
        }
    }
}

fun buildRouteName(base: String, args: Parcelable? = null): String = base
