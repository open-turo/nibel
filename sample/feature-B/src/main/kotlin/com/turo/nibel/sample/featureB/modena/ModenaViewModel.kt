package com.turo.nibel.sample.featureB.modena

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.turo.nibel.runtime.getNibelArgs
import com.turo.nibel.sample.navigation.ModenaArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ModenaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _sideEffects = MutableSharedFlow<ModenaSideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<ModenaSideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(
        ModenaState(inputText = savedStateHandle.getNibelArgs<ModenaArgs>()!!.inputText)
    )
    val state: StateFlow<ModenaState> get() = _state

    fun onBack() {
        _sideEffects.tryEmit(ModenaSideEffect.NavigateBack)
    }

    fun onContinue(nextButton: ModenaNextButton) {
        when (nextButton) {
            ModenaNextButton.Gaydon ->
                _sideEffects.tryEmit(ModenaSideEffect.NavigateToGaydon)
        }
    }

    fun onInputTextChanged(inputText: String) {
        _state.value = _state.value.copy(inputText = inputText)
    }
}
