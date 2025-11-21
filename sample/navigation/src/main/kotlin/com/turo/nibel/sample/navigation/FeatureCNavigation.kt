package com.turo.nibel.sample.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nibel.annotations.DestinationWithArgs

sealed class FourthArgs : Parcelable {
    @Parcelize
    data class WithText(val inputText: String) : FourthArgs()

    @Parcelize
    data object Empty : FourthArgs()
}

data class FourthScreenDestination(override val args: FourthArgs) : DestinationWithArgs<FourthArgs>
