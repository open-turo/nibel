package nibel.compiler.template

fun fragmentEntryTemplate(
    packageName: String,
    fragmentName: String,
    argsQualifiedName: String?,
    resultQualifiedName: String?,
    composableContent: String,
    fragmentEntryFactory: String,
) = """
package $packageName

import android.os.Parcelable
import androidx.compose.runtime.Composable
import nibel.runtime.asNibelArgs
import nibel.runtime.ComposableFragment
import nibel.runtime.FragmentEntryFactory
import nibel.runtime.FragmentEntry
${if (resultQualifiedName != null) "import nibel.runtime.ResultEntry" else ""}

class $fragmentName : ComposableFragment()${if (resultQualifiedName != null) {
    val argsType = argsQualifiedName ?: "Parcelable"
    ", ResultEntry<$argsType, $resultQualifiedName>"
} else {
    ""
}} {

    @Composable
    override fun ComposableContent() {
        $composableContent
    }

${if (resultQualifiedName != null) "    override val resultType: Class<$resultQualifiedName> = $resultQualifiedName::class.java\n" else ""}$fragmentEntryFactory
}

""".trimIndent()
