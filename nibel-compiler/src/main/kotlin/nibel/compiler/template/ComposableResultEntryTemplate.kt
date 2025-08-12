package nibel.compiler.template

fun composableResultEntryWithArgsTemplate(
    packageName: String,
    composableHolderName: String,
    argsQualifiedName: String,
    resultTypeQualifiedName: String,
    composableContent: String,
    composableEntryFactory: String,
) = """
package $packageName

import android.os.Parcelable
import androidx.compose.runtime.Composable
import nibel.runtime.ComposableEntry
import nibel.runtime.ComposableEntryFactory
import nibel.runtime.ResultEntry
import kotlinx.parcelize.Parcelize

@Parcelize
class $composableHolderName(
    override val args: $argsQualifiedName,
    override val name: String,
) : ComposableEntry<$argsQualifiedName>(args, name), ResultEntry<$resultTypeQualifiedName> {

    override val resultType: Class<$resultTypeQualifiedName> = $resultTypeQualifiedName::class.java

    @Composable
    override fun ComposableContent() {
        $composableContent
    }

$composableEntryFactory
}

""".trimIndent()

fun composableResultEntryWithNoArgsTemplate(
    packageName: String,
    composableHolderName: String,
    resultTypeQualifiedName: String,
    composableContent: String,
    composableEntryFactory: String,
) = """
package $packageName

import android.os.Parcelable
import androidx.compose.runtime.Composable
import nibel.runtime.ComposableEntry
import nibel.runtime.ComposableEntryFactory
import nibel.runtime.ResultEntry
import kotlinx.parcelize.Parcelize

@Parcelize
class $composableHolderName(
    override val name: String,
) : ComposableEntry<Parcelable>(null, name), ResultEntry<$resultTypeQualifiedName> {

    override val resultType: Class<$resultTypeQualifiedName> = $resultTypeQualifiedName::class.java

    @Composable
    override fun ComposableContent() {
        $composableContent
    }

$composableEntryFactory
}

""".trimIndent()
