package nibel.annotations

import android.os.Parcelable

/**
 * A base type for all navigation destinations.
 */
sealed interface Destination

/**
 * A base type for destinations used in multi-module navigation.
 */
sealed interface ExternalDestination : Destination

/**
 * A destination type for screens with no arguments.
 * Each type of [DestinationWithNoArgs] must be associated with exactly one screen annotated with
 * [UiExternalEntry].
 *
 * See [UiExternalEntry].
 */
interface DestinationWithNoArgs : ExternalDestination

/**
 * A destination type for screens with arguments.
 * Each type of [DestinationWithArgs] must be associated with exactly one screen annotated with
 * [UiExternalEntry].
 *
 * See [UiExternalEntry].
 */
interface DestinationWithArgs<A : Parcelable> : ExternalDestination {
    /**
     * Type-safe `Parcelable` args for the screen.
     */
    val args: A
}

/**
 * A type of a destination that it used for navigating internally in a single feature module.
 */
interface InternalDestination : Destination
