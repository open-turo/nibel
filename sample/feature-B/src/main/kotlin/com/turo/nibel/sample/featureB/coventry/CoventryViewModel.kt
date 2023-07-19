package com.turo.nibel.sample.featureB.coventry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import nibel.runtime.getNibelArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CoventryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _sideEffects = MutableSharedFlow<CoventrySideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<CoventrySideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(
        CoventryState(inputText = savedStateHandle.getNibelArgs<CoventryArgs>()!!.inputText)
    )
    val state: StateFlow<CoventryState> get() = _state

    fun onBack() {
        _sideEffects.tryEmit(CoventrySideEffect.NavigateBack)
    }

    fun onContinue(nextButton: CoventryNextButton) {
        when (nextButton) {
            CoventryNextButton.Modena ->
                _sideEffects.tryEmit(CoventrySideEffect.NavigateToModena(state.value.inputText))

            CoventryNextButton.Coventry ->
                _sideEffects.tryEmit(CoventrySideEffect.NavigateToCoventry(state.value.inputText))
        }
    }

    fun onInputTextChanged(inputText: String) {
        _state.value = _state.value.copy(inputText = inputText)
    }
}
