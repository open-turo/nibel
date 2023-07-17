package com.turo.nibel.sample.featureA.yokohama

import com.turo.nibel.sample.common.NextButton

data class YokohamaState(
    val inputText: String
) {
    val title: String = "Feature A | Yokohama"

    val nextButtons = listOf(
        YokohamaNextButton.Modena,
        YokohamaNextButton.Molsheim
    )
}

sealed class YokohamaNextButton(override val title: String) : NextButton {
    object Modena : YokohamaNextButton("Modena (composable, external, args)")

    object Molsheim : YokohamaNextButton("Molsheim (legacy fragment, internal, args)")
}
