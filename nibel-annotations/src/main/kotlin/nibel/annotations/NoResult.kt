package nibel.annotations

import android.os.Parcelable

/**
 * A special type of result that is treated specially by Nibel's annotation processor and marks a
 * screen with no result.
 */
@Suppress("ParcelCreator")
object NoResult : Parcelable
