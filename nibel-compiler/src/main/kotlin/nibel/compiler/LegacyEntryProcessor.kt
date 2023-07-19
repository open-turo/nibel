package nibel.compiler

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import nibel.annotations.LegacyExternalEntry
import nibel.annotations.LegacyEntry
import nibel.compiler.ProcessorType.ExternalEntry
import nibel.compiler.ProcessorType.InternalEntry
import nibel.compiler.generator.EntryFactoriesRegistry
import nibel.compiler.generator.EntryFactoryProviderGenerator
import nibel.compiler.generator.LegacyEntryGeneratingVisitor

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
