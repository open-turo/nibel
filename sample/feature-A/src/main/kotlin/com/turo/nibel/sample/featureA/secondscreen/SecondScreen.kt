package com.turo.nibel.sample.featureA.secondscreen

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turo.nibel.sample.common.CommonScreen
import com.turo.nibel.sample.common.SideEffectHandler
import com.turo.nibel.sample.navigation.ThirdArgs
import com.turo.nibel.sample.navigation.ThirdScreenDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.runtime.LocalImplementationType

@UiEntry(
    type = ImplementationType.Composable,
    args = SecondArgs::class,
)
@Composable
fun SecondScreen(viewModel: SecondViewModel = hiltViewModel()) {
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
private fun SideEffectHandler(sideEffects: Flow<SecondSideEffect>) {
    SideEffectHandler(sideEffects) {
        when (it) {
            is SecondSideEffect.NavigateBack -> navigateBack()
            is SecondSideEffect.NavigateToThirdScreen -> {
                val args = ThirdArgs(it.inputText)
                navigateForResult(
                    externalDestination = ThirdScreenDestination(args),
                    callback = it.callback,
                )
            }
        }
    }
}

@Parcelize
data class SecondArgs(val inputText: String) : Parcelable
