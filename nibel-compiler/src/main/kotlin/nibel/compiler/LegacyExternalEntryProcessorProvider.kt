package nibel.compiler

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import nibel.compiler.ProcessorType.ExternalEntry

@AutoService(SymbolProcessorProvider::class)
class LegacyExternalEntryProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return LegacyEntryProcessor(
            type = ExternalEntry,
            options = environment.options,
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
        )
    }
}
