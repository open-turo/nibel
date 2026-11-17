package com.turo.nibel.sample.featureA.results

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PickerArgs(val level: Int) : Parcelable

@Parcelize
data class ColorResult(val color: String) : Parcelable

val pickableColors = listOf("Red", "Green", "Blue")
