package com.turo.nibel.sample.featureA.yokohama

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.runtime.LocalImplementationType
import com.turo.nibel.sample.common.CommonScreen
import com.turo.nibel.sample.common.SideEffectHandler
import com.turo.nibel.sample.featureA.molsheim.MolsheimArgs
import com.turo.nibel.sample.featureA.molsheim.MolsheimFragmentEntry
import com.turo.nibel.sample.navigation.ModenaArgs
import com.turo.nibel.sample.navigation.ModenaDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize

@Parcelize
data class YokohamaArgs(val inputText: String) : Parcelable

@UiEntry(
    type = ImplementationType.Fragment,
    args = YokohamaArgs::class
)
@Composable
fun YokohamaScreen(viewModel: YokohamaViewModel = hiltViewModel()) {
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
private fun SideEffectHandler(sideEffects: Flow<YokohamaSideEffect>) {
    SideEffectHandler(sideEffects) {
        when (it) {
            is YokohamaSideEffect.NavigateBack -> navigateBack()
            is YokohamaSideEffect.NavigateToMolsheim -> navigateTo(
                MolsheimFragmentEntry.newInstance(MolsheimArgs(inputText = it.inputText))
            )

            is YokohamaSideEffect.NavigateToModena -> navigateTo(
                ModenaDestination(ModenaArgs(inputText = it.inputText))
            )
        }
    }
}
