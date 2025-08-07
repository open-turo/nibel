package nibel.compiler.template

fun fragmentExternalEntryFactoryTemplate(
    objectName: String,
    fragmentName: String,
    destinationQualifiedName: String,
    hasArgs: Boolean,
    argsQualifiedName: String?,
) = """
|    $objectName: FragmentEntryFactory<$destinationQualifiedName> {
|
|        override fun newInstance(destination: $destinationQualifiedName): FragmentEntry {
|            val fragment = $fragmentName()
|            ${if (hasArgs) "fragment.arguments = destination.args.asNibelArgs()" else ""}
|            return FragmentEntry(fragment)
|        }
${
    if (hasArgs) {
        fragmentInternalEntryFactoryMethodTemplate(fragmentName, argsQualifiedName!!)
    } else {
        fragmentInternalEntryFactoryMethodTemplate(fragmentName)
    }
}
|    }
""".trimMargin("|")

fun fragmentInternalEntryFactoryTemplate(
    objectName: String,
    fragmentName: String,
    argsQualifiedName: String,
) = """
|    $objectName {
${fragmentInternalEntryFactoryMethodTemplate(fragmentName, argsQualifiedName)}
|    }
""".trimMargin("|")

fun fragmentInternalEntryFactoryMethodTemplate(
    fragmentName: String,
    argsQualifiedName: String,
) = """
|
|        fun newInstance(args: $argsQualifiedName): FragmentEntry {
|            val fragment = $fragmentName()
|            fragment.arguments = args.asNibelArgs()
|            return FragmentEntry(fragment)
|        }
""".trimMargin("|")

fun fragmentInternalEntryFactoryTemplate(
    objectName: String,
    fragmentName: String,
) = """
|    $objectName {
${fragmentInternalEntryFactoryMethodTemplate(fragmentName)}
|    }
""".trimMargin("|")

fun fragmentInternalEntryFactoryMethodTemplate(
    fragmentName: String,
) = """
|
|        fun newInstance(): FragmentEntry {
|            val fragment = $fragmentName()
|            return FragmentEntry(fragment)
|        }
""".trimMargin("|")
