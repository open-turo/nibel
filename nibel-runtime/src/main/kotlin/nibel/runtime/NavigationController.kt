package nibel.runtime

import android.os.Parcelable
import nibel.annotations.DestinationWithResult
import nibel.annotations.ExternalDestination
import nibel.annotations.UiEntry
import nibel.annotations.UiExternalEntry

/**
 * [NavigationController] is a core component for navigation between screens of the app.
 * See [UiEntry], [UiExternalEntry]
 *
 * ```
 * @UiEntry(...)
 * @Composable
 * fun FooScreen(navigator: NavigationController) {
 *
 *   navigator.navigateTo(BarScreenEntry.newInstance())
 *   // OR
 *   navigator.navigatorTo(BarDestination)
 * }
 * ```
 */
abstract class NavigationController(
    val fragmentSpec: FragmentSpec<*> = Nibel.fragmentSpec,
    val composeSpec: ComposeSpec<*> = Nibel.composeSpec,
) {

    /**
     * Perform back navigation.
     */
    abstract fun navigateBack()

    /**
     * Perform navigation to an external entry declared in another module.
     */
    abstract fun navigateTo(
        externalDestination: ExternalDestination,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
    )

    /**
     * Perform a navigation to a screen by having a direct reference to its generated entry type.
     */
    abstract fun navigateTo(
        entry: Entry,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
    )

    /**
     * Opens the screen of [entry] for a result that is delivered to [launcher].
     *
     * The result types of the entry and the launcher must match. See [ResultLauncher] for how
     * results and cancellations are delivered.
     */
    open fun <R : Parcelable> navigateForResult(
        entry: ResultEntry<R>,
        launcher: ResultLauncher<R>,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
    ): Unit = resultsNotSupported()

    /**
     * Opens the screen associated with [destination] for a result that is delivered to [launcher].
     *
     * The destination must declare its result type with [DestinationWithResult], and it must match
     * the launcher's.
     */
    open fun <D, R : Parcelable> navigateForResult(
        destination: D,
        launcher: ResultLauncher<R>,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
    ): Unit where D : ExternalDestination, D : DestinationWithResult<R> = resultsNotSupported()

    /**
     * Sends [result] to the caller of the current screen and navigates back. If the screen wasn't
     * opened for a result, only navigates back.
     *
     * @throws IllegalArgumentException if [result] isn't of the type the caller expects.
     */
    open fun setResultAndNavigateBack(result: Parcelable): Unit = resultsNotSupported()

    /**
     * Sends a `null` result to the caller of the current screen and navigates back. If the screen
     * wasn't opened for a result, only navigates back.
     */
    open fun cancelResultAndNavigateBack(): Unit = resultsNotSupported()

    private fun resultsNotSupported(): Nothing =
        throw UnsupportedOperationException("${javaClass.name} doesn't support navigation for result.")
}
