package com.turo.nibel.sample.featureA.yokohama

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.turo.nibel.runtime.getNibelArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class YokohamaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _sideEffects = MutableSharedFlow<YokohamaSideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<YokohamaSideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(
        YokohamaState(inputText = savedStateHandle.getNibelArgs<YokohamaArgs>()!!.inputText)
    )
    val state: StateFlow<YokohamaState> get() = _state

    fun onBack() {
        _sideEffects.tryEmit(YokohamaSideEffect.NavigateBack)
    }

    fun onContinue(nextButton: YokohamaNextButton) {
        when (nextButton) {
            YokohamaNextButton.Molsheim ->
                _sideEffects.tryEmit(YokohamaSideEffect.NavigateToMolsheim(state.value.inputText))

            YokohamaNextButton.Modena ->
                _sideEffects.tryEmit(YokohamaSideEffect.NavigateToModena(state.value.inputText))
        }
    }

    fun onInputTextChanged(inputText: String) {
        _state.value = _state.value.copy(inputText = inputText)
    }
}
