package com.turo.nibel.sample.featureB.coventry

import com.turo.nibel.sample.common.NextButton

data class CoventryState(
    val inputText: String
) {
    val title: String = "Feature B | Coventry"

    val nextButtons = listOf(
        CoventryNextButton.Modena,
        CoventryNextButton.Coventry,
    )
}

sealed class CoventryNextButton(override val title: String) : NextButton {

    object Modena : CoventryNextButton("Modena (composable, internal, args)")

    object Coventry : CoventryNextButton("Coventry (composable, internal, args)")
}
