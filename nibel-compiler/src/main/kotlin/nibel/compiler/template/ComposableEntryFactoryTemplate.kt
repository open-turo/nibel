package nibel.compiler.template

@Suppress("LongParameterList", "MaxLineLength")
fun composableExternalEntryFactoryTemplate(
    composableHolderName: String,
    destinationQualifiedName: String,
    hasArgs: Boolean,
    argsQualifiedName: String?,
) = """
|    companion object: ComposableEntryFactory<$destinationQualifiedName> {
|
|        override fun newInstance(destination: $destinationQualifiedName): ComposableEntry<${if (hasArgs) argsQualifiedName else "*"}> {
|            val entry = $composableHolderName(
|                ${if (hasArgs) "args = destination.args," else ""}
|                name = nibel.runtime.buildRouteName($composableHolderName::class.qualifiedName!!, ${if (hasArgs) "destination.args" else "null"}),
|            )
|            return entry
|        }
${
    if (hasArgs) {
        composableInternalEntryFactoryMethodTemplate(composableHolderName, argsQualifiedName!!)
    } else {
        composableInternalEntryFactoryMethodTemplate(composableHolderName)
    }
}
|    }
""".trimMargin("|")

fun composableInternalEntryFactoryTemplate(
    composableHolderName: String,
    argsQualifiedName: String,
) = """
|    companion object {
${
    composableInternalEntryFactoryMethodTemplate(
        composableHolderName,
        argsQualifiedName,
    )
}
|    }
""".trimMargin("|")

@Suppress("MaxLineLength")
fun composableInternalEntryFactoryMethodTemplate(
    composableHolderName: String,
    argsQualifiedName: String,
) = """
|
|        fun newInstance(args: $argsQualifiedName): ComposableEntry<$argsQualifiedName> {
|            val entry = $composableHolderName(
|                args = args,
|                name = nibel.runtime.buildRouteName($composableHolderName::class.qualifiedName!!, args),
|            )
|            return entry
|        }
""".trimMargin("|")

fun composableInternalEntryFactoryTemplate(
    composableHolderName: String,
) = """
|    companion object {
${composableInternalEntryFactoryMethodTemplate(composableHolderName)}
|    }
""".trimMargin("|")

@Suppress("MaxLineLength")
fun composableInternalEntryFactoryMethodTemplate(
    composableHolderName: String,
) = """
|
|        fun newInstance(): ComposableEntry<Parcelable> {
|            val entry = $composableHolderName(
|                name = nibel.runtime.buildRouteName($composableHolderName::class.qualifiedName!!, null),
|            )
|            return entry
|        }
""".trimMargin("|")

// Result-specific factory method templates

fun composableResultInternalEntryFactoryTemplate(
    composableHolderName: String,
    argsQualifiedName: String,
    resultQualifiedName: String,
) = """
|    companion object {
${
    composableResultInternalEntryFactoryMethodTemplate(
        composableHolderName,
        argsQualifiedName,
        resultQualifiedName,
    )
}
|    }
""".trimMargin("|")

@Suppress("MaxLineLength")
fun composableResultInternalEntryFactoryMethodTemplate(
    composableHolderName: String,
    argsQualifiedName: String,
    resultQualifiedName: String,
) = """
|
|        fun newInstance(args: $argsQualifiedName): ResultEntry<$resultQualifiedName> {
|            val entry = $composableHolderName(
|                args = args,
|                name = nibel.runtime.buildRouteName($composableHolderName::class.qualifiedName!!, args),
|            )
|            return entry
|        }
""".trimMargin("|")

fun composableResultInternalEntryFactoryTemplate(
    composableHolderName: String,
    resultQualifiedName: String,
) = """
|    companion object {
${composableResultInternalEntryFactoryMethodTemplate(composableHolderName, resultQualifiedName)}
|    }
""".trimMargin("|")

@Suppress("MaxLineLength")
fun composableResultInternalEntryFactoryMethodTemplate(
    composableHolderName: String,
    resultQualifiedName: String,
) = """
|
|        fun newInstance(): ResultEntry<$resultQualifiedName> {
|            val entry = $composableHolderName(
|                name = nibel.runtime.buildRouteName($composableHolderName::class.qualifiedName!!, null),
|            )
|            return entry
|        }
""".trimMargin("|")

// External result entry factory template

@Suppress("LongParameterList", "MaxLineLength")
fun composableExternalResultEntryFactoryTemplate(
    composableHolderName: String,
    destinationQualifiedName: String,
    hasArgs: Boolean,
    argsQualifiedName: String?,
    resultQualifiedName: String,
) = """
|    companion object: ComposableEntryFactory<$destinationQualifiedName> {
|
|        override fun newInstance(destination: $destinationQualifiedName): ComposableEntry<${if (hasArgs) argsQualifiedName else "*"}> {
|            val entry = $composableHolderName(
|                ${if (hasArgs) "args = destination.args," else ""}
|                name = nibel.runtime.buildRouteName($composableHolderName::class.qualifiedName!!, ${if (hasArgs) "destination.args" else "null"}),
|            )
|            return entry
|        }
${
    if (hasArgs) {
        composableResultInternalEntryFactoryMethodTemplate(
            composableHolderName,
            argsQualifiedName!!,
            resultQualifiedName,
        )
    } else {
        composableResultInternalEntryFactoryMethodTemplate(composableHolderName, resultQualifiedName)
    }
}
|    }
""".trimMargin("|")
