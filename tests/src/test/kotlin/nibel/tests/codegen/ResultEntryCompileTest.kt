@file:Suppress("TestFunctionName")

package nibel.tests.codegen

import androidx.compose.runtime.Composable
import nibel.annotations.DestinationWithArgs
import nibel.annotations.DestinationWithNoArgs
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.annotations.UiExternalEntry

/**
 * Compilation tests for result-based navigation entries.
 *
 * These tests verify that the KSP annotation processor can successfully generate entry classes
 * that support result-based navigation using the Activity Result API pattern. Result entries
 * implement both the base Entry interface and the ResultEntry<R> interface.
 *
 * Key features being tested:
 * - Internal result entries with proper ResultEntry<R> interface implementation
 * - External result entries with correct factory method return types
 * - Type-safe result handling without unsafe casts
 * - Proper resultType property generation for runtime type information
 * - Factory methods returning ResultEntry<R> instead of ComposableEntry<Args>
 *
 * Test coverage:
 * - Internal Fragment/Composable result entries with and without arguments
 * - External Fragment/Composable result entries with and without arguments
 * - Result entries with additional parameters
 * - Proper factory method type safety
 *
 * The tests pass if the annotated functions compile successfully and generate
 * Entry classes that implement ResultEntry<R> with correct factory return types.
 */

// =============================================================================
// Internal Result Entries
// =============================================================================

/**
 * Tests Fragment-based internal result entry without arguments.
 * Should generate: FragmentResultEntryWithNoArgsEntry extending FragmentEntry
 * and implementing ResultEntry<TestResult>
 */
@UiEntry(type = ImplementationType.Fragment, result = TestResult::class)
@Composable
fun FragmentResultEntryWithNoArgs() = Unit

/**
 * Tests Fragment-based internal result entry with arguments.
 * Should generate: FragmentResultEntryWithArgsEntry extending FragmentEntry
 * with TestArgs parameter and implementing ResultEntry<TestResult>
 */
@UiEntry(type = ImplementationType.Fragment, args = TestArgs::class, result = TestResult::class)
@Composable
fun FragmentResultEntryWithArgs() = Unit

/**
 * Tests Composable-based internal result entry without arguments.
 * Should generate: ComposableResultEntryWithNoArgsEntry extending ComposableEntry<Parcelable>
 * and implementing ResultEntry<TestResult> with newInstance(): ResultEntry<TestResult>
 */
@UiEntry(type = ImplementationType.Composable, result = TestResult::class)
@Composable
fun ComposableResultEntryWithNoArgs() = Unit

/**
 * Tests Composable-based internal result entry with arguments.
 * Should generate: ComposableResultEntryWithArgsEntry extending ComposableEntry<TestArgs>
 * and implementing ResultEntry<TestResult> with newInstance(TestArgs): ResultEntry<TestResult>
 */
@UiEntry(type = ImplementationType.Composable, args = TestArgs::class, result = TestResult::class)
@Composable
fun ComposableResultEntryWithArgs() = Unit

// =============================================================================
// External Result Entries
// =============================================================================

/**
 * Destination for Composable external result entry without arguments
 */
object ComposableResultDestination1 : DestinationWithNoArgs

/**
 * Tests Composable-based external result entry without arguments.
 * Should generate: ComposableExternalResultEntryWithNoArgsEntry extending ComposableEntry<*>
 * and implementing ResultEntry<TestResult> with factory newInstance(): ResultEntry<TestResult>
 */
@UiExternalEntry(type = ImplementationType.Composable, destination = ComposableResultDestination1::class, result = TestResult::class)
@Composable
fun ComposableExternalResultEntryWithNoArgs() = Unit

/**
 * Destination for Composable external result entry with arguments
 */
data class ComposableResultDestination2(override val args: TestArgs) : DestinationWithArgs<TestArgs>

/**
 * Tests Composable-based external result entry with arguments.
 * Should generate: ComposableExternalResultEntryWithArgsEntry extending ComposableEntry<TestArgs>
 * and implementing ResultEntry<TestResult> with factory newInstance(TestArgs): ResultEntry<TestResult>
 */
@UiExternalEntry(type = ImplementationType.Composable, destination = ComposableResultDestination2::class, result = TestResult::class)
@Composable
fun ComposableExternalResultEntryWithArgs() = Unit

/**
 * Destination for Fragment external result entry without arguments
 */
object FragmentResultDestination1 : DestinationWithNoArgs

/**
 * Tests Fragment-based external result entry without arguments.
 * Should generate: FragmentExternalResultEntryWithNoArgsEntry extending FragmentEntry
 * and implementing ResultEntry<TestResult> with proper factory companion
 */
@UiExternalEntry(type = ImplementationType.Fragment, destination = FragmentResultDestination1::class, result = TestResult::class)
@Composable
fun FragmentExternalResultEntryWithNoArgs() = Unit

/**
 * Destination for Fragment external result entry with arguments
 */
data class FragmentResultDestination2(override val args: TestArgs) : DestinationWithArgs<TestArgs>

/**
 * Tests Fragment-based external result entry with arguments.
 * Should generate: FragmentExternalResultEntryWithArgsEntry extending FragmentEntry
 * with TestArgs parameter and implementing ResultEntry<TestResult> with proper factory companion
 */
@UiExternalEntry(type = ImplementationType.Fragment, destination = FragmentResultDestination2::class, result = TestResult::class)
@Composable
fun FragmentExternalResultEntryWithArgs() = Unit

// =============================================================================
// Result Entries with Parameters
// =============================================================================

/**
 * Tests Composable-based internal result entry with additional parameters.
 * Should generate proper ComposableContent() that provides navigation, type, and result capabilities
 */
@UiEntry(type = ImplementationType.Composable, result = TestResult::class)
@Composable
fun ComposableResultEntryWithParams(
    navigator: nibel.runtime.NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit

/**
 * Tests Composable-based internal result entry with args and additional parameters.
 * Should generate proper ComposableContent() with args, navigation, type, and result capabilities
 */
@UiEntry(type = ImplementationType.Composable, args = TestArgs::class, result = TestResult::class)
@Composable
fun ComposableResultEntryWithArgsAndParams(
    args: TestArgs,
    navigator: nibel.runtime.NavigationController,
    type: ImplementationType,
    paramWithDefaultValue: String = ""
) = Unit
