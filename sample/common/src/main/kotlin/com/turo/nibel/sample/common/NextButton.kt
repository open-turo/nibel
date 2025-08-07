package com.turo.nibel.sample.common

interface NextButton {
    val title: String
}

data class GenericNextButton(override val title: String) : NextButton
