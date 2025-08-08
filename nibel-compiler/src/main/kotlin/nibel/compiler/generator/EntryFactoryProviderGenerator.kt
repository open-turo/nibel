package nibel.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import nibel.compiler.template.entryFactoryProviderTemplate

class EntryFactoryProviderGenerator(
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator,
    private val entryFactoriesRegistry: EntryFactoriesRegistry,
) {

    fun generate() {
        for ((packageName, entryFactoryProviders) in entryFactoriesRegistry.values) {
            if (entryFactoryProviders.isEmpty()) continue

            generateFile(
                packageName = packageName,
                entryFactoryProviders = entryFactoryProviders,
            )
        }
    }

    private fun generateFile(
        packageName: String,
        entryFactoryProviders: List<EntryFactoryProviderMetadata>,
    ) {
        for (provider in entryFactoryProviders) {
            val fileName = "_" + provider.destinationQualifiedName.replace(".", "_")

            @Suppress("SpreadOperator")
            val file = codeGenerator.createNewFile(
                dependencies = Dependencies(
                    aggregating = false,
                    *resolver.getAllFiles().toList().toTypedArray(),
                ),
                packageName = packageName,
                fileName = fileName,
            )

            val content = buildString {
                append("package $packageName")
                append("\n\n")

                val code = entryFactoryProviderTemplate(
                    destinationName = fileName,
                    entryFactoryQualifiedName = provider.entryFactoryQualifiedName,
                )
                append(code)
            }

            file.write(content.toByteArray())
            file.close()
        }
    }
}
