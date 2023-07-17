package com.turo.nibel.compiler

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.turo.nibel.compiler.ProcessorType.InternalEntry

@AutoService(SymbolProcessorProvider::class)
class LegacyInternalEntryProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return LegacyEntryProcessor(
            type = InternalEntry,
            options = environment.options,
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
        )
    }
}
