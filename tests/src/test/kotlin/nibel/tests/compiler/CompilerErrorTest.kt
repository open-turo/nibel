package nibel.tests.compiler

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import nibel.compiler.LegacyExternalEntryProcessorProvider
import nibel.compiler.LegacyInternalEntryProcessorProvider
import nibel.compiler.UiExternalEntryProcessorProvider
import nibel.compiler.UiInternalEntryProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test

/**
 * Checks that invalid entry declarations fail the build with the expected error.
 */
@OptIn(ExperimentalCompilerApi::class)
class CompilerErrorTest {

    @Test
    fun `valid entry produces no processor errors`() {
        val result = compile(
            """
            package test

            import android.os.Parcelable
            import androidx.compose.runtime.Composable
            import kotlinx.parcelize.Parcelize
            import nibel.annotations.ImplementationType
            import nibel.annotations.UiEntry

            @Parcelize
            data class DeclaredArgs(val value: String) : Parcelable

            @UiEntry(type = ImplementationType.Composable, args = DeclaredArgs::class)
            @Composable
            fun ValidScreen(args: DeclaredArgs) = Unit
            """,
        )

        result.messages shouldNotContain "e: [ksp]"
        result.sourcesGeneratedBySymbolProcessor.map { it.name }.toList() shouldContain "ValidScreenEntry.kt"
    }

    @Test
    fun `composable param with a type other than the annotation args fails`() {
        val result = compile(
            """
            package test

            import android.os.Parcelable
            import androidx.compose.runtime.Composable
            import kotlinx.parcelize.Parcelize
            import nibel.annotations.ImplementationType
            import nibel.annotations.UiEntry

            @Parcelize
            data class DeclaredArgs(val value: String) : Parcelable

            @Parcelize
            data class OtherArgs(val value: String) : Parcelable

            @UiEntry(type = ImplementationType.Composable, args = DeclaredArgs::class)
            @Composable
            fun MismatchedArgsScreen(args: OtherArgs) = Unit
            """,
        )

        result.exitCode shouldBe ExitCode.COMPILATION_ERROR
        result.messages shouldContain "Invalid parameter 'args' of entry composable"
    }

    // region Result types

    @Test
    fun `result type that is a regular class fails`() {
        val result = compile(
            imports + """
            class PlainResult(val value: String) : Parcelable

            @UiEntry(type = ImplementationType.Composable, result = PlainResult::class)
            @Composable
            fun Screen() = Unit
            """,
        )

        result.exitCode shouldBe ExitCode.COMPILATION_ERROR
        result.messages shouldContain "Result types are allowed to be only 'data class' or 'object'."
    }

    @Test
    fun `generic result type fails`() {
        val result = compile(
            imports + """
            @Parcelize
            data class GenericResult<T : Parcelable>(val value: T) : Parcelable

            @LegacyEntry(result = GenericResult::class)
            class LegacyScreen : androidx.fragment.app.Fragment()
            """,
        )

        result.exitCode shouldBe ExitCode.COMPILATION_ERROR
        result.messages shouldContain "Result types are not allowed to have generic type parameters."
    }

    @Test
    fun `destination and annotation with different result types fail`() {
        val result = compile(
            imports + resultTypes + """
            object PickerDestination : DestinationWithNoArgs, DestinationWithResult<PickedColor>

            @UiExternalEntry(
                type = ImplementationType.Composable,
                destination = PickerDestination::class,
                result = PickedSize::class,
            )
            @Composable
            fun PickerScreen() = Unit
            """,
        )

        result.exitCode shouldBe ExitCode.COMPILATION_ERROR
        result.messages shouldContain "Result type test.PickedSize of the annotation doesn't match result type " +
            "test.PickedColor of destination test.PickerDestination."
    }

    @Test
    fun `destination with a result and annotation without one fail`() {
        val result = compile(
            imports + resultTypes + """
            object PickerDestination : DestinationWithNoArgs, DestinationWithResult<PickedColor>

            @LegacyExternalEntry(destination = PickerDestination::class)
            class PickerFragment : androidx.fragment.app.Fragment()
            """,
        )

        result.exitCode shouldBe ExitCode.COMPILATION_ERROR
        result.messages shouldContain "Destination test.PickerDestination declares result type " +
            "test.PickedColor, so the annotation must declare result = PickedColor::class."
    }

    @Test
    fun `annotation with a result and destination without one fail`() {
        val result = compile(
            imports + resultTypes + """
            object PickerDestination : DestinationWithNoArgs

            @UiExternalEntry(
                type = ImplementationType.Fragment,
                destination = PickerDestination::class,
                result = PickedColor::class,
            )
            @Composable
            fun PickerScreen() = Unit
            """,
        )

        result.exitCode shouldBe ExitCode.COMPILATION_ERROR
        result.messages shouldContain "The annotation declares result type test.PickedColor, so destination " +
            "test.PickerDestination must implement DestinationWithResult<PickedColor>."
    }

    @Test
    fun `destination listing its result type first is valid`() {
        val result = compile(
            imports + resultTypes + """
            object PickerDestination : DestinationWithResult<PickedColor>, DestinationWithNoArgs

            @UiExternalEntry(
                type = ImplementationType.Composable,
                destination = PickerDestination::class,
                result = PickedColor::class,
            )
            @Composable
            fun PickerScreen() = Unit
            """,
        )

        result.messages shouldNotContain "e: [ksp]"
        result.sourcesGeneratedBySymbolProcessor.map { it.name }.toList() shouldContain "PickerScreenEntry.kt"
    }

    @Test
    fun `result type inherited through another interface is valid`() {
        assertValid(
            """
            interface ColorPicking : DestinationWithResult<PickedColor>

            object PickerDestination : DestinationWithNoArgs, ColorPicking
            """,
        )
    }

    @Test
    fun `fully qualified destination supertypes are valid`() {
        assertValid(
            """
            object PickerDestination :
                nibel.annotations.DestinationWithNoArgs,
                nibel.annotations.DestinationWithResult<PickedColor>
            """,
        )
    }

    @Test
    fun `destination supertypes through typealiases are valid`() {
        assertValid(
            """
            typealias NoArgs = DestinationWithNoArgs
            typealias ColorResultOf = DestinationWithResult<PickedColor>

            object PickerDestination : NoArgs, ColorResultOf
            """,
        )
    }

    private fun assertValid(destination: String) {
        val result = compile(
            imports + resultTypes + destination.trimIndent() + """

            @UiExternalEntry(
                type = ImplementationType.Composable,
                destination = PickerDestination::class,
                result = PickedColor::class,
            )
            @Composable
            fun PickerScreen() = Unit
            """,
        )

        result.messages shouldNotContain "e: [ksp]"
        result.sourcesGeneratedBySymbolProcessor.map { it.name }.toList() shouldContain "PickerScreenEntry.kt"
    }

    // endregion

    // region Caller result types

    @Test
    fun `launcher matching the entry result type compiles`() {
        val result = compile(callerSource("navigator.navigateForResult(PickerFragmentEntry.newInstanceForResult(), colorLauncher)"))

        result.exitCode shouldBe ExitCode.OK
    }

    @Test
    fun `launcher with another result type doesn't compile`() {
        val result = compile(callerSource("navigator.navigateForResult(PickerFragmentEntry.newInstanceForResult(), sizeLauncher)"))

        result.exitCode shouldBe ExitCode.COMPILATION_ERROR
        result.messages shouldNotContain "e: [ksp]"
        result.messages shouldContain "Argument type mismatch: actual type is 'ResultLauncher<PickedSize>'"
    }

    @Test
    fun `entry without a result doesn't compile`() {
        val result = compile(callerSource("navigator.navigateForResult(PlainFragmentEntry.newInstance(), colorLauncher)"))

        result.exitCode shouldBe ExitCode.COMPILATION_ERROR
        result.messages shouldNotContain "e: [ksp]"
        result.messages shouldContain "Argument type mismatch: actual type is 'FragmentEntry'"
    }

    /**
     * Caller code around [call]. Legacy entries are used because their generated code compiles
     * without the Compose and Parcelize compiler plugins.
     */
    private fun callerSource(call: String) = """
        package test

        import android.os.Parcel
        import android.os.Parcelable
        import androidx.fragment.app.Fragment
        import nibel.annotations.LegacyEntry
        import nibel.runtime.NavigationController
        import nibel.runtime.ResultLauncher

        abstract class TestParcelable : Parcelable {
            override fun describeContents() = 0
            override fun writeToParcel(dest: Parcel, flags: Int) = Unit
        }

        data class PickedColor(val value: String) : TestParcelable()

        data class PickedSize(val value: Int) : TestParcelable()

        @LegacyEntry(result = PickedColor::class)
        class PickerFragment : Fragment()

        @LegacyEntry
        class PlainFragment : Fragment()

        fun open(
            navigator: NavigationController,
            colorLauncher: ResultLauncher<PickedColor>,
            sizeLauncher: ResultLauncher<PickedSize>,
        ) {
            $call
        }
    """

    // endregion

    private val imports = """
        package test

        import android.os.Parcelable
        import androidx.compose.runtime.Composable
        import kotlinx.parcelize.Parcelize
        import nibel.annotations.DestinationWithNoArgs
        import nibel.annotations.DestinationWithResult
        import nibel.annotations.ImplementationType
        import nibel.annotations.LegacyEntry
        import nibel.annotations.LegacyExternalEntry
        import nibel.annotations.UiEntry
        import nibel.annotations.UiExternalEntry

    """.trimIndent() + "\n"

    private val resultTypes = """
        @Parcelize
        data class PickedColor(val value: String) : Parcelable

        @Parcelize
        data class PickedSize(val value: Int) : Parcelable

    """.trimIndent() + "\n"

    private fun compile(source: String): JvmCompilationResult =
        KotlinCompilation().apply {
            sources = listOf(SourceFile.kotlin("Source.kt", source.trimIndent()))
            inheritClassPath = true
            messageOutputStream = System.out
            configureKsp {
                symbolProcessorProviders += UiInternalEntryProcessorProvider()
                symbolProcessorProviders += UiExternalEntryProcessorProvider()
                symbolProcessorProviders += LegacyInternalEntryProcessorProvider()
                symbolProcessorProviders += LegacyExternalEntryProcessorProvider()
            }
        }.compile()
}
