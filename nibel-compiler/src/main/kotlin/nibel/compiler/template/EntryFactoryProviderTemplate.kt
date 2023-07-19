package nibel.compiler.template

fun entryFactoryProviderTemplate(
    destinationName: String,
    entryFactoryQualifiedName: String,
) = """
@nibel.runtime.EntryFactoryProvider
object $destinationName {

    @JvmStatic
    fun provide() = $entryFactoryQualifiedName
}

""".trimIndent()
