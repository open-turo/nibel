package com.turo.nibel.sample.featureA.firstscreen

import com.turo.nibel.sample.common.NextButton
import com.turo.nibel.sample.featureA.photopicker.PhotoPickerResult

data class FirstState(
    val inputText: String,
    val selectedPhoto: PhotoPickerResult? = null,
) {
    val title: String = "Feature A | First Screen"

    val nextButtons = listOf(
        FirstNextButton.SecondScreen,
        FirstNextButton.PhotoPicker,
    )
}

sealed class FirstNextButton(override val title: String) : NextButton {
    object SecondScreen : FirstNextButton("Second Screen (type.composable, internal)")
    object PhotoPicker : FirstNextButton("Photo Picker (result navigation demo)")
}
