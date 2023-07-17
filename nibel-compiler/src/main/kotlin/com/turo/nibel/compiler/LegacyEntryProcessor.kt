package com.turo.nibel.compiler

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.turo.nibel.annotations.LegacyExternalEntry
import com.turo.nibel.annotations.LegacyEntry
import com.turo.nibel.compiler.ProcessorType.ExternalEntry
import com.turo.nibel.compiler.ProcessorType.InternalEntry
import com.turo.nibel.compiler.generator.EntryFactoriesRegistry
import com.turo.nibel.compiler.generator.EntryFactoryProviderGenerator
import com.turo.nibel.compiler.generator.LegacyEntryGeneratingVisitor

class LegacyEntryProcessor(
    private val type: ProcessorType,
    private val options: Map<String, String>,
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    private val entryFactoriesRegistry by lazy {
        EntryFactoriesRegistry()
    }

    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()

        val symbols = when (type) {
            ExternalEntry -> resolver.getSymbolsWithAnnotation(LegacyExternalEntry::class.qualifiedName!!)
            InternalEntry -> resolver.getSymbolsWithAnnotation(LegacyEntry::class.qualifiedName!!)
        }.filterIsInstance<KSClassDeclaration>()

        symbols.forEach { classDeclaration ->
            val visitor = LegacyEntryGeneratingVisitor(
                type = type,
                codeGenerator = codeGenerator,
                resolver = resolver,
                logger = logger,
                entryFactoriesRegistry = entryFactoriesRegistry
            )
            classDeclaration.accept(visitor, Unit)
        }

        if (type == ExternalEntry) {
            EntryFactoryProviderGenerator(
                resolver = resolver,
                codeGenerator = codeGenerator,
                logger = logger,
                entryFactoriesRegistry = entryFactoriesRegistry
            ).generate()
        }

        invoked = true
        return emptyList()
    }
}
