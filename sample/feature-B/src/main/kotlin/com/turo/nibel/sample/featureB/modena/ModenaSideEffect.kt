package com.turo.nibel.sample.featureB.modena

sealed interface ModenaSideEffect {

    object NavigateBack : ModenaSideEffect

    object NavigateToGaydon : ModenaSideEffect
}
