package com.turo.nibel.compiler.template

fun entryFactoryProviderTemplate(
    destinationName: String,
    entryFactoryQualifiedName: String,
) = """
@com.turo.nibel.runtime.EntryFactoryProvider
object $destinationName {

    @JvmStatic
    fun provide() = $entryFactoryQualifiedName
}

""".trimIndent()
