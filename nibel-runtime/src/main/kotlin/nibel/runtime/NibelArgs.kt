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
 * Convert [Parcelable] args to a [Bundle].
 */
fun Parcelable.asNibelArgs() = Bundle().apply { putParcelable(Nibel.argsKey, this@asNibelArgs) }

/**
 * Retrieve args from a [SavedStateHandle].
 */
fun <A : Parcelable> SavedStateHandle.getNibelArgs(): A? = get<A>(Nibel.argsKey)

/**
 * Retrieve args from a [Bundle].
 */
inline fun <reified A : Parcelable> Bundle.getNibelArgs(): A? =
    if (SDK_INT >= TIRAMISU) getParcelable(Nibel.argsKey, A::class.java)
    else @Suppress("DEPRECATION") getParcelable(Nibel.argsKey)
