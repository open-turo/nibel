@file:Suppress("UNUSED_PARAMETER", "TestFunctionName")

package nibel.tests.codegen

import androidx.compose.runtime.Composable
import nibel.annotations.DestinationWithArgs
import nibel.annotations.DestinationWithNoArgs
import nibel.annotations.ImplementationType
import nibel.annotations.UiExternalEntry
import nibel.runtime.NavigationController

/**
 * Compilation tests for @UiExternalEntry annotation with additional parameters (external entries).
 *
 * These tests verify that the KSP annotation processor can successfully generate entry classes
 * for external entries that have additional Composable function parameters beyond args.
 * The processor should properly handle:
 * - Navigation controller injection in cross-module scenarios
 * - Implementation type parameters for external entries
 * - Parameters with default values in external entry contexts
 * - Mixed parameter types in generated ComposableContent() calls for external entries
 *
 * Test coverage:
 * - External Fragment/Composable entries with and without args + additional parameters
 * - Proper parameter passing in generated ComposableContent() for external entries
 * - Cross-module parameter injection and factory method generation
 *
 * The tests pass if the annotated functions compile successfully and generate
 * Entry classes with proper external factory companions that inject additional parameters.
 */

/**
 * Destination for Fragment external entry without args but with parameters
 */
object ParamsDestination1 : DestinationWithNoArgs

/**
 * Tests Fragment-based external entry without args but with additional parameters.
 * Should generate factory companion and proper ComposableContent() with parameter injection
 */
@UiExternalEntry(ImplementationType.Fragment, ParamsDestination1::class)
@Composable
fun FragmentExternalEntryWithNoArgsWithParams(
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit

/**
 * Destination for Composable external entry without args but with parameters
 */
object ParamsDestination2 : DestinationWithNoArgs

/**
 * Tests Composable-based external entry without args but with additional parameters.
 * Should generate factory companion and proper ComposableContent() with parameter injection
 */
@UiExternalEntry(ImplementationType.Composable, ParamsDestination2::class)
@Composable
fun ComposableExternalEntryWithNoArgsWithParams(
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit

/**
 * Destination for Fragment external entry with args and parameters
 */
data class ParamsDestination3(override val args: TestArgs) : DestinationWithArgs<TestArgs>

/**
 * Tests Fragment-based external entry with args and additional parameters.
 * Should generate factory companion and proper ComposableContent() with args and parameter injection
 */
@UiExternalEntry(ImplementationType.Fragment, ParamsDestination3::class)
@Composable
fun FragmentExternalEntryWithArgsWithParams(
    args: TestArgs,
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit

/**
 * Destination for Composable external entry with args and parameters
 */
data class ParamsDestination4(override val args: TestArgs) : DestinationWithArgs<TestArgs>

/**
 * Tests Composable-based external entry with args and additional parameters.
 * Should generate factory companion and proper ComposableContent() with args and parameter injection
 */
@UiExternalEntry(ImplementationType.Composable, ParamsDestination4::class)
@Composable
fun ComposableExternalEntryWithArgsWithParams(
    args: TestArgs,
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit
