package com.turo.nibel.sample.featureB.coventry

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turo.nibel.annotations.ImplementationType
import com.turo.nibel.annotations.UiEntry
import com.turo.nibel.runtime.LocalImplementationType
import com.turo.nibel.runtime.NavigationController
import com.turo.nibel.sample.common.CommonScreen
import com.turo.nibel.sample.common.SideEffectHandler
import com.turo.nibel.sample.featureB.modena.ModenaScreenEntry
import com.turo.nibel.sample.navigation.ModenaArgs
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize

@Parcelize
data class CoventryArgs(val inputText: String) : Parcelable

@UiEntry(
    type = ImplementationType.Composable,
    args = CoventryArgs::class
)
@Composable
fun CoventryScreen(
    args: CoventryArgs,
    navigator: NavigationController,
    viewModel: CoventryViewModel = hiltViewModel()
) {
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
private fun SideEffectHandler(sideEffects: Flow<CoventrySideEffect>) {
    SideEffectHandler(sideEffects) {
        when (it) {
            is CoventrySideEffect.NavigateBack -> navigateBack()
            is CoventrySideEffect.NavigateToModena -> navigateTo(
                ModenaScreenEntry.newInstance(ModenaArgs(inputText = it.inputText))
            )

            is CoventrySideEffect.NavigateToCoventry -> navigateTo(
                CoventryScreenEntry.newInstance(CoventryArgs(inputText = it.inputText))
            )
        }
    }
}
