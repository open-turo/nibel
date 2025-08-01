package com.turo.nibel.sample.featureA.photopicker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Result data class for PhotoPickerScreen demonstration.
 * Represents the data returned from a photo picker screen.
 */
@Parcelize
data class PhotoPickerResult(
    val photoUrl: String,
    val photoName: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
