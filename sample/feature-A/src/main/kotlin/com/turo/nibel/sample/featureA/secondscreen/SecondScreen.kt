package com.turo.nibel.sample.featureA.secondscreen

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turo.nibel.sample.common.CommonScreen
import com.turo.nibel.sample.common.SideEffectHandler
import com.turo.nibel.sample.navigation.ThirdArgs
import com.turo.nibel.sample.navigation.ThirdResult
import com.turo.nibel.sample.navigation.ThirdScreenDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.runtime.LocalImplementationType
import nibel.runtime.NavigationController

@UiEntry(
    type = ImplementationType.Composable,
    args = SecondArgs::class,
)
@Composable
fun SecondScreen(
    navigator: NavigationController,
    viewModel: SecondViewModel = hiltViewModel(),
) {
    SideEffectHandler(viewModel.sideEffects, navigator)

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
private fun SideEffectHandler(
    sideEffects: Flow<SecondSideEffect>,
    navigator: NavigationController,
) {
    SideEffectHandler(sideEffects) {
        when (it) {
            is SecondSideEffect.NavigateBack -> navigator.navigateBack()
            is SecondSideEffect.NavigateToThirdScreen -> {
                val args = ThirdArgs(it.inputText)
                navigator.navigateTo(ThirdScreenDestination(args))
            }
            is SecondSideEffect.NavigateForResultToThirdScreen -> {
                val args = ThirdArgs(it.inputText)
                navigator.navigateForResult(
                    destination = ThirdScreenDestination(args),
                    callback = { result: ThirdResult? ->
                        // Handle the result here - could update state or trigger another side effect
                        println("Received result from ThirdScreen: $result")
                    },
                )
            }
            is SecondSideEffect.HandleThirdScreenResult -> {
                // Handle the received result (could update UI state, show toast, etc.)
                println("Handling third screen result: ${it.result}")
            }
        }
    }
}

@Parcelize
data class SecondArgs(val inputText: String) : Parcelable
