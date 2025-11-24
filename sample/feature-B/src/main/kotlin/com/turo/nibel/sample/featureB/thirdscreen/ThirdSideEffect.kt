package com.turo.nibel.sample.featureB.thirdscreen

import com.turo.nibel.sample.navigation.ThirdScreenResult

sealed interface ThirdSideEffect {

    object NavigateBack : ThirdSideEffect

    data class NavigateToFourthScreen(val inputText: String) : ThirdSideEffect

    object NavigateToFirstScreen : ThirdSideEffect

    data class SetResultAndNavigateBack(val result: ThirdScreenResult) : ThirdSideEffect

    object CancelResultAndNavigateBack : ThirdSideEffect
}
