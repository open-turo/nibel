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
