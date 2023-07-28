@file:Suppress("TestFunctionName")

package nibel.tests.codegen

import androidx.compose.runtime.Composable
import nibel.annotations.DestinationWithArgs
import nibel.annotations.DestinationWithNoArgs
import nibel.annotations.ImplementationType
import nibel.annotations.UiExternalEntry

object Destination1 : DestinationWithNoArgs

@UiExternalEntry(ImplementationType.Fragment, Destination1::class)
@Composable
fun FragmentExternalEntryWithNoArgs() = Unit


data class Destination2(override val args: TestArgs) : DestinationWithArgs<TestArgs>

@UiExternalEntry(ImplementationType.Fragment, Destination2::class)
@Composable
fun FragmentExternalEntryWithArgs() = Unit


object Destination3 : DestinationWithNoArgs

@UiExternalEntry(ImplementationType.Composable, Destination3::class)
@Composable
fun ComposableExternalEntryWithNoArgs() = Unit


data class Destination4(override val args: TestArgs) : DestinationWithArgs<TestArgs>

@UiExternalEntry(ImplementationType.Composable, Destination4::class)
@Composable
fun ComposableExternalEntryWithArgs() = Unit


object Destination5 : DestinationWithNoArgs
