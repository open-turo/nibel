package com.turo.nibel.sample.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nibel.annotations.DestinationWithArgs

@Parcelize
data class FourthArgs(val inputText: String) : Parcelable

data class FourthScreenDestination(override val args: FourthArgs) : DestinationWithArgs<FourthArgs>
