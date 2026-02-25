package com.turo.nibel.sample.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nibel.annotations.DestinationWithArgs

@Parcelize
data class ThirdArgs(val inputText: String) : Parcelable

data class ThirdScreenDestination(override val args: ThirdArgs) : DestinationWithArgs<ThirdArgs>

@Parcelize
data class FifthArgs(val label: String) : Parcelable

data class FifthScreenDestination(override val args: FifthArgs) : DestinationWithArgs<FifthArgs>
