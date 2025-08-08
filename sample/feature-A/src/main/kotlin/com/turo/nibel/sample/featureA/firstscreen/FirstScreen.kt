package com.turo.nibel.sample.featureA.firstscreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turo.nibel.sample.common.CommonScreen
import com.turo.nibel.sample.common.SideEffectHandler
import com.turo.nibel.sample.featureA.secondscreen.SecondArgs
import com.turo.nibel.sample.featureA.secondscreen.SecondScreenEntry
import com.turo.nibel.sample.navigation.FirstScreenDestination
import kotlinx.coroutines.flow.Flow
import nibel.annotations.ImplementationType
import nibel.annotations.UiExternalEntry
import nibel.runtime.LocalImplementationType

@UiExternalEntry(
    type = ImplementationType.Fragment,
    destination = FirstScreenDestination::class,
)
@Composable
fun FirstScreen(viewModel: FirstViewModel = hiltViewModel()) {
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
    )
}

@Composable
private fun SideEffectHandler(sideEffects: Flow<FirstSideEffect>) {
    SideEffectHandler(sideEffects) {
        when (it) {
            is FirstSideEffect.NavigateBack -> navigateBack()
            is FirstSideEffect.NavigateToSecondScreen -> {
                val args = SecondArgs(it.inputText)
                navigateTo(SecondScreenEntry.newInstance(args))
            }
        }
    }
}
