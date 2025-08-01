package com.turo.nibel.sample.featureA.firstscreen

import com.turo.nibel.sample.featureA.photopicker.PhotoPickerResult

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class FirstViewModel @Inject constructor() : ViewModel() {
    private val _sideEffects = MutableSharedFlow<FirstSideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<FirstSideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(FirstState(inputText = ""))
    val state: StateFlow<FirstState> get() = _state

    fun onBack() {
        _sideEffects.tryEmit(FirstSideEffect.NavigateBack)
    }

    fun onContinue(nextButton: FirstNextButton) {
        when (nextButton) {
            FirstNextButton.SecondScreen ->
                _sideEffects.tryEmit(FirstSideEffect.NavigateToSecondScreen(state.value.inputText))
            FirstNextButton.PhotoPicker ->
                _sideEffects.tryEmit(FirstSideEffect.NavigateToPhotoPicker)
        }
    }

    fun onInputTextChanged(inputText: String) {
        _state.value = _state.value.copy(inputText = inputText)
    }

    fun onPhotoSelected(photo: PhotoPickerResult?) {
        _state.value = _state.value.copy(selectedPhoto = photo)
    }
}
