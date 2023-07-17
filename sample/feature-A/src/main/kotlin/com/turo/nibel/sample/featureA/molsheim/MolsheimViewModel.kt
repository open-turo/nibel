package com.turo.nibel.sample.featureA.molsheim

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
class MolsheimViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _sideEffects = MutableSharedFlow<MolsheimSideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<MolsheimSideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(
        MolsheimState(inputText = savedStateHandle.getNibelArgs<MolsheimArgs>()!!.inputText)
    )
    val state: StateFlow<MolsheimState> get() = _state

    fun onContinue(nextButton: MolsheimNextButton) = Unit
}
