package nibel.compiler.template

import nibel.compiler.generator.EntryMetadata

/**
 * Adds a typed `newInstanceForResult` factory method to this entry factory (a companion object or
 * object ending with its closing brace) if the screen of [metadata] declares a result. Leaves the
 * factory unchanged otherwise.
 */
fun String.withResultFactoryMethod(metadata: EntryMetadata): String {
    val resultQualifiedName = metadata.resultQualifiedName ?: return this
    val argsQualifiedName = metadata.argsQualifiedName

    val closingBrace = substringAfterLast('\n')
    check(closingBrace.trim() == "}") { "Expected an entry factory ending with its closing brace." }

    val parameters = if (argsQualifiedName != null) "args: $argsQualifiedName" else ""
    val arguments = if (argsQualifiedName != null) "args" else ""
    val method = """
        |
        |        fun newInstanceForResult($parameters): nibel.runtime.ResultEntry<$resultQualifiedName> =
        |            nibel.runtime.ResultEntry(newInstance($arguments))
    """.trimMargin()
    return substringBeforeLast('\n') + "\n" + method + "\n" + closingBrace
}
