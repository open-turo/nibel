package com.turo.nibel.sample.featureB.modena

import com.turo.nibel.sample.common.NextButton

data class ModenaState(
    val inputText: String
) {
    val title: String = "Feature B | Modena"

    val nextButtons = listOf(
        ModenaNextButton.Gaydon,
    )
}

sealed class ModenaNextButton(override val title: String) : NextButton {

    object Gaydon : ModenaNextButton("Gaydon (legacy fragment, external, no args)")
}
