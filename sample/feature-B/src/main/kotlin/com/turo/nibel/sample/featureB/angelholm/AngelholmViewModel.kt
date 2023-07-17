package com.turo.nibel.sample.featureB.angelholm

import androidx.lifecycle.ViewModel
import com.turo.nibel.sample.featureB.angelholm.AngelholmNextButton
import com.turo.nibel.sample.featureB.angelholm.AngelholmSideEffect
import com.turo.nibel.sample.featureB.angelholm.AngelholmState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AngelholmViewModel @Inject constructor() : ViewModel() {
    private val _sideEffects = MutableSharedFlow<AngelholmSideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<AngelholmSideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(
        AngelholmState(inputText = "")
    )
    val state: StateFlow<AngelholmState> get() = _state

    fun onBack() {
        _sideEffects.tryEmit(AngelholmSideEffect.NavigateBack)
    }

    fun onContinue(nextButton: AngelholmNextButton) = Unit

    fun onInputTextChanged(inputText: String) {
        _state.value = _state.value.copy(inputText = inputText)
    }
}
