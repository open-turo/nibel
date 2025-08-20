package com.turo.nibel.sample.featureA.secondscreen

import com.turo.nibel.sample.navigation.ThirdResult

sealed interface SecondSideEffect {

    object NavigateBack : SecondSideEffect

    data class NavigateToThirdScreen(val inputText: String) : SecondSideEffect

    data class NavigateForResultToThirdScreen(val inputText: String) : SecondSideEffect

    data class HandleThirdScreenResult(val result: ThirdResult?) : SecondSideEffect
}
