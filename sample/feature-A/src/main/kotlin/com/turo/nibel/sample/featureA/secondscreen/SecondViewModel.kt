package com.turo.nibel.sample.featureA.secondscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.turo.nibel.sample.navigation.ThirdResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import nibel.runtime.getNibelArgs
import javax.inject.Inject

@HiltViewModel
class SecondViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _sideEffects = MutableSharedFlow<SecondSideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<SecondSideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(
        SecondState(inputText = savedStateHandle.getNibelArgs<SecondArgs>()!!.inputText),
    )
    val state: StateFlow<SecondState> get() = _state

    fun onBack() {
        _sideEffects.tryEmit(SecondSideEffect.NavigateBack)
    }

    fun onContinue(nextButton: SecondNextButton) {
        when (nextButton) {
            SecondNextButton.SecondScreen ->
                _sideEffects.tryEmit(SecondSideEffect.NavigateToThirdScreen(state.value.inputText))

            SecondNextButton.SecondScreenForResult ->
                _sideEffects.tryEmit(SecondSideEffect.NavigateForResultToThirdScreen(state.value.inputText))
        }
    }

    fun handleThirdScreenResult(result: ThirdResult?) {
        _sideEffects.tryEmit(SecondSideEffect.HandleThirdScreenResult(result))
    }

    fun onInputTextChanged(inputText: String) {
        _state.value = _state.value.copy(inputText = inputText)
    }
}
