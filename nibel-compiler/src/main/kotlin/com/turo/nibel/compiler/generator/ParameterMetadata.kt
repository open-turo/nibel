package com.turo.nibel.compiler.generator

data class ParameterMetadata(
    val type: ParameterType,
    val name: String,
    val qualifiedClassName: String,
)

enum class ParameterType {
    ARGS,
    NAVIGATION_CONTROLLER,
    IMPLEMENTATION_TYPE
}
