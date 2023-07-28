package com.turo.nibel.sample.featureC.fourthscreen

import com.turo.nibel.sample.common.NextButton

data class FourthState(
    val inputText: String
) {
    val title: String = "Feature C | Fourth Screen"

    val nextButtons = listOf<FourthNextButton>()
}

sealed class FourthNextButton(override val title: String) : NextButton
