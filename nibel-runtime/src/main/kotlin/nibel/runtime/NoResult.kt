package nibel.runtime

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A special type of result that is treated specially by Nibel's annotation processor and marks a
 * screen that returns no result.
 */
@Parcelize
object NoResult : Parcelable
