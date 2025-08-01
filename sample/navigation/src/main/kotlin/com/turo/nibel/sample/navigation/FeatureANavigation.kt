package com.turo.nibel.sample.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nibel.annotations.DestinationWithNoArgs
import nibel.annotations.DestinationWithArgs

object FirstScreenDestination : DestinationWithNoArgs

/**
 * Arguments for the PhotoPickerScreen.
 */
@Parcelize
data class PhotoPickerArgs(
    val allowMultiple: Boolean = false,
    val maxPhotos: Int = 1
) : Parcelable
