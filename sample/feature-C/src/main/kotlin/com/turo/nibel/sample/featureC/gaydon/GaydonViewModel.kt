package com.turo.nibel.sample.featureC.gaydon

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class GaydonViewModel @Inject constructor() : ViewModel() {

    private val _sideEffects = MutableSharedFlow<GaydonSideEffect>(extraBufferCapacity = 1)
    val sideEffects: Flow<GaydonSideEffect> get() = _sideEffects

    private val _state = MutableStateFlow(
        GaydonState(inputText = "")
    )
    val state: StateFlow<GaydonState> get() = _state

    fun onContinue(nextButton: GaydonNextButton) = Unit
}
