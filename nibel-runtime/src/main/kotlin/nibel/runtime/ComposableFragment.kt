package nibel.runtime

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.annotations.UiExternalEntry

/**
 * Base class for generated screen entries when [ImplementationType.Fragment] is used.
 *
 * See [UiEntry] and [UiExternalEntry].
 */
abstract class ComposableFragment : Fragment() {

    /**
     * [Composable] content of the screen.
     */
    @Composable
    abstract fun ComposableContent()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        @Suppress("DEPRECATION")
        val args = arguments?.get(Nibel.argsKey) as? Parcelable?

        setContent {
            Nibel.RootDelegate.Content {
                CompositionLocalProvider(
                    LocalImplementationType provides ImplementationType.Fragment,
                ) {
                    Nibel.NavigationDelegate.Content(rootArgs = args) {
                        ComposableContent()
                    }
                }
            }
        }
    }
}
