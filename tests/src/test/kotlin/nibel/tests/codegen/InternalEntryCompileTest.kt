@file:Suppress("TestFunctionName")

package nibel.tests.codegen

import androidx.compose.runtime.Composable
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry


@UiEntry(ImplementationType.Fragment)
@Composable
fun FragmentEntryWithNoArgs() = Unit

@UiEntry(ImplementationType.Fragment, TestArgs::class)
@Composable
fun FragmentEntryWithArgs() = Unit

@UiEntry(ImplementationType.Composable)
@Composable
fun ComposableEntryWithNoArgs() = Unit

@UiEntry(ImplementationType.Composable, TestArgs::class)
@Composable
fun ComposableEntryWithArgs() = Unit
