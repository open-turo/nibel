package com.turo.nibel.sample.featureC.fourthscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.turo.nibel.sample.navigation.FourthArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import nibel.runtime.getNibelArgs
import javax.inject.Inject

@HiltViewModel
class FourthViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _sideEffects = MutableSharedFlow<FourthSideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<FourthSideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(
        FourthState(inputText = savedStateHandle.getNibelArgs<FourthArgs>()!!.inputText)
    )
    val state: StateFlow<FourthState> get() = _state

    fun onContinue(nextButton: FourthNextButton) = Unit
}
