package com.turo.nibel.sample.featureA.zeroscreen

sealed interface ZeroSideEffect {

    object NavigateToFirstScreen : ZeroSideEffect

    object NavigateToThirdScreen : ZeroSideEffect

    object NavigateToResultsDemo : ZeroSideEffect

    object PickColor : ZeroSideEffect
}
