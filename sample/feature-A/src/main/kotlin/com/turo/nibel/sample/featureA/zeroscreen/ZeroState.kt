package com.turo.nibel.sample.featureA.zeroscreen

import com.turo.nibel.sample.common.NextButton

data class ZeroState(
    val inputText: String,
) {
    val title: String = "Feature A | Zero Screen"

    val nextButtons = listOf(
        DearbornNextButton.FirstScreen,
        DearbornNextButton.ThirdScreen,
        DearbornNextButton.ResultsDemo,
        DearbornNextButton.PickColor,
    )
}

sealed class DearbornNextButton(override val title: String) : NextButton {
    object FirstScreen : DearbornNextButton("First Screen (type.fragment, internal)")
    object ThirdScreen : DearbornNextButton("Third Screen (type.fragment, external)")
    object ResultsDemo : DearbornNextButton("Results Demo (type.fragment, internal)")
    object PickColor : DearbornNextButton("Pick color (fragment → compose)")
}
