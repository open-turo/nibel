package com.turo.nibel.compiler.template

fun fragmentEntryTemplate(
    packageName: String,
    fragmentName: String,
    composableContent: String,
    fragmentEntryFactory: String,
) = """
package $packageName

import androidx.compose.runtime.Composable
import com.turo.nibel.runtime.asNibelArgs
import com.turo.nibel.runtime.ComposableFragment
import com.turo.nibel.runtime.FragmentEntryFactory
import com.turo.nibel.runtime.FragmentEntry

class $fragmentName : ComposableFragment() {

    @Composable
    override fun ComposableContent() {
        $composableContent
    }

$fragmentEntryFactory
}

""".trimIndent()
