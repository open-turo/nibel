package com.turo.nibel.sample.featureB.modena

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nibel.annotations.ImplementationType
import nibel.annotations.UiExternalEntry
import nibel.runtime.LocalImplementationType
import com.turo.nibel.sample.common.CommonScreen
import com.turo.nibel.sample.common.SideEffectHandler
import com.turo.nibel.sample.navigation.GaydonDestination
import com.turo.nibel.sample.navigation.ModenaArgs
import com.turo.nibel.sample.navigation.ModenaDestination
import kotlinx.coroutines.flow.Flow

@UiExternalEntry(
    type = ImplementationType.Composable,
    destination = ModenaDestination::class
)
@Composable
fun ModenaScreen(args: ModenaArgs, viewModel: ModenaViewModel = hiltViewModel()) {
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
        inputTextEditable = false
    )
}

@Composable
private fun SideEffectHandler(sideEffects: Flow<ModenaSideEffect>) {
    SideEffectHandler(sideEffects) {
        when (it) {
            ModenaSideEffect.NavigateBack -> navigateBack()
            ModenaSideEffect.NavigateToGaydon -> navigateTo(GaydonDestination)
        }
    }
}
