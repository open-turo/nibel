package com.turo.nibel.sample.featureA.secondscreen

import com.turo.nibel.sample.common.NextButton

data class SecondState(
    val inputText: String,
) {
    val title: String = "Feature A | Second Screen"

    val nextButtons = listOf(
        SecondNextButton.ThirdScreen,
        SecondNextButton.ExternalContentDemo,
    )
}

sealed class SecondNextButton(override val title: String) : NextButton {
    object ThirdScreen : SecondNextButton("Third Screen (type.composable, external)")
    object ExternalContentDemo : SecondNextButton("ExternalContent Demo (type.composable, external)")
}
