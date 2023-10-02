@file:Suppress("UNUSED_PARAMETER", "TestFunctionName")

package nibel.tests.codegen

import androidx.compose.runtime.Composable
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.runtime.NavigationController

@UiEntry(ImplementationType.Fragment)
@Composable
fun FragmentEntryWithNoArgsWithParams(
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit

@UiEntry(ImplementationType.Composable)
@Composable
fun ComposableEntryWithNoArgsWithParams(
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit

@UiEntry(ImplementationType.Fragment, TestArgs::class)
@Composable
fun FragmentEntryWithArgsWithParams(
    args: TestArgs,
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit

@UiEntry(ImplementationType.Composable, TestArgs::class)
@Composable
fun ComposableEntryWithArgsWithParams(
    args: TestArgs,
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit
