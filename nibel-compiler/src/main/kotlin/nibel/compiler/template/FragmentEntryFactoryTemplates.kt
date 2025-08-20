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

fun fragmentExternalResultEntryFactoryTemplate(
    objectName: String,
    fragmentName: String,
    destinationQualifiedName: String,
    hasArgs: Boolean,
    argsQualifiedName: String?,
    resultQualifiedName: String,
) = """
|    $objectName: FragmentResultEntryFactory<$destinationQualifiedName, $resultQualifiedName> {
|
|        override fun newInstance(destination: $destinationQualifiedName): FragmentResultEntryWrapper<$resultQualifiedName> {
|            val fragment = ${fragmentName}Fragment()
|            ${if (hasArgs) "fragment.arguments = destination.args.asNibelArgs()" else ""}
|            val fragmentEntry = FragmentEntry(fragment)
|            return FragmentResultEntryWrapper(fragmentEntry, $resultQualifiedName::class.java)
|        }
${
    if (hasArgs) {
        fragmentResultInternalEntryFactoryMethodTemplate(fragmentName, argsQualifiedName!!, resultQualifiedName)
    } else {
        fragmentResultInternalEntryFactoryMethodTemplate(fragmentName, resultQualifiedName)
    }
}
|    }
""".trimMargin("|")

fun fragmentResultInternalEntryFactoryTemplate(
    objectName: String,
    fragmentName: String,
    argsQualifiedName: String,
    resultQualifiedName: String,
) = """
|    $objectName {
${fragmentResultInternalEntryFactoryMethodTemplate(fragmentName, argsQualifiedName, resultQualifiedName)}
|    }
""".trimMargin("|")

fun fragmentResultInternalEntryFactoryMethodTemplate(
    fragmentName: String,
    argsQualifiedName: String,
    resultQualifiedName: String,
) = """
|
|        fun newInstance(args: $argsQualifiedName): FragmentResultEntryWrapper<$resultQualifiedName> {
|            val fragment = ${fragmentName}Fragment()
|            fragment.arguments = args.asNibelArgs()
|            val fragmentEntry = FragmentEntry(fragment)
|            return FragmentResultEntryWrapper(fragmentEntry, $resultQualifiedName::class.java)
|        }
""".trimMargin("|")

fun fragmentResultInternalEntryFactoryTemplate(
    objectName: String,
    fragmentName: String,
    resultQualifiedName: String,
) = """
|    $objectName {
${fragmentResultInternalEntryFactoryMethodTemplate(fragmentName, resultQualifiedName)}
|    }
""".trimMargin("|")

fun fragmentResultInternalEntryFactoryMethodTemplate(
    fragmentName: String,
    resultQualifiedName: String,
) = """
|
|        fun newInstance(): FragmentResultEntryWrapper<$resultQualifiedName> {
|            val fragment = ${fragmentName}Fragment()
|            val fragmentEntry = FragmentEntry(fragment)
|            return FragmentResultEntryWrapper(fragmentEntry, $resultQualifiedName::class.java)
|        }
""".trimMargin("|")
