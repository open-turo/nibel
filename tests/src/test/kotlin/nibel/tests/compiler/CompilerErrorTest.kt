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

    private fun compile(source: String): JvmCompilationResult =
        KotlinCompilation().apply {
            sources = listOf(SourceFile.kotlin("Source.kt", source.trimIndent()))
            inheritClassPath = true
            messageOutputStream = System.out
            configureKsp {
                symbolProcessorProviders += UiInternalEntryProcessorProvider()
                symbolProcessorProviders += UiExternalEntryProcessorProvider()
            }
        }.compile()
}
