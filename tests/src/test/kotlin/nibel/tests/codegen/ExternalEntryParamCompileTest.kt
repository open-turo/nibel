@file:Suppress("UNUSED_PARAMETER", "TestFunctionName")

package nibel.tests.codegen

import androidx.compose.runtime.Composable
import nibel.annotations.DestinationWithArgs
import nibel.annotations.DestinationWithNoArgs
import nibel.annotations.ImplementationType
import nibel.annotations.UiExternalEntry
import nibel.runtime.NavigationController

object ParamsDestination1 : DestinationWithNoArgs

@UiExternalEntry(ImplementationType.Fragment, ParamsDestination1::class)
@Composable
fun FragmentExternalEntryWithNoArgsWithParams(
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit


object ParamsDestination2 : DestinationWithNoArgs

@UiExternalEntry(ImplementationType.Composable, ParamsDestination2::class)
@Composable
fun ComposableExternalEntryWithNoArgsWithParams(
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit


data class ParamsDestination3(override val args: TestArgs) : DestinationWithArgs<TestArgs>

@UiExternalEntry(ImplementationType.Fragment, ParamsDestination3::class)
@Composable
fun FragmentExternalEntryWithArgsWithParams(
    args: TestArgs,
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit

data class ParamsDestination4(override val args: TestArgs) : DestinationWithArgs<TestArgs>

@UiExternalEntry(ImplementationType.Composable, ParamsDestination4::class)
@Composable
fun ComposableExternalEntryWithArgsWithParams(
    args: TestArgs,
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit
