package nibel.runtime

import android.os.Parcelable

/**
 * Marker interface for entry classes that return a result to their caller.
 *
 * Generated entry classes implement this interface when the `result` parameter is specified
 * in [nibel.annotations.UiEntry] or [nibel.annotations.UiExternalEntry] annotations.
 *
 * This interface is always implemented alongside either [ComposableEntry] or [FragmentEntry],
 * never on its own. It adds result type information to enable type-safe result navigation where:
 * - The caller uses `NavigationController.navigateForResult()` to navigate and receive a callback
 * - The called screen uses `NavigationController.setResultAndNavigateBack()` to return a result
 * - The called screen can use `NavigationController.cancelResultAndNavigateBack()` to cancel without result
 *
 * The result type [TResult] must be [Parcelable] and is validated at compile-time by the
 * annotation processor.
 *
 * Example usage:
 * ```
 * // Define result type
 * @Parcelize
 * data class PhotoResult(val uri: String) : Parcelable
 *
 * // Annotate screen with result
 * @UiEntry(
 *     type = ImplementationType.Composable,
 *     result = PhotoResult::class
 * )
 * @Composable
 * fun PhotoPickerScreen(navigator: NavigationController) {
 *     // ... selection UI
 *     navigator.setResultAndNavigateBack(PhotoResult(selectedUri))
 * }
 *
 * // Navigate and receive result
 * navigator.navigateForResult(PhotoPickerScreenEntry.newInstance()) { result: PhotoResult? ->
 *     if (result != null) {
 *         // User selected a photo
 *     } else {
 *         // User cancelled
 *     }
 * }
 * ```
 *
 * The generated entry class will look like:
 * ```
 * class PhotoPickerScreenEntry(...)
 *     : ComposableEntry<NoArgs>(...), ResultEntry<NoArgs, PhotoResult> {
 *     override val resultType: Class<PhotoResult> = PhotoResult::class.java
 *     // ...
 * }
 * ```
 *
 * @param TArgs The type of arguments passed to the entry (must be [Parcelable])
 * @param TResult The type of result returned by the entry (must be [Parcelable])
 *
 * @see nibel.annotations.UiEntry
 * @see nibel.annotations.UiExternalEntry
 * @see NavigationController.navigateForResult
 * @see NavigationController.setResultAndNavigateBack
 * @see NavigationController.cancelResultAndNavigateBack
 */
interface ResultEntry<TArgs : Parcelable, TResult : Parcelable> {
    /**
     * The runtime [Class] of the result type.
     * Used for type checking and result delivery at runtime.
     */
    val resultType: Class<TResult>
}
