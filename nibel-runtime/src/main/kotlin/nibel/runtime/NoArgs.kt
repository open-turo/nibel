package nibel.runtime

import android.os.Parcelable
import nibel.annotations.LegacyEntry
import nibel.annotations.UiEntry
import kotlinx.parcelize.Parcelize

/**
 * A special type of args for [UiEntry] and [LegacyEntry] that is treated specially by Nibel's
 * annotation processor and marks a screen with no arguments.
 */
@Parcelize
object NoArgs : Parcelable
