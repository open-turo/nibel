package com.turo.nibel.sample.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nibel.annotations.DestinationWithArgs
import nibel.annotations.DestinationWithNoArgs
import nibel.annotations.DestinationWithResult

@Parcelize
data class ThirdArgs(val input: Input) : Parcelable

data class ThirdScreenDestination(override val args: ThirdArgs) : DestinationWithArgs<ThirdArgs>

@Parcelize
data class FifthArgs(val label: String) : Parcelable

data class FifthScreenDestination(override val args: FifthArgs) : DestinationWithArgs<FifthArgs>

@Parcelize
data class SizeResult(val size: String) : Parcelable

object SizePickerDestination : DestinationWithNoArgs, DestinationWithResult<SizeResult>

@Parcelize
sealed class Input : Parcelable {
    data class Text(val inputText: String) : Input() {
        override fun toString(): String = inputText
    }

    data object Nothing : Input() {
        override fun toString(): String = ""
    }
}
