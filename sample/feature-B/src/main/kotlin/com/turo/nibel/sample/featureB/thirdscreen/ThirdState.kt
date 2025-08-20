package com.turo.nibel.sample.featureB.thirdscreen

import com.turo.nibel.sample.common.NextButton

data class ThirdState(
    val inputText: String,
) {
    val title: String = "Feature B | Third Screen"

    val nextButtons = listOf(
        ThirdNextButton.FourthScreen,
        ThirdNextButton.FirstScreen,
        ThirdNextButton.ReturnResult,
    )
}

sealed class ThirdNextButton(override val title: String) : NextButton {
    object FourthScreen : ThirdNextButton("Fourth Screen (legacy fragment, external)")

    object FirstScreen : ThirdNextButton("First Screen (type.fragment, external, no args)")

    object ReturnResult : ThirdNextButton("Return Result")
}
