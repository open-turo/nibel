package nibel.runtime

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle

/**
 * Default key for passing arguments during the navigation.
 */
const val NIBEL_ARGS = "nibel_args"

/**
 * Converts [Parcelable] args to a [Bundle] to pass them in fragments.
 *
 * A [Nibel.argsKey] is used as a key for storing args.
 */
fun Parcelable.asNibelArgs() = Bundle().apply { putParcelable(Nibel.argsKey, this@asNibelArgs) }

/**
 * Retrieves Nibel args from a [SavedStateHandle].
 *
 * A [Nibel.argsKey] is used as a key for retrieving args.
 */
fun <A : Parcelable> SavedStateHandle.getNibelArgs(): A? = get<A>(Nibel.argsKey)

/**
 * Retrieves Nibel args from a [Bundle].
 *
 * A [Nibel.argsKey] is used as a key for retrieving args.
 */
inline fun <reified A : Parcelable> Bundle.getNibelArgs(): A? =
    if (SDK_INT >= TIRAMISU) getParcelable(Nibel.argsKey, A::class.java)
    else @Suppress("DEPRECATION") getParcelable(Nibel.argsKey)
