package com.turo.nibel.sample.featureC.gaydon

import com.turo.nibel.sample.common.NextButton

data class GaydonState(
    val inputText: String
) {
    val title: String = "Feature C | Gaydon"

    val nextButtons = listOf<GaydonNextButton>()
}

sealed class GaydonNextButton(override val title: String) : NextButton
