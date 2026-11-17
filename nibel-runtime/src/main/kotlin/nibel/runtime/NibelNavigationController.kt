package nibel.runtime

import android.os.Parcelable
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import nibel.annotations.DestinationWithResult
import nibel.annotations.ExternalDestination

/**
 * Default implementation of [NavigationController]. Depending on circumstances, relies on 2 tools
 * for navigation:
 * - Fragment transaction manager.
 * - Compose navigation library.
 */
open class NibelNavigationController(
    val internalNavController: NavController,
    val fragmentManager: FragmentManager,
    val onBackPressedDispatcher: OnBackPressedDispatcher,
    val exploredEntries: ExploredEntriesRegistry,
    fragmentSpec: FragmentSpec<*> = Nibel.fragmentSpec,
    composeSpec: ComposeSpec<*> = Nibel.composeSpec,
) : NavigationController(fragmentSpec, composeSpec) {

    protected val fragmentTransactionContext =
        FragmentTransactionContext(fragmentManager)

    protected val composeNavigationContext =
        ComposeNavigationContext(internalNavController, exploredEntries)

    override fun navigateBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun navigateTo(
        externalDestination: ExternalDestination,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>,
    ) {
        val destinationEntry = Nibel.findEntryFactory(externalDestination)
            ?.newInstance(externalDestination)
            ?: error("Unable to find destination '${externalDestination.javaClass}'")
        navigateTo(entry = destinationEntry, fragmentSpec, composeSpec)
    }

    override fun navigateTo(
        entry: Entry,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>,
    ) {
        when (entry) {
            is ComposableEntry<*> -> navigateTo(entry, composeSpec)
            is FragmentEntry -> navigateTo(entry, fragmentSpec)
        }
    }

    open fun navigateTo(entry: FragmentEntry, fragmentSpec: FragmentSpec<*>) {
        when (fragmentSpec) {
            is FragmentTransactionSpec -> with(fragmentSpec) {
                fragmentTransactionContext.navigateTo(entry)
            }

            else -> error("Unknown fragment navigation spec '${fragmentSpec.javaClass}'")
        }
    }

    open fun navigateTo(entry: ComposableEntry<*>, composeSpec: ComposeSpec<*>) {
        when (composeSpec) {
            is ComposeNavigationSpec -> with(composeSpec) {
                composeNavigationContext.navigateTo(entry)
            }

            else -> error("Unknown compose navigation spec '${composeSpec.javaClass}'")
        }
    }

    override fun <D, R : Parcelable> navigateForResult(
        destination: D,
        launcher: ResultLauncher<R>,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>,
    ) where D : ExternalDestination, D : DestinationWithResult<R> {
        val destinationEntry = Nibel.findEntryFactory(destination)
            ?.newInstance(destination)
            ?: error("Unable to find destination '${destination.javaClass}'")
        navigateForResult(ResultEntry(destinationEntry), launcher, fragmentSpec, composeSpec)
    }

    override fun <R : Parcelable> navigateForResult(
        entry: ResultEntry<R>,
        launcher: ResultLauncher<R>,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>,
    ) {
        when (val target = entry.entry) {
            is FragmentEntry -> {
                launcher.attachTo(target)
                navigateTo(target, fragmentSpec)
                // Only once navigation didn't throw, so a failed call leaves no pending request.
                // A fragment transaction covers the caller only if it replaces it, which the caller
                // detects itself. Any other spec, for example one that starts an activity, is
                // assumed to cover it.
                launcher.request.launch(callerCovered = fragmentSpec !is FragmentTransactionSpec)
            }

            is ComposableEntry<*> -> {
                navigateTo(target, composeSpec)
                // The target belongs to the opened screen, not to this controller, so that
                // chained requests don't overwrite each other.
                val openedEntry = internalNavController.currentBackStackEntry
                check(openedEntry != null && openedEntry.isFor(target)) {
                    "${composeSpec.javaClass.name} didn't show '${target.name}' right away, so it can't " +
                        "be used with navigateForResult()."
                }
                launcher.target.writeTo(openedEntry.savedStateHandle)
                // navigate() already paused the caller, before the request started.
                launcher.request.launch(callerCovered = true)
            }
        }
    }

    override fun setResultAndNavigateBack(result: Parcelable) {
        sendResult(result)
        navigateBack()
    }

    override fun cancelResultAndNavigateBack() {
        sendResult(null)
        navigateBack()
    }

    private fun sendResult(result: Parcelable?) {
        val target = ResultTarget.from(internalNavController.currentBackStackEntry?.savedStateHandle) ?: return
        fragmentManager.sendResult(target, result)
    }

    private fun NavBackStackEntry.isFor(entry: ComposableEntry<*>): Boolean {
        val route = destination.route ?: return false
        return route == entry.name || route.startsWith("${entry.name}/")
    }
}
