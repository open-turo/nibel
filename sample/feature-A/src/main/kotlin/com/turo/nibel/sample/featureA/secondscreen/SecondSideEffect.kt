package com.turo.nibel.sample.featureA.secondscreen

import com.turo.nibel.sample.navigation.ThirdScreenResult

sealed interface SecondSideEffect {

    object NavigateBack : SecondSideEffect

    data class NavigateToThirdScreen(
        val inputText: String,
        val callback: (ThirdScreenResult?) -> Unit,
    ) : SecondSideEffect
}
