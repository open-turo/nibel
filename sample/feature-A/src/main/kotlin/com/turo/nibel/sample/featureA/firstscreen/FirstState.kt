package com.turo.nibel.sample.featureA.firstscreen

import com.turo.nibel.sample.common.NextButton

data class FirstState(
    val inputText: String
) {
    val title: String = "Feature A | First Screen"

    val nextButtons = listOf(
        FirstNextButton.SecondScreen,
    )
}

sealed class FirstNextButton(override val title: String) : NextButton {
    object SecondScreen : FirstNextButton("Second Screen (type.composable, internal)")
}
