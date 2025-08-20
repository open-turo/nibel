package com.turo.nibel.sample.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nibel.annotations.DestinationWithArgs

@Parcelize
data class ThirdArgs(val inputText: String) : Parcelable

@Parcelize
data class ThirdResult(val inputText: String) : Parcelable

data class ThirdScreenDestination(override val args: ThirdArgs) : DestinationWithArgs<ThirdArgs>
