package com.turo.nibel.sample.navigation

import android.os.Parcelable
import nibel.annotations.DestinationWithArgs
import nibel.annotations.DestinationWithNoArgs
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModenaArgs(val inputText: String) : Parcelable

data class ModenaDestination(
    override val args: ModenaArgs
) : DestinationWithArgs<ModenaArgs>

object StuttgartDestination : DestinationWithNoArgs
