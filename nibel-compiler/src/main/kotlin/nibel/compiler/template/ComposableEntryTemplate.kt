package nibel.compiler.template

fun composableEntryWithArgsTemplate(
    packageName: String,
    composableHolderName: String,
    argsQualifiedName: String,
    composableContent: String,
    composableEntryFactory: String,
) = """
package $packageName

import android.os.Parcelable
import androidx.compose.runtime.Composable
import nibel.runtime.ComposableEntry
import nibel.runtime.ComposableEntryFactory
import kotlinx.parcelize.Parcelize

@Parcelize
class $composableHolderName(
    override val args: $argsQualifiedName,
    override val name: String,
) : ComposableEntry<$argsQualifiedName>(args, name) {

    @Composable
    override fun ComposableContent() {
        $composableContent
    }

$composableEntryFactory
}

""".trimIndent()

fun composableEntryWithNoArgsTemplate(
    packageName: String,
    composableHolderName: String,
    composableContent: String,
    composableEntryFactory: String,
) = """
package $packageName

import android.os.Parcelable
import androidx.compose.runtime.Composable
import nibel.runtime.ComposableEntry
import nibel.runtime.ComposableEntryFactory
import kotlinx.parcelize.Parcelize

@Parcelize
class $composableHolderName(
    override val name: String,
) : ComposableEntry<Parcelable>(null, name) {

    @Composable
    override fun ComposableContent() {
        $composableContent
    }

$composableEntryFactory
}

""".trimIndent()
