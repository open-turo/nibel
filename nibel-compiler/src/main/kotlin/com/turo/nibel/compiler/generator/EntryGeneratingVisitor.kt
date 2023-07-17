package com.turo.nibel.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.turo.nibel.annotations.ImplementationType.Composable
import com.turo.nibel.annotations.ImplementationType.Fragment
import com.turo.nibel.annotations.UiExternalEntry
import com.turo.nibel.annotations.UiEntry
import com.turo.nibel.compiler.ProcessorType
import com.turo.nibel.compiler.ProcessorType.ExternalEntry
import com.turo.nibel.compiler.ProcessorType.InternalEntry

class EntryGeneratingVisitor(
    val type: ProcessorType,
    codeGenerator: CodeGenerator,
    resolver: Resolver,
    logger: KSPLogger,
    entryFactoriesRegistry: EntryFactoriesRegistry
) : AbstractEntryGeneratingVisitor(resolver, logger) {

    private val fragmentGenerator by lazy {
        FragmentGenerator(resolver, codeGenerator, logger, entryFactoriesRegistry)
    }
    private val composableGenerator by lazy {
        ComposableGenerator(resolver, codeGenerator, logger, entryFactoriesRegistry)
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        if (!function.isCorrectlyAnnotated()) return

        val annotation = when (type) {
            ExternalEntry -> function.findAnnotation<UiExternalEntry>()
            InternalEntry -> function.findAnnotation<UiEntry>()
        }!!

        val arguments = annotation.arguments.toMap()
        val implementationType = (arguments["type"] as KSType).asImplementationType()

        val metadata = when (type) {
            ExternalEntry -> arguments.parseExternalEntry(function)?.run {
                copy(parameters = function.parseParameters(argsQualifiedName))
            }

            InternalEntry -> arguments.parseInternalEntry(function)?.run {
                copy(parameters = function.parseParameters(argsQualifiedName))
            }
        } ?: return

        when (implementationType) {
            Fragment -> fragmentGenerator.generate(function, metadata)
            Composable -> composableGenerator.generate(function, metadata)
        }
    }

    private fun KSFunctionDeclaration.isCorrectlyAnnotated(): Boolean {
        if (!isComposable()) {
            logger.error(
                message = "Only @Composable function can be annotated with " +
                        "@${UiExternalEntry::class.simpleName} or @${UiEntry::class.simpleName}.",
                symbol = this,
            )
            return false
        }

        val annotationCount = countUiEntryAnnotations()
        when {
            annotationCount == 0 -> {
                logger.error(
                    message = "Must be annotated with " +
                            "@${UiExternalEntry::class.simpleName} or @${UiEntry::class.simpleName}.",
                    symbol = this,
                )
                return false
            }

            annotationCount > 1 -> {
                logger.error(
                    message = "@Composable function can't be annotated with more than one " +
                            "@${UiExternalEntry::class.simpleName} or @${UiEntry::class.simpleName}.",
                    symbol = this,
                )
                return false
            }
        }
        return true
    }

    private fun KSFunctionDeclaration.countUiEntryAnnotations(): Int =
        annotations.map { it.shortName.getShortName() }.count {
            it == UiExternalEntry::class.simpleName || it == UiEntry::class.simpleName
        }

    private fun KSFunctionDeclaration.parseParameters(
        argsQualifiedName: String?
    ): Map<ParameterType, ParameterMetadata> {
        val allowedParamTypes = mutableMapOf(
            "com.turo.nibel.runtime.NavigationController" to ParameterType.NAVIGATION_CONTROLLER,
            "com.turo.nibel.annotations.ImplementationType" to ParameterType.IMPLEMENTATION_TYPE,
        )
        if (argsQualifiedName != null) {
            allowedParamTypes[argsQualifiedName] = ParameterType.ARGS
        }

        val composableParams = mutableMapOf<ParameterType, ParameterMetadata>()

        for (param in parameters) {
            val qualifiedName = param.type.resolve().declaration.qualifiedName!!.asString()
            val paramType = allowedParamTypes[qualifiedName]
            if (paramType != null) {
                composableParams[paramType] = ParameterMetadata(
                    type = paramType,
                    name = param.name!!.asString(),
                    qualifiedClassName = qualifiedName
                )
            } else if (!param.hasDefault) {
                logger.error(
                    message = "Invalid parameter '$param' of entry composable",
                    symbol = this,
                )
            }
        }
        return composableParams
    }
}
