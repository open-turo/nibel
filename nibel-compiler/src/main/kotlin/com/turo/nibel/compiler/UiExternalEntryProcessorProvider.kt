package com.turo.nibel.compiler

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.turo.nibel.compiler.ProcessorType
import com.turo.nibel.compiler.UiEntryProcessor

@AutoService(SymbolProcessorProvider::class)
class UiExternalEntryProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return UiEntryProcessor(
            type = ProcessorType.ExternalEntry,
            options = environment.options,
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
        )
    }
}
