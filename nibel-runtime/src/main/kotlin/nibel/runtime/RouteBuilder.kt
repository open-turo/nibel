package nibel.runtime

import android.os.Parcelable

/**
 * Builds a route name for navigation from the qualified class name and optional arguments.
 *
 * @param qualifiedName The fully qualified class name of the entry
 * @param args Optional arguments for the route
 * @return A route name string for navigation
 */
fun buildRouteName(qualifiedName: String, args: Parcelable?): String {
    return if (args != null) {
        "$qualifiedName/${args.hashCode()}"
    } else {
        qualifiedName
    }
}
