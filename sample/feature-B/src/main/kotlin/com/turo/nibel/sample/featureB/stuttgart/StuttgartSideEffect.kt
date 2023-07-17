package com.turo.nibel.sample.featureB.stuttgart

sealed interface StuttgartSideEffect {

    object NavigateBack : StuttgartSideEffect

    data class NavigateToCoventry(val inputText: String) : StuttgartSideEffect

    data class NavigateToAngelholm(val inputText: String) : StuttgartSideEffect
}
