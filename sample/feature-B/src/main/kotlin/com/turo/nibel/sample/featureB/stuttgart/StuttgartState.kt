package com.turo.nibel.sample.featureB.stuttgart

import com.turo.nibel.sample.common.NextButton

data class StuttgartState(
    val inputText: String
) {
    val title: String = "Feature B | Stuttgart"

    val nextButtons = listOf(
        StuttgartNextButton.Coventry,
        StuttgartNextButton.Angelholm,
    )
}

sealed class StuttgartNextButton(override val title: String) : NextButton {

    object Coventry : StuttgartNextButton("Coventry (composable, internal, args)")

    object Angelholm : StuttgartNextButton("Ängelholm (composable, internal, args)")
}
