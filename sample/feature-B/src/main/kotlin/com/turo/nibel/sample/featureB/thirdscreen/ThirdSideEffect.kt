package com.turo.nibel.sample.featureB.thirdscreen

sealed interface ThirdSideEffect {

    object NavigateBack : ThirdSideEffect

    data class NavigateToFourthScreen(val inputText: String) : ThirdSideEffect

    object NavigateToFirstScreen : ThirdSideEffect
}
