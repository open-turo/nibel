package nibel.compiler

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

@AutoService(SymbolProcessorProvider::class)
class UiExternalEntryProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return UiEntryProcessor(
            type = ProcessorType.ExternalEntry,
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
        )
    }
}
