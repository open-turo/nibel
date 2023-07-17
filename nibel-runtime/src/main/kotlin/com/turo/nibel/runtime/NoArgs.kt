package com.turo.nibel.runtime

import android.os.Parcelable
import com.turo.nibel.annotations.LegacyEntry
import com.turo.nibel.annotations.UiEntry
import kotlinx.parcelize.Parcelize

/**
 * A special type of args for [UiEntry] and [LegacyEntry] that is treated specially by Nibel's
 * annotation processor and marks a screen with no arguments.
 */
@Parcelize
object NoArgs : Parcelable
