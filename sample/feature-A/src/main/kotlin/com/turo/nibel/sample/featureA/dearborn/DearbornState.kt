package com.turo.nibel.sample.featureA.dearborn

import com.turo.nibel.sample.common.NextButton

data class DearbornState(
    val inputText: String
) {
    val title: String = "Feature A | Dearborn"

    val nextButtons = listOf(
        DearbornNextButton.Yokohama,
        DearbornNextButton.Stuttgart,
    )
}

sealed class DearbornNextButton(override val title: String) : NextButton {
    object Yokohama : DearbornNextButton("Yokohama (composable fragment, internal, args)")
    object Stuttgart : DearbornNextButton("Stuttgart (composable fragment, external, no args)")
}
