package com.turo.nibel.compiler.generator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.turo.nibel.annotations.DestinationWithArgs
import com.turo.nibel.annotations.DestinationWithNoArgs
import com.turo.nibel.runtime.NoArgs

abstract class AbstractEntryGeneratingVisitor(
    private val resolver: Resolver,
    protected val logger: KSPLogger,
) : KSVisitorVoid() {

    protected fun Arguments.parseExternalEntry(symbol: KSNode): ExternalEntryMetadata? {
        val arg = this["destination"] as KSType
        val destinationSimpleName = arg.declaration.simpleName
        val destinationPackageName = arg.declaration.packageName
        val destinationClassName = arg.declaration.qualifiedName!!

        val declaration = resolver.getClassDeclarationByName(destinationClassName)!!
        if (!declaration.isCorrectDestinationDeclaration(symbol)) {
            return null
        }

        val reference = declaration.findDestinationSuperType(symbol) ?: return null

        val ref = reference.toString()
        return when {
            ref.startsWith(DestinationWithArgs::class.simpleName!!) -> {
                val argsReference = reference.element?.typeArguments?.first()?.type?.resolve()
                val argsDeclaration = argsReference?.declaration as KSClassDeclaration
                if (!argsDeclaration.isCorrectArgsDeclaration(symbol)) {
                    return null
                }
                val argsClassName = argsDeclaration.qualifiedName!!.asString()
                if (argsClassName == NoArgs::class.qualifiedName) {
                    logger.error(
                        message = "Unable to use NoArgs as argument for DestinationWithArgs",
                        symbol = symbol,
                    )
                    return null
                }
                ExternalEntryMetadata(
                    destinationName = destinationSimpleName.asString(),
                    destinationPackageName = destinationPackageName.asString(),
                    destinationQualifiedName = destinationClassName.asString(),
                    argsQualifiedName = argsClassName,
                    parameters = emptyMap()
                )
            }

            ref.startsWith(DestinationWithNoArgs::class.simpleName!!) ->
                ExternalEntryMetadata(
                    destinationName = destinationSimpleName.asString(),
                    destinationPackageName = destinationPackageName.asString(),
                    destinationQualifiedName = destinationClassName.asString(),
                    argsQualifiedName = null,
                    parameters = emptyMap()
                )

            else -> null
        }
    }

    protected fun Arguments.parseInternalEntry(symbol: KSNode): InternalEntryMetadata? {
        val arg = this["args"] as KSType
        val declaration = arg.declaration as KSClassDeclaration
        if (!declaration.isCorrectArgsDeclaration(symbol)) {
            return null
        }

        val argsClassName = arg.declaration.qualifiedName!!.asString()
        return if (argsClassName == NoArgs::class.qualifiedName) {
            InternalEntryMetadata(
                argsQualifiedName = null,
                parameters = emptyMap()
            )
        } else {
            InternalEntryMetadata(
                argsQualifiedName = argsClassName,
                parameters = emptyMap()
            )
        }
    }

    private fun KSClassDeclaration.isCorrectDestinationDeclaration(symbol: KSNode): Boolean {
        if ((classKind != ClassKind.CLASS && classKind != ClassKind.OBJECT) ||
            Modifier.SEALED in modifiers ||
            Modifier.ABSTRACT in modifiers ||
            Modifier.OPEN in modifiers ||
            Modifier.VALUE in modifiers
        ) {
            logger.error(
                message = "Destinations are allowed to be only 'class', 'data class' or 'object'.",
                symbol = symbol,
            )
            return false
        }
        if (typeParameters.isNotEmpty()) {
            logger.error(
                message = "Destination declarations are not allowed to have generic type parameters.",
                symbol = symbol,
            )
            return false
        }
        return true
    }

    private fun KSClassDeclaration.isCorrectArgsDeclaration(symbol: KSNode): Boolean {
        if (Modifier.DATA !in modifiers && classKind != ClassKind.OBJECT) {
            logger.error(
                message = "Args are allowed to be only 'data class' or 'object'.",
                symbol = symbol,
            )
            return false
        }
        if (typeParameters.isNotEmpty()) {
            logger.error(
                message = "Args declarations are not allowed to have generic type parameters.",
                symbol = symbol,
            )
            return false
        }
        return true
    }

    private fun KSClassDeclaration.findDestinationSuperType(symbol: KSNode): KSTypeReference? {
        val destinationSuperType = superTypes.toList()
            .find { it.toString().startsWith("DestinationWith") }

        if (destinationSuperType == null) {
            logger.error(
                message = "Destination should directly inherit " +
                        "${DestinationWithNoArgs::class.simpleName} or ${DestinationWithArgs::class.simpleName}.",
                symbol = symbol,
            )
            return null
        }
        return destinationSuperType
    }
}
