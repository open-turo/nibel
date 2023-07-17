package com.turo.nibel.sample.featureA.yokohama

sealed interface YokohamaSideEffect {

    object NavigateBack : YokohamaSideEffect

    data class NavigateToMolsheim(val inputText: String) : YokohamaSideEffect

    data class NavigateToModena(val inputText: String) : YokohamaSideEffect

}
