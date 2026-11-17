@file:Suppress("TestFunctionName")

package nibel.tests.codegen

import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nibel.annotations.DestinationWithArgs
import nibel.annotations.DestinationWithNoArgs
import nibel.annotations.DestinationWithResult
import nibel.annotations.ImplementationType
import nibel.annotations.LegacyEntry
import nibel.annotations.LegacyExternalEntry
import nibel.annotations.UiEntry
import nibel.annotations.UiExternalEntry
import nibel.runtime.ComposableEntry
import nibel.runtime.FragmentEntry
import nibel.runtime.ResultEntry
import org.junit.Test

// Internal entries

@UiEntry(ImplementationType.Fragment, result = TestResult::class)
@Composable
fun FragmentEntryWithResult() = Unit

@UiEntry(ImplementationType.Fragment, TestArgs::class, TestResult::class)
@Composable
fun FragmentEntryWithArgsAndResult() = Unit

@UiEntry(ImplementationType.Composable, result = TestResult::class)
@Composable
fun ComposableEntryWithResult() = Unit

@UiEntry(ImplementationType.Composable, TestArgs::class, TestResult::class)
@Composable
fun ComposableEntryWithArgsAndResult() = Unit

@LegacyEntry(result = TestResult::class)
class LegacyFragmentWithResult : Fragment()

@LegacyEntry(TestArgs::class, TestResult::class)
class LegacyFragmentWithArgsAndResult : Fragment()

// External entries

object ResultDestination1 : DestinationWithNoArgs, DestinationWithResult<TestResult>

@UiExternalEntry(ImplementationType.Fragment, ResultDestination1::class, TestResult::class)
@Composable
fun FragmentExternalEntryWithResult() = Unit

data class ResultDestination2(override val args: TestArgs) : DestinationWithArgs<TestArgs>, DestinationWithResult<TestResult>

@UiExternalEntry(ImplementationType.Fragment, ResultDestination2::class, TestResult::class)
@Composable
fun FragmentExternalEntryWithArgsAndResult() = Unit

object ResultDestination3 : DestinationWithResult<TestResult>, DestinationWithNoArgs

@UiExternalEntry(ImplementationType.Composable, ResultDestination3::class, TestResult::class)
@Composable
fun ComposableExternalEntryWithResult() = Unit

data class ResultDestination4(override val args: TestArgs) : DestinationWithArgs<TestArgs>, DestinationWithResult<TestResult>

@UiExternalEntry(ImplementationType.Composable, ResultDestination4::class, TestResult::class)
@Composable
fun ComposableExternalEntryWithArgsAndResult() = Unit

object ResultDestination5 : DestinationWithNoArgs, DestinationWithResult<TestResult>

@LegacyExternalEntry(ResultDestination5::class, TestResult::class)
class LegacyExternalFragmentWithResult : Fragment()

data class ResultDestination6(override val args: TestArgs) : DestinationWithArgs<TestArgs>, DestinationWithResult<TestResult>

@LegacyExternalEntry(ResultDestination6::class, TestResult::class)
class LegacyExternalFragmentWithArgsAndResult : Fragment()

/**
 * Every entry that declares a result gets a `newInstanceForResult` factory typed with its result.
 * The explicit types make a wrong generated signature fail to compile. Factories with args are only
 * referenced, since creating them needs a real Bundle.
 */
class ResultEntryCompileTest {

    @Test
    fun `entries with args have typed factories`() {
        val factories: List<(TestArgs) -> ResultEntry<TestResult>> = listOf(
            FragmentEntryWithArgsAndResultEntry::newInstanceForResult,
            FragmentExternalEntryWithArgsAndResultEntry::newInstanceForResult,
            ComposableEntryWithArgsAndResultEntry::newInstanceForResult,
            ComposableExternalEntryWithArgsAndResultEntry::newInstanceForResult,
            LegacyFragmentWithArgsAndResultEntry::newInstanceForResult,
            LegacyExternalFragmentWithArgsAndResultEntry::newInstanceForResult,
        )
        factories.size shouldBe 6
    }

    @Test
    fun `fragment entries without args are typed with their result`() {
        val entries: List<ResultEntry<TestResult>> = listOf(
            FragmentEntryWithResultEntry.newInstanceForResult(),
            FragmentExternalEntryWithResultEntry.newInstanceForResult(),
            LegacyFragmentWithResultEntry.newInstanceForResult(),
            LegacyExternalFragmentWithResultEntry.newInstanceForResult(),
        )
        entries.forEach { it.entry.shouldBeInstanceOf<FragmentEntry>() }
    }

    @Test
    fun `composable entries without args are typed with their result`() {
        val entries: List<ResultEntry<TestResult>> = listOf(
            ComposableEntryWithResultEntry.newInstanceForResult(),
            ComposableExternalEntryWithResultEntry.newInstanceForResult(),
        )
        entries.forEach { it.entry.shouldBeInstanceOf<ComposableEntry<*>>() }
    }

    @Test
    fun `regular factories stay available`() {
        val withArgs: (TestArgs) -> FragmentEntry = FragmentEntryWithArgsAndResultEntry::newInstance
        withArgs.shouldBeInstanceOf<(TestArgs) -> FragmentEntry>()
        ComposableEntryWithResultEntry.newInstance().shouldBeInstanceOf<ComposableEntry<*>>()
        LegacyFragmentWithResultEntry.newInstance().shouldBeInstanceOf<FragmentEntry>()
    }
}
