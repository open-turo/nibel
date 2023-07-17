package com.turo.nibel.sample.featureA.dearborn

sealed interface DearbornSideEffect {

    object NavigateToYokohama : DearbornSideEffect

    object NavigateToStuttgart : DearbornSideEffect
}
