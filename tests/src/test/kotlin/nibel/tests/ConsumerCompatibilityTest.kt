package nibel.tests

import android.app.Activity
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import io.kotest.matchers.nulls.shouldNotBeNull
import nibel.annotations.ExternalDestination
import nibel.runtime.ComposableEntry
import nibel.runtime.ComposeNavigationContext
import nibel.runtime.ComposeNavigationDelegate
import nibel.runtime.ComposeNavigationSpec
import nibel.runtime.ComposeSpec
import nibel.runtime.Entry
import nibel.runtime.ExploredEntriesRegistry
import nibel.runtime.FragmentEntry
import nibel.runtime.FragmentSpec
import nibel.runtime.FragmentTransactionContext
import nibel.runtime.FragmentTransactionSpec
import nibel.runtime.NavigationController
import nibel.runtime.NavigationControllerArgs
import nibel.runtime.Nibel
import nibel.runtime.NibelNavigationController
import org.junit.Test

/**
 * Source compatibility fixture: these are the ways consumers extend Nibel today, and they must keep
 * compiling without changes. Breaking this file is a breaking change for consumers.
 */
class ConsumerCompatibilityTest {

    /** Anonymous controller, for example to provide a fake one to a composable. */
    private fun anonymousController() = object : NavigationController() {
        override fun navigateBack() = Unit

        override fun navigateTo(
            externalDestination: ExternalDestination,
            fragmentSpec: FragmentSpec<*>,
            composeSpec: ComposeSpec<*>,
        ) = Unit

        override fun navigateTo(entry: Entry, fragmentSpec: FragmentSpec<*>, composeSpec: ComposeSpec<*>) = Unit
    }

    /** Controller that adds its own fragment navigation spec. */
    class NewActivityNavigationController(
        activity: Activity,
        internalNavController: NavController,
        fragmentManager: FragmentManager,
        onBackPressedDispatcher: OnBackPressedDispatcher,
        exploredEntries: ExploredEntriesRegistry,
    ) : NibelNavigationController(
        internalNavController,
        fragmentManager,
        onBackPressedDispatcher,
        exploredEntries,
    ) {
        private val newActivityContext = NewActivityContext(activity)

        override fun navigateTo(entry: FragmentEntry, fragmentSpec: FragmentSpec<*>) {
            when (fragmentSpec) {
                is NewActivitySpec -> with(fragmentSpec) { newActivityContext.navigateTo(entry) }
                else -> super.navigateTo(entry, fragmentSpec)
            }
        }

        override fun navigateTo(entry: ComposableEntry<*>, composeSpec: ComposeSpec<*>) {
            super.navigateTo(entry, composeSpec)
        }
    }

    /** Delegate that provides its own controller. */
    class CustomNavigationDelegate : ComposeNavigationDelegate() {
        @Composable
        override fun rememberNavigationController(args: NavigationControllerArgs): NavigationController =
            super.rememberNavigationController(args)
    }

    class NewActivityContext(val activity: Activity)

    /** Fragment spec with its own context. */
    class NewActivitySpec : FragmentSpec<NewActivityContext> {
        override fun NewActivityContext.navigateTo(entry: FragmentEntry) = Unit
    }

    /** Fragment transaction spec with custom animations. */
    private val slideInFragmentSpec = object : FragmentTransactionSpec() {
        override fun FragmentTransactionContext.navigateTo(entry: FragmentEntry) = Unit
    }

    /** Compose spec extending the default one. */
    class CustomComposeSpec : ComposeNavigationSpec() {
        override fun ComposeNavigationContext.navigateTo(entry: ComposableEntry<*>) = Unit
    }

    @Test
    fun `consumer extensions compile and instantiate`() {
        Nibel.configure()
        anonymousController().shouldNotBeNull()
        CustomNavigationDelegate().shouldNotBeNull()
        NewActivitySpec().shouldNotBeNull()
        slideInFragmentSpec.shouldNotBeNull()
        CustomComposeSpec().shouldNotBeNull()
    }
}
