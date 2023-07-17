package com.turo.nibel.sample.featureA.molsheim

import com.turo.nibel.sample.common.NextButton

data class MolsheimState(
    val inputText: String
) {
    val title: String = "Feature A | Molsheim"

    val nextButtons = listOf<MolsheimNextButton>()
}

sealed class MolsheimNextButton(override val title: String) : NextButton
