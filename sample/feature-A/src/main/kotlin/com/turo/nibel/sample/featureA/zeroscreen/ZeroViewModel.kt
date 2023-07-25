package com.turo.nibel.sample.featureA.zeroscreen

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ZeroViewModel @Inject constructor() : ViewModel() {

    private val _sideEffects = MutableSharedFlow<ZeroSideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<ZeroSideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(ZeroState(inputText = ""))
    val state: StateFlow<ZeroState> get() = _state

    fun onContinue(nextButton: DearbornNextButton) {
        when (nextButton) {
            DearbornNextButton.FirstScreen ->
                _sideEffects.tryEmit(ZeroSideEffect.NavigateToFirstScreen)

            DearbornNextButton.ThirdScreen ->
                _sideEffects.tryEmit(ZeroSideEffect.NavigateToThirdScreen)
        }
    }
}
