package nibel.annotations

import android.os.Parcelable

/**
 * A base type for all navigation destinations.
 */
sealed interface Destination

/**
 * A base type for destinations for external multi-module navigation.
 */
sealed interface ExternalDestination : Destination

/**
 * An external destination type for screens with no arguments.
 * Each descendant of [DestinationWithNoArgs] must be associated with exactly one screen annotated
 * with [UiExternalEntry].
 */
interface DestinationWithNoArgs : ExternalDestination

/**
 * An external destination type for screens with arguments.
 * Each descendant of [DestinationWithArgs] must be associated with exactly one screen annotated
 * with [UiExternalEntry].
 */
interface DestinationWithArgs<A : Parcelable> : ExternalDestination {
    /**
     * Type-safe `Parcelable` args for the screen.
     */
    val args: A
}

/**
 * Declares the type of result returned by the screen associated with a destination, so callers
 * can open it with `NavigationController.navigateForResult` and a launcher for the same type.
 *
 * Combine it with [DestinationWithNoArgs] or [DestinationWithArgs]. The associated screen must
 * declare the same result type in its [UiExternalEntry] or [LegacyExternalEntry] annotation.
 * ```
 * object DatePickerDestination : DestinationWithNoArgs, DestinationWithResult<PickedDate>
 *
 * data class EditNameDestination(
 *     override val args: EditNameArgs,
 * ) : DestinationWithArgs<EditNameArgs>, DestinationWithResult<EditedName>
 * ```
 */
interface DestinationWithResult<R : Parcelable>

/**
 * A base type for destinations for internal single-module navigation.
 */
interface InternalDestination : Destination
