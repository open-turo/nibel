package nibel.runtime

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle

/**
 * Default key for navigation arguments.
 */
const val NIBEL_ARGS = "nibel_args"

/**
 * Key for request ID used in result-based navigation.
 */
const val NIBEL_REQUEST_KEY = "nibel_request_key"

/**
 * Convert [Parcelable] args to a [Bundle].
 */
fun Parcelable.asNibelArgs() = Bundle().apply { putParcelable(Nibel.argsKey, this@asNibelArgs) }

/**
 * Add request key to a [Bundle] for result-based navigation.
 */
fun Bundle.putRequestKey(key: String?) {
    key?.let { putString(NIBEL_REQUEST_KEY, it) }
}

/**
 * Retrieve args from a [SavedStateHandle].
 */
fun <A : Parcelable> SavedStateHandle.getNibelArgs(): A? = get<A>(Nibel.argsKey)

/**
 * Retrieve request key from a [SavedStateHandle].
 */
fun SavedStateHandle.getRequestKey(): String? = get<String>(NIBEL_REQUEST_KEY)

/**
 * Retrieve args from a [Bundle].
 */
inline fun <reified A : Parcelable> Bundle.getNibelArgs(): A? =
    if (SDK_INT >= TIRAMISU) {
        getParcelable(Nibel.argsKey, A::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(Nibel.argsKey)
    }

/**
 * Retrieve request key from a [Bundle].
 */
fun Bundle.getRequestKey(): String? = getString(NIBEL_REQUEST_KEY)
