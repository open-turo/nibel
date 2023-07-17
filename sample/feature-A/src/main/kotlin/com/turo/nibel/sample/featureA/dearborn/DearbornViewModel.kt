package com.turo.nibel.sample.featureA.dearborn

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DearbornViewModel @Inject constructor() : ViewModel() {

    private val _sideEffects = MutableSharedFlow<DearbornSideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<DearbornSideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(
        DearbornState(inputText = "")
    )
    val state: StateFlow<DearbornState> get() = _state

    fun onContinue(nextButton: DearbornNextButton) {
        when (nextButton) {
            DearbornNextButton.Yokohama ->
                _sideEffects.tryEmit(DearbornSideEffect.NavigateToYokohama)

            DearbornNextButton.Stuttgart ->
                _sideEffects.tryEmit(DearbornSideEffect.NavigateToStuttgart)
        }
    }
}
