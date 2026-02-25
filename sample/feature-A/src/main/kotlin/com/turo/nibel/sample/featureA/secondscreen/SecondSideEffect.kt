package com.turo.nibel.sample.featureA.secondscreen

sealed interface SecondSideEffect {

    object NavigateBack : SecondSideEffect

    data class NavigateToThirdScreen(val inputText: String) : SecondSideEffect

    object NavigateToExternalContentDemo : SecondSideEffect
}
