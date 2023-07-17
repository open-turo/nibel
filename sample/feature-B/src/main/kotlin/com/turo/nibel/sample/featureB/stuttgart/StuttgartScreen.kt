package com.turo.nibel.sample.featureB.stuttgart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turo.nibel.annotations.ImplementationType
import com.turo.nibel.annotations.UiExternalEntry
import com.turo.nibel.runtime.LocalImplementationType
import com.turo.nibel.sample.common.CommonScreen
import com.turo.nibel.sample.common.SideEffectHandler
import com.turo.nibel.sample.featureB.angelholm.AngelholmScreenEntry
import com.turo.nibel.sample.featureB.coventry.CoventryArgs
import com.turo.nibel.sample.featureB.coventry.CoventryScreenEntry
import com.turo.nibel.sample.navigation.StuttgartDestination
import kotlinx.coroutines.flow.Flow

@UiExternalEntry(
    type = ImplementationType.Fragment,
    destination = StuttgartDestination::class,
)
@Composable
fun StuttgartScreen(viewModel: StuttgartViewModel = hiltViewModel()) {
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
private fun SideEffectHandler(sideEffects: Flow<StuttgartSideEffect>) {
    SideEffectHandler(sideEffects) {
        when (it) {
            is StuttgartSideEffect.NavigateBack -> navigateBack()
            is StuttgartSideEffect.NavigateToCoventry -> navigateTo(
                CoventryScreenEntry.newInstance(CoventryArgs(inputText = it.inputText))
            )

            is StuttgartSideEffect.NavigateToAngelholm -> navigateTo(
                AngelholmScreenEntry.newInstance()
            )
        }
    }
}
