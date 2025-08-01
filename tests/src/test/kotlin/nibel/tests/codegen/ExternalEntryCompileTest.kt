@file:Suppress("TestFunctionName")

package nibel.tests.codegen

import androidx.compose.runtime.Composable
import nibel.annotations.DestinationWithArgs
import nibel.annotations.DestinationWithNoArgs
import nibel.annotations.ImplementationType
import nibel.annotations.UiExternalEntry

/**
 * Compilation tests for @UiExternalEntry annotation (external entries).
 *
 * These tests verify that the KSP annotation processor can successfully generate entry classes
 * for external (inter-module) navigation targets. External entries are defined in one module
 * but can be used from other modules, requiring explicit destination declarations.
 *
 * Test coverage:
 * - Fragment external entries with and without arguments
 * - Composable external entries with and without arguments
 * - Proper destination class generation and factory methods
 * - Cross-module navigation support
 *
 * The tests pass if the annotated functions compile successfully and generate
 * the corresponding Entry classes with proper factory companion objects.
 */

/**
 * Destination for Fragment external entry without arguments
 */
object Destination1 : DestinationWithNoArgs

/**
 * Tests Fragment-based external entry without arguments.
 * Should generate: FragmentExternalEntryWithNoArgsEntry class with factory companion
 */
@UiExternalEntry(ImplementationType.Fragment, Destination1::class)
@Composable
fun FragmentExternalEntryWithNoArgs() = Unit

/**
 * Destination for Fragment external entry with arguments
 */
data class Destination2(override val args: TestArgs) : DestinationWithArgs<TestArgs>

/**
 * Tests Fragment-based external entry with arguments.
 * Should generate: FragmentExternalEntryWithArgsEntry class with factory companion taking TestArgs
 */
@UiExternalEntry(ImplementationType.Fragment, Destination2::class)
@Composable
fun FragmentExternalEntryWithArgs() = Unit

/**
 * Destination for Composable external entry without arguments
 */
object Destination3 : DestinationWithNoArgs

/**
 * Tests Composable-based external entry without arguments.
 * Should generate: ComposableExternalEntryWithNoArgsEntry class extending ComposableEntry<*>
 */
@UiExternalEntry(ImplementationType.Composable, Destination3::class)
@Composable
fun ComposableExternalEntryWithNoArgs() = Unit

/**
 * Destination for Composable external entry with arguments
 */
data class Destination4(override val args: TestArgs) : DestinationWithArgs<TestArgs>

/**
 * Tests Composable-based external entry with arguments.
 * Should generate: ComposableExternalEntryWithArgsEntry class extending ComposableEntry<TestArgs>
 */
@UiExternalEntry(ImplementationType.Composable, Destination4::class)
@Composable
fun ComposableExternalEntryWithArgs() = Unit

/**
 * Unused destination for completeness in test coverage
 */
object Destination5 : DestinationWithNoArgs
