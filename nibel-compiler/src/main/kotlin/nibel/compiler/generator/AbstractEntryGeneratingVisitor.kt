package nibel.compiler.generator

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import nibel.annotations.DestinationWithArgs
import nibel.annotations.DestinationWithNoArgs
import nibel.annotations.DestinationWithResult
import nibel.runtime.NoArgs
import nibel.runtime.NoResult

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

        val destinationSuperType = declaration.findDestinationSuperType(symbol) ?: return null
        val result = parseResult(symbol) ?: return null
        if (!declaration.matchesResult(result, symbol)) {
            return null
        }

        return when (destinationSuperType.declaration.qualifiedName?.asString()) {
            DestinationWithArgs::class.qualifiedName -> {
                val argsReference = destinationSuperType.arguments.first().type?.resolve()?.expandTypeAliases()
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
                    resultQualifiedName = result.qualifiedName,
                    parameters = emptyMap(),
                )
            }

            DestinationWithNoArgs::class.qualifiedName ->
                ExternalEntryMetadata(
                    destinationName = destinationSimpleName.asString(),
                    destinationPackageName = destinationPackageName.asString(),
                    destinationQualifiedName = destinationClassName.asString(),
                    argsQualifiedName = null,
                    resultQualifiedName = result.qualifiedName,
                    parameters = emptyMap(),
                )

            else -> null
        }
    }

    /**
     * A parsed `result` annotation argument. [qualifiedName] is `null` for screens without a result.
     */
    private class ParsedResult(val qualifiedName: String?)

    /**
     * Parses the `result` annotation argument, or returns `null` after reporting an error if the
     * declared result type isn't valid.
     */
    private fun Arguments.parseResult(symbol: KSNode): ParsedResult? {
        val type = this["result"] as? KSType ?: return ParsedResult(null)
        val declaration = type.declaration as KSClassDeclaration
        val qualifiedName = declaration.qualifiedName!!.asString()
        if (qualifiedName == NoResult::class.qualifiedName) {
            return ParsedResult(null)
        }
        if (Modifier.DATA !in declaration.modifiers && declaration.classKind != ClassKind.OBJECT) {
            logger.error(
                message = "Result types are allowed to be only 'data class' or 'object'.",
                symbol = symbol,
            )
            return null
        }
        if (declaration.typeParameters.isNotEmpty()) {
            logger.error(
                message = "Result types are not allowed to have generic type parameters.",
                symbol = symbol,
            )
            return null
        }
        return ParsedResult(qualifiedName)
    }

    /**
     * Checks that a destination declares the same result type with [DestinationWithResult] as its
     * screen's annotation, reporting an error otherwise.
     */
    private fun KSClassDeclaration.matchesResult(result: ParsedResult, symbol: KSNode): Boolean {
        val destinationName = qualifiedName!!.asString()
        // Searches the whole hierarchy, so DestinationWithResult can come from another interface.
        val destinationResult = getAllSuperTypes()
            .map { it.expandTypeAliases() }
            .find { it.declaration.qualifiedName?.asString() == DestinationWithResult::class.qualifiedName }
            ?.arguments?.firstOrNull()?.type?.resolve()?.expandTypeAliases()
            ?.declaration?.qualifiedName?.asString()
        val annotationResult = result.qualifiedName

        val error = when {
            destinationResult == annotationResult -> return true
            annotationResult == null ->
                "Destination $destinationName declares result type $destinationResult, so the annotation " +
                    "must declare result = ${destinationResult?.substringAfterLast('.')}::class."

            destinationResult == null ->
                "The annotation declares result type $annotationResult, so destination $destinationName " +
                    "must implement ${DestinationWithResult::class.simpleName}" +
                    "<${annotationResult.substringAfterLast('.')}>."

            else ->
                "Result type $annotationResult of the annotation doesn't match result type " +
                    "$destinationResult of destination $destinationName."
        }
        logger.error(message = error, symbol = symbol)
        return false
    }

    protected fun Arguments.parseInternalEntry(symbol: KSNode): InternalEntryMetadata? {
        val arg = this["args"] as KSType
        val declaration = arg.declaration as KSClassDeclaration
        if (!declaration.isCorrectArgsDeclaration(symbol)) {
            return null
        }
        val result = parseResult(symbol) ?: return null

        val argsClassName = arg.declaration.qualifiedName!!.asString()
        return InternalEntryMetadata(
            argsQualifiedName = argsClassName.takeUnless { it == NoArgs::class.qualifiedName },
            resultQualifiedName = result.qualifiedName,
            parameters = emptyMap(),
        )
    }

    private fun KSClassDeclaration.isCorrectDestinationDeclaration(symbol: KSNode): Boolean {
        val isAllowedType = (classKind != ClassKind.CLASS && classKind != ClassKind.OBJECT) ||
            Modifier.SEALED in modifiers ||
            Modifier.ABSTRACT in modifiers ||
            Modifier.OPEN in modifiers ||
            Modifier.VALUE in modifiers

        if (isAllowedType) {
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
        val isInvalidArgsType =
            (classKind != ClassKind.CLASS && classKind != ClassKind.OBJECT && Modifier.SEALED !in modifiers)

        if (isInvalidArgsType) {
            logger.error(
                message = "Args are allowed to be only 'data class', 'object', 'sealed class' or 'sealed interface'.",
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

    private fun KSClassDeclaration.findDestinationSuperType(symbol: KSNode): KSType? {
        // Resolved rather than matched by name, so qualified names and typealiases work, and
        // DestinationWithResult can be listed in any position.
        val destinationTypes =
            setOf(DestinationWithArgs::class.qualifiedName, DestinationWithNoArgs::class.qualifiedName)
        val destinationSuperType = superTypes
            .map { it.resolve().expandTypeAliases() }
            .find { it.declaration.qualifiedName?.asString() in destinationTypes }

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

    private fun KSType.expandTypeAliases(): KSType {
        var type = this
        while (true) {
            val alias = type.declaration as? KSTypeAlias ?: return type
            type = alias.type.resolve()
        }
    }
}
