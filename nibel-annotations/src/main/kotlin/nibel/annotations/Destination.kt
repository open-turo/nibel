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
 * A base type for destinations for internal single-module navigation.
 */
interface InternalDestination : Destination
