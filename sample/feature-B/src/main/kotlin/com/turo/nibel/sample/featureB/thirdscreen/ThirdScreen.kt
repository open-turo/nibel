package com.turo.nibel.sample.featureB.thirdscreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turo.nibel.sample.common.CommonScreen
import com.turo.nibel.sample.common.SideEffectHandler
import com.turo.nibel.sample.navigation.FirstScreenDestination
import com.turo.nibel.sample.navigation.FourthArgs
import com.turo.nibel.sample.navigation.FourthScreenDestination
import com.turo.nibel.sample.navigation.ThirdScreenDestination
import kotlinx.coroutines.flow.Flow
import nibel.annotations.ImplementationType
import nibel.annotations.UiExternalEntry
import nibel.runtime.LocalImplementationType

@UiExternalEntry(
    type = ImplementationType.Composable,
    destination = ThirdScreenDestination::class
)
@Composable
fun ThirdScreen(viewModel: ThirdViewModel = hiltViewModel()) {
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
private fun SideEffectHandler(sideEffects: Flow<ThirdSideEffect>) {
    SideEffectHandler(sideEffects) {
        when (it) {
            is ThirdSideEffect.NavigateBack -> navigateBack()
            is ThirdSideEffect.NavigateToFourthScreen -> {
                val args = FourthArgs(it.inputText)
                navigateTo(FourthScreenDestination(args))
            }

            is ThirdSideEffect.NavigateToFirstScreen ->
                navigateTo(FirstScreenDestination)
        }
    }
}
