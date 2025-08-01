@file:Suppress("UNUSED_PARAMETER", "TestFunctionName")

package nibel.tests.codegen

import androidx.compose.runtime.Composable
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.runtime.NavigationController

/**
 * Compilation tests for @UiEntry annotation with additional parameters (internal entries).
 *
 * These tests verify that the KSP annotation processor can successfully generate entry classes
 * for internal entries that have additional Composable function parameters beyond args.
 * The processor should properly handle:
 * - Navigation controller injection
 * - Implementation type parameters
 * - Parameters with default values
 * - Mixed parameter types in generated ComposableContent() calls
 *
 * Test coverage:
 * - Fragment/Composable entries with and without args + additional parameters
 * - Proper parameter passing in generated ComposableContent() implementations
 * - Support for default parameter values
 *
 * The tests pass if the annotated functions compile successfully and generate
 * Entry classes that properly inject the additional parameters.
 */

/**
 * Tests Fragment-based internal entry without args but with additional parameters.
 * Should generate proper ComposableContent() that provides navigator, type, and default values
 */
@UiEntry(ImplementationType.Fragment)
@Composable
fun FragmentEntryWithNoArgsWithParams(
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit

/**
 * Tests Composable-based internal entry without args but with additional parameters.
 * Should generate proper ComposableContent() that provides navigator, type, and default values
 */
@UiEntry(ImplementationType.Composable)
@Composable
fun ComposableEntryWithNoArgsWithParams(
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit

/**
 * Tests Fragment-based internal entry with args and additional parameters.
 * Should generate proper ComposableContent() that provides args, navigator, type, and default values
 */
@UiEntry(ImplementationType.Fragment, TestArgs::class)
@Composable
fun FragmentEntryWithArgsWithParams(
    args: TestArgs,
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit

/**
 * Tests Composable-based internal entry with args and additional parameters.
 * Should generate proper ComposableContent() that provides args, navigator, type, and default values
 */
@UiEntry(ImplementationType.Composable, TestArgs::class)
@Composable
fun ComposableEntryWithArgsWithParams(
    args: TestArgs,
    navigator: NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit
