package com.turo.nibel.sample.featureB.thirdscreen

import com.turo.nibel.sample.common.NextButton

data class ThirdState(
    val inputText: String,
) {
    val title: String = "Feature B | Third Screen"

    val nextButtons = listOf(
        ThirdNextButton.ReturnResult,
        ThirdNextButton.FourthScreen,
        ThirdNextButton.FirstScreen,
    )
}

sealed class ThirdNextButton(override val title: String) : NextButton {
    object ReturnResult : ThirdNextButton("Return Result to SecondScreen")

    object FourthScreen : ThirdNextButton("Fourth Screen (legacy fragment, external)")

    object FirstScreen : ThirdNextButton("First Screen (type.fragment, external, no args)")
}
