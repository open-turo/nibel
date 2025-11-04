package nibel.runtime

import android.os.Parcelable

/**
 * Builds a unique route name for navigation based on the entry class name and arguments.
 *
 * The route name is used by the Compose Navigation library to identify destinations.
 * For entries with arguments, the route includes a hash of the arguments to ensure uniqueness.
 * For entries without arguments, only the qualified name is used.
 *
 * @param qualifiedName The fully qualified class name of the entry
 * @param args The arguments for the entry, or null if no arguments
 * @return A unique route string for navigation
 */
fun buildRouteName(qualifiedName: String, args: Parcelable?): String {
    return if (args != null) {
        "$qualifiedName@${args.hashCode()}"
    } else {
        qualifiedName
    }
}
