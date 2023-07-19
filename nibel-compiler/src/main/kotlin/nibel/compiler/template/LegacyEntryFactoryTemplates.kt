package nibel.compiler.template

fun legacyEntryTemplate(
    packageName: String,
    fragmentEntryFactory: String,
) = """
package $packageName

import nibel.runtime.asNibelArgs
import nibel.runtime.FragmentEntryFactory
import nibel.runtime.FragmentEntry

$fragmentEntryFactory

""".trimIndent()
