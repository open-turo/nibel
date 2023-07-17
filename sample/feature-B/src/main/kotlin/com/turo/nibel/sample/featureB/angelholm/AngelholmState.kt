package com.turo.nibel.sample.featureB.angelholm

import com.turo.nibel.sample.common.NextButton

data class AngelholmState(
    val inputText: String
) {
    val title: String = "Feature B | Ängelholm"

    val nextButtons = listOf<AngelholmNextButton>()
}

sealed class AngelholmNextButton(override val title: String) : NextButton
