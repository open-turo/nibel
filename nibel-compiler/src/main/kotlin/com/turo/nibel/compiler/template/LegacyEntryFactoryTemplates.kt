package com.turo.nibel.compiler.template

fun legacyEntryTemplate(
    packageName: String,
    fragmentEntryFactory: String,
) = """
package $packageName

import com.turo.nibel.runtime.asNibelArgs
import com.turo.nibel.runtime.FragmentEntryFactory
import com.turo.nibel.runtime.FragmentEntry

$fragmentEntryFactory

""".trimIndent()
