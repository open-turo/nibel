package com.turo.nibel.sample.featureB.thirdscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.turo.nibel.sample.navigation.ThirdArgs
import com.turo.nibel.sample.navigation.ThirdScreenResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import nibel.runtime.getNibelArgs
import javax.inject.Inject

@HiltViewModel
class ThirdViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _sideEffects = MutableSharedFlow<ThirdSideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<ThirdSideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(
        ThirdState(inputText = savedStateHandle.getNibelArgs<ThirdArgs>()!!.inputText),
    )
    val state: StateFlow<ThirdState> get() = _state

    fun onBack() {
        _sideEffects.tryEmit(ThirdSideEffect.NavigateBack)
    }

    fun onContinue(nextButton: ThirdNextButton) {
        when (nextButton) {
            ThirdNextButton.ReturnResult -> {
                val result = ThirdScreenResult(
                    inputText = state.value.inputText,
                )
                _sideEffects.tryEmit(ThirdSideEffect.SetResultAndNavigateBack(result))
            }

            ThirdNextButton.FourthScreen ->
                _sideEffects.tryEmit(ThirdSideEffect.NavigateToFourthScreen(state.value.inputText))

            ThirdNextButton.FirstScreen ->
                _sideEffects.tryEmit(ThirdSideEffect.NavigateToFirstScreen)
        }
    }

    fun onInputTextChanged(inputText: String) {
        _state.value = _state.value.copy(inputText = inputText)
    }
}
