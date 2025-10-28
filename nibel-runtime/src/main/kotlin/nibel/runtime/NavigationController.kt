package nibel.runtime

import android.os.Parcelable
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
     * Navigate to a screen and receive a typed result when it completes.
     *
     * The screen being navigated to must be annotated with a `result` parameter in [UiEntry]
     * or [UiExternalEntry], and the entry must implement [ResultEntry].
     *
     * The callback will be invoked with:
     * - Non-null result if the screen calls [setResultAndNavigateBack]
     * - Null if the screen calls [cancelResultAndNavigateBack] or user presses back
     *
     * Example:
     * ```
     * navigator.navigateForResult(PhotoPickerEntry.newInstance(args)) { result: PhotoResult? ->
     *     result?.let { handlePhoto(it.uri) }
     * }
     * ```
     *
     * @param entry The entry to navigate to (must implement [ResultEntry])
     * @param fragmentSpec Fragment specification for Fragment implementation types
     * @param composeSpec Compose specification for Composable implementation types
     * @param callback Callback invoked with result (null if cancelled)
     *
     * @see setResultAndNavigateBack
     * @see cancelResultAndNavigateBack
     * @see UiEntry
     * @see ResultEntry
     */
    abstract fun <TResult : Parcelable> navigateForResult(
        entry: Entry,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
        callback: (TResult?) -> Unit,
    )

    /**
     * Navigate to an external destination and receive a typed result when it completes.
     *
     * The destination's associated screen must be annotated with a `result` parameter in
     * [UiExternalEntry]. The destination should implement [nibel.annotations.DestinationWithResult]
     * for type safety.
     *
     * The callback will be invoked with:
     * - Non-null result if the screen calls [setResultAndNavigateBack]
     * - Null if the screen calls [cancelResultAndNavigateBack] or user presses back
     *
     * Example:
     * ```
     * navigator.navigateForResult(PhotoPickerDestination) { result: PhotoResult? ->
     *     result?.let { handlePhoto(it.uri) }
     * }
     * ```
     *
     * @param externalDestination The external destination to navigate to
     * @param fragmentSpec Fragment specification for Fragment implementation types
     * @param composeSpec Compose specification for Composable implementation types
     * @param callback Callback invoked with result (null if cancelled)
     *
     * @see setResultAndNavigateBack
     * @see cancelResultAndNavigateBack
     * @see UiExternalEntry
     * @see nibel.annotations.DestinationWithResult
     */
    abstract fun <TResult : Parcelable> navigateForResult(
        externalDestination: ExternalDestination,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
        callback: (TResult?) -> Unit,
    )

    /**
     * Set a result and navigate back to the previous screen.
     *
     * The result will be delivered to the callback provided in [navigateForResult].
     * This method can only be called from screens annotated with a `result` parameter
     * in [UiEntry] or [UiExternalEntry].
     *
     * Example:
     * ```
     * @UiEntry(
     *     type = ImplementationType.Composable,
     *     result = PhotoResult::class
     * )
     * @Composable
     * fun PhotoPickerScreen(navigator: NavigationController) {
     *     // ... photo selection UI
     *     Button(onClick = {
     *         navigator.setResultAndNavigateBack(PhotoResult(selectedUri))
     *     }) {
     *         Text("Select")
     *     }
     * }
     * ```
     *
     * @param result The result to return to the caller
     *
     * @see navigateForResult
     * @see cancelResultAndNavigateBack
     */
    abstract fun <TResult : Parcelable> setResultAndNavigateBack(result: TResult)

    /**
     * Navigate back without setting a result (equivalent to user pressing back button).
     *
     * The [navigateForResult] callback will receive null, indicating cancellation.
     * This method can be called from any screen.
     *
     * Example:
     * ```
     * @Composable
     * fun PhotoPickerScreen(navigator: NavigationController) {
     *     Button(onClick = { navigator.cancelResultAndNavigateBack() }) {
     *         Text("Cancel")
     *     }
     * }
     * ```
     *
     * @see navigateForResult
     * @see setResultAndNavigateBack
     */
    abstract fun cancelResultAndNavigateBack()
}
