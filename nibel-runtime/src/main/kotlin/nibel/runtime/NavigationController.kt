package nibel.runtime

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
    protected val resultCallbackRegistry = ResultCallbackRegistry()

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
     * Navigate to a result-based screen and receive a callback with the result.
     *
     * @param entry The result entry to navigate to
     * @param callback The callback to be invoked when the result is available
     * @param fragmentSpec Fragment navigation specification (optional)
     * @param composeSpec Compose navigation specification (optional)
     */
    abstract fun <R : Any> navigateForResult(
        entry: ResultEntry<R>,
        callback: ResultCallback<R>,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
    )

    /**
     * Navigate to a result-based external destination and receive a callback with the result.
     *
     * @param destination The external destination to navigate to
     * @param callback The callback to be invoked when the result is available
     * @param fragmentSpec Fragment navigation specification (optional)
     * @param composeSpec Compose navigation specification (optional)
     */
    abstract fun <R : Any> navigateForResult(
        destination: ExternalDestination,
        callback: ResultCallback<R>,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
    )

    /**
     * Sets the result for a result-based screen and navigates back.
     * This should be called from screens that were launched with [navigateForResult].
     *
     * @param result The result to return to the calling screen
     */
    abstract fun <R : Any> setResultAndNavigateBack(result: R)

    /**
     * Cancels a result-based navigation and navigates back without providing a result.
     */
    abstract fun cancelResultAndNavigateBack()
}
