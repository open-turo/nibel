@file:Suppress("TestFunctionName")

package nibel.tests.codegen

import androidx.compose.runtime.Composable
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry

/**
 * Compilation tests for @UiEntry annotation (internal entries).
 *
 * These tests verify that the KSP annotation processor can successfully generate entry classes
 * for internal (intra-module) navigation targets. Internal entries are defined in the same
 * module where they are used and don't require external destination declarations.
 *
 * Test coverage:
 * - Fragment entries with and without arguments
 * - Composable entries with and without arguments
 * - Proper code generation for basic internal entry patterns
 *
 * The tests pass if the annotated functions compile successfully and generate
 * the corresponding Entry classes without compilation errors.
 */

/**
 * Tests Fragment-based internal entry without arguments.
 * Should generate: FragmentEntryWithNoArgsEntry class extending FragmentEntry
 */
@UiEntry(ImplementationType.Fragment)
@Composable
fun FragmentEntryWithNoArgs() = Unit

/**
 * Tests Fragment-based internal entry with arguments.
 * Should generate: FragmentEntryWithArgsEntry class extending FragmentEntry with TestArgs parameter
 */
@UiEntry(ImplementationType.Fragment, TestArgs::class)
@Composable
fun FragmentEntryWithArgs() = Unit

/**
 * Tests Composable-based internal entry without arguments.
 * Should generate: ComposableEntryWithNoArgsEntry class extending ComposableEntry<Parcelable>
 */
@UiEntry(ImplementationType.Composable)
@Composable
fun ComposableEntryWithNoArgs() = Unit

/**
 * Tests Composable-based internal entry with arguments.
 * Should generate: ComposableEntryWithArgsEntry class extending ComposableEntry<TestArgs>
 */
@UiEntry(ImplementationType.Composable, TestArgs::class)
@Composable
fun ComposableEntryWithArgs() = Unit
