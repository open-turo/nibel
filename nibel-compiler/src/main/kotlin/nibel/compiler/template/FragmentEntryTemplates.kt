package nibel.compiler.template

fun fragmentEntryTemplate(
    packageName: String,
    fragmentName: String,
    composableContent: String,
    fragmentEntryFactory: String,
) = """
package $packageName

import androidx.compose.runtime.Composable
import nibel.runtime.asNibelArgs
import nibel.runtime.ComposableFragment
import nibel.runtime.FragmentEntryFactory
import nibel.runtime.FragmentEntry

class $fragmentName : ComposableFragment() {

    @Composable
    override fun ComposableContent() {
        $composableContent
    }

$fragmentEntryFactory
}

""".trimIndent()

fun fragmentResultEntryTemplate(
    packageName: String,
    fragmentName: String,
    @Suppress("UnusedParameter") resultTypeQualifiedName: String,
    composableContent: String,
    fragmentResultEntryFactory: String,
) = """
package $packageName

import androidx.compose.runtime.Composable
import nibel.runtime.asNibelArgs
import nibel.runtime.ComposableFragment
import nibel.runtime.FragmentEntry
import nibel.runtime.FragmentEntryFactory
import nibel.runtime.FragmentResultEntryFactory
import nibel.runtime.FragmentResultEntryWrapper
import nibel.runtime.ResultEntry

class ${fragmentName}Fragment : ComposableFragment() {

    @Composable
    override fun ComposableContent() {
        $composableContent
    }

$fragmentResultEntryFactory
}

""".trimIndent()
