package nibel.compiler.template

fun composableEntryWithArgsTemplate(
    packageName: String,
    composableHolderName: String,
    argsQualifiedName: String,
    resultQualifiedName: String?,
    composableContent: String,
    composableEntryFactory: String,
) = """
package $packageName

import android.os.Parcelable
import androidx.compose.runtime.Composable
import nibel.runtime.ComposableEntry
import nibel.runtime.ComposableEntryFactory
import kotlinx.parcelize.Parcelize
${if (resultQualifiedName != null) "import nibel.runtime.ResultEntry" else ""}

@Parcelize
class $composableHolderName(
    override val args: $argsQualifiedName,
    override val name: String,
) : ComposableEntry<$argsQualifiedName>(args, name)${if (resultQualifiedName != null) ", ResultEntry<$argsQualifiedName, $resultQualifiedName>" else ""} {

    @Composable
    override fun ComposableContent() {
        $composableContent
    }

${if (resultQualifiedName != null) "    override val resultType: Class<$resultQualifiedName> = $resultQualifiedName::class.java\n" else ""}$composableEntryFactory
}

""".trimIndent()

fun composableEntryWithNoArgsTemplate(
    packageName: String,
    composableHolderName: String,
    resultQualifiedName: String?,
    composableContent: String,
    composableEntryFactory: String,
) = """
package $packageName

import android.os.Parcelable
import androidx.compose.runtime.Composable
import nibel.runtime.ComposableEntry
import nibel.runtime.ComposableEntryFactory
import kotlinx.parcelize.Parcelize
${if (resultQualifiedName != null) "import nibel.runtime.ResultEntry" else ""}

@Parcelize
class $composableHolderName(
    override val name: String,
) : ComposableEntry<Parcelable>(null, name)${if (resultQualifiedName != null) ", ResultEntry<Parcelable, $resultQualifiedName>" else ""} {

    @Composable
    override fun ComposableContent() {
        $composableContent
    }

${if (resultQualifiedName != null) "    override val resultType: Class<$resultQualifiedName> = $resultQualifiedName::class.java\n" else ""}$composableEntryFactory
}

""".trimIndent()
