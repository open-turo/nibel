package nibel.runtime

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A special type of result that is treated specially by Nibel's annotation processor and marks a
 * screen that does not return a result.
 *
 * This is the default value for the `result` parameter in [nibel.annotations.UiEntry] and
 * [nibel.annotations.UiExternalEntry] annotations.
 *
 * @see nibel.annotations.UiEntry
 * @see nibel.annotations.UiExternalEntry
 */
@Parcelize
object NoResult : Parcelable
