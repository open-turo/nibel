package com.turo.nibel.compiler.generator

import com.turo.nibel.compiler.generator.ParameterType.ARGS
import com.turo.nibel.compiler.generator.ParameterType.IMPLEMENTATION_TYPE
import com.turo.nibel.compiler.generator.ParameterType.NAVIGATION_CONTROLLER

class ComposableContentGenerator {

    fun generate(
        composableQualifiedName: String,
        parameters: Map<ParameterType, ParameterMetadata>
    ) = buildString {
        append("$composableQualifiedName(")
        if (parameters.isEmpty()) {
            append(")")
            return@buildString
        } else {
            append("\n")
        }

        for ((type, param) in parameters) {
            val indent = " ".repeat(12)
            append("$indent${param.name} = ")
            val value = when (type) {
                ARGS -> "$COMPOSITION_LOCALS_PACKAGE.LocalArgs.current as ${param.qualifiedClassName}"
                NAVIGATION_CONTROLLER -> "$COMPOSITION_LOCALS_PACKAGE.LocalNavigationController.current"
                IMPLEMENTATION_TYPE -> "$COMPOSITION_LOCALS_PACKAGE.LocalImplementationType.current!!"
            }
            append(value)
            append(",\n")
        }

        val indent = " ".repeat(8)
        append("$indent)")
    }

    companion object {
        const val COMPOSITION_LOCALS_PACKAGE = "com.turo.nibel.runtime"
    }
}
