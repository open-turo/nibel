package com.turo.nibel.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.turo.nibel.annotations.LegacyExternalEntry
import com.turo.nibel.annotations.LegacyEntry
import com.turo.nibel.compiler.ProcessorType
import com.turo.nibel.compiler.ProcessorType.ExternalEntry
import com.turo.nibel.compiler.ProcessorType.InternalEntry

class LegacyEntryGeneratingVisitor(
    val type: ProcessorType,
    codeGenerator: CodeGenerator,
    resolver: Resolver,
    logger: KSPLogger,
    entryFactoriesRegistry: EntryFactoriesRegistry
) : AbstractEntryGeneratingVisitor(resolver, logger) {

    private val legacyEntryGenerator by lazy {
        LegacyEntryGenerator(resolver, codeGenerator, logger, entryFactoriesRegistry)
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val annotation = when (type) {
            ExternalEntry -> classDeclaration.findAnnotation<LegacyExternalEntry>()
            InternalEntry -> classDeclaration.findAnnotation<LegacyEntry>()
        }!!

        val arguments = annotation.arguments.toMap()

        val metadata = when (type) {
            ExternalEntry -> arguments.parseExternalEntry(classDeclaration)
            InternalEntry -> arguments.parseInternalEntry(classDeclaration)
        } ?: return

        legacyEntryGenerator.generate(classDeclaration, metadata)
    }
}
