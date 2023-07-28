package com.turo.nibel.sample.featureA.secondscreen

import com.turo.nibel.sample.common.NextButton

data class SecondState(
    val inputText: String
) {
    val title: String = "Feature A | Second Screen"

    val nextButtons = listOf(
        SecondNextButton.SecondScreen,
    )
}

sealed class SecondNextButton(override val title: String) : NextButton {
    object SecondScreen : SecondNextButton("Third Screen (type.composable, external)")
}
