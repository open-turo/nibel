package com.turo.nibel.sample.featureB.stuttgart

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class StuttgartViewModel @Inject constructor() : ViewModel() {
    private val _sideEffects = MutableSharedFlow<StuttgartSideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<StuttgartSideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(
        StuttgartState(inputText = "")
    )
    val state: StateFlow<StuttgartState> get() = _state

    fun onBack() {
        _sideEffects.tryEmit(StuttgartSideEffect.NavigateBack)
    }

    fun onContinue(nextButton: StuttgartNextButton) {
        when (nextButton) {
            StuttgartNextButton.Coventry ->
                _sideEffects.tryEmit(StuttgartSideEffect.NavigateToCoventry(state.value.inputText))

            StuttgartNextButton.Angelholm ->
                _sideEffects.tryEmit(StuttgartSideEffect.NavigateToAngelholm(state.value.inputText))
        }
    }

    fun onInputTextChanged(inputText: String) {
        _state.value = _state.value.copy(inputText = inputText)
    }
}
