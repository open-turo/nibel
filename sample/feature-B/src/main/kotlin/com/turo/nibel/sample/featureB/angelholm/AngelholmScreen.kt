package com.turo.nibel.sample.featureB.angelholm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.runtime.LocalImplementationType
import com.turo.nibel.sample.common.CommonScreen
import com.turo.nibel.sample.common.SideEffectHandler
import kotlinx.coroutines.flow.Flow

@UiEntry(type = ImplementationType.Composable)
@Composable
fun AngelholmScreen(viewModel: AngelholmViewModel = hiltViewModel()) {
    SideEffectHandler(viewModel.sideEffects)

    val state by viewModel.state.collectAsStateWithLifecycle()

    CommonScreen(
        title = state.title,
        inputText = state.inputText,
        implementationType = LocalImplementationType.current!!,
        onBack = viewModel::onBack,
        nextButtons = state.nextButtons,
        onContinue = viewModel::onContinue,
        onInputTextChanged = viewModel::onInputTextChanged,
        inputTextEditable = false,
        inputTextVisible = false
    )
}

@Composable
private fun SideEffectHandler(sideEffects: Flow<AngelholmSideEffect>) {
    SideEffectHandler(sideEffects) {
        when (it) {
            AngelholmSideEffect.NavigateBack -> navigateBack()
        }
    }
}
