package com.turo.nibel.sample.featureA.firstscreen

sealed interface FirstSideEffect {

    object NavigateBack : FirstSideEffect

    data class NavigateToSecondScreen(val inputText: String) : FirstSideEffect

    object NavigateToPhotoPicker : FirstSideEffect
}
