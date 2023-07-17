package com.turo.nibel.sample.featureB.coventry

sealed interface CoventrySideEffect {

    object NavigateBack : CoventrySideEffect

    data class NavigateToModena(val inputText: String) : CoventrySideEffect

    data class NavigateToCoventry(val inputText: String) : CoventrySideEffect
}
