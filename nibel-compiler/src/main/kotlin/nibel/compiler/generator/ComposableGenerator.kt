package nibel.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import nibel.compiler.template.composableEntryWithArgsTemplate
import nibel.compiler.template.composableEntryWithNoArgsTemplate
import nibel.compiler.template.composableExternalEntryFactoryTemplate
import nibel.compiler.template.composableExternalResultEntryFactoryTemplate
import nibel.compiler.template.composableInternalEntryFactoryTemplate
import nibel.compiler.template.composableResultEntryWithArgsTemplate
import nibel.compiler.template.composableResultEntryWithNoArgsTemplate
import nibel.compiler.template.composableResultInternalEntryFactoryTemplate

@Suppress("unused", "LongMethod")
class ComposableGenerator(
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val entryFactoriesRegistry: EntryFactoriesRegistry,
) {
    private val composableContentGenerator by lazy {
        ComposableContentGenerator()
    }

    fun generate(function: KSFunctionDeclaration, metadata: EntryMetadata) {
        val packageName = function.packageName.asString()

        val composableSimpleName = function.simpleName.asString()
        val composableHolderName = "${composableSimpleName}Entry"

        val composableQualifiedName = function.qualifiedName!!.asString()

        val composableEntryFactory = when (metadata) {
            is ExternalEntryMetadata -> {
                entryFactoriesRegistry[metadata.destinationPackageName] =
                    EntryFactoryProviderMetadata(
                        packageName = metadata.destinationPackageName,
                        destinationName = metadata.destinationName,
                        destinationQualifiedName = metadata.destinationQualifiedName,
                        entryFactoryQualifiedName = "$packageName.$composableHolderName.Companion",
                    )

                composableExternalEntryFactoryTemplate(
                    composableHolderName = composableHolderName,
                    destinationQualifiedName = metadata.destinationQualifiedName,
                    hasArgs = metadata.argsQualifiedName != null,
                    argsQualifiedName = metadata.argsQualifiedName,
                )
            }

            is InternalEntryMetadata -> {
                if (metadata.argsQualifiedName != null) {
                    composableInternalEntryFactoryTemplate(
                        composableHolderName = composableHolderName,
                        argsQualifiedName = metadata.argsQualifiedName!!,
                    )
                } else {
                    composableInternalEntryFactoryTemplate(
                        composableHolderName = composableHolderName,
                    )
                }
            }
        }

        val composableContent = composableContentGenerator.generate(
            composableQualifiedName = composableQualifiedName,
            parameters = metadata.parameters,
        )

        val resultQualifiedName = metadata.resultQualifiedName
        if (resultQualifiedName != null) {
            // For result entries, create result-specific factory
            val resultEntryFactory = when (metadata) {
                is InternalEntryMetadata -> {
                    if (metadata.argsQualifiedName != null) {
                        composableResultInternalEntryFactoryTemplate(
                            composableHolderName = composableHolderName,
                            argsQualifiedName = metadata.argsQualifiedName!!,
                            resultQualifiedName = resultQualifiedName,
                        )
                    } else {
                        composableResultInternalEntryFactoryTemplate(
                            composableHolderName = composableHolderName,
                            resultQualifiedName = resultQualifiedName,
                        )
                    }
                }
                is ExternalEntryMetadata -> {
                    // For external result entries, create result-specific factory
                    composableExternalResultEntryFactoryTemplate(
                        composableHolderName = composableHolderName,
                        destinationQualifiedName = metadata.destinationQualifiedName,
                        hasArgs = metadata.argsQualifiedName != null,
                        argsQualifiedName = metadata.argsQualifiedName,
                        resultQualifiedName = resultQualifiedName,
                    )
                }
            }

            generateComposableResultEntry(
                packageName = packageName,
                composableHolderName = composableHolderName,
                argsQualifiedName = metadata.argsQualifiedName,
                resultQualifiedName = resultQualifiedName,
                composableContent = composableContent,
                composableEntryFactory = resultEntryFactory,
            )
        } else {
            generateComposableEntry(
                packageName = packageName,
                composableHolderName = composableHolderName,
                argsQualifiedName = metadata.argsQualifiedName,
                composableContent = composableContent,
                composableEntryFactory = composableEntryFactory,
            )
        }
    }

    private fun generateComposableEntry(
        packageName: String,
        composableHolderName: String,
        argsQualifiedName: String?,
        composableContent: String,
        composableEntryFactory: String,
    ) {
        @Suppress("SpreadOperator")
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = false,
                *resolver.getAllFiles().toList().toTypedArray(),
            ),
            packageName = packageName,
            fileName = composableHolderName,
        )

        val content = if (argsQualifiedName != null) {
            composableEntryWithArgsTemplate(
                packageName = packageName,
                composableHolderName = composableHolderName,
                composableContent = composableContent,
                argsQualifiedName = argsQualifiedName,
                composableEntryFactory = composableEntryFactory,
            )
        } else {
            composableEntryWithNoArgsTemplate(
                packageName = packageName,
                composableHolderName = composableHolderName,
                composableContent = composableContent,
                composableEntryFactory = composableEntryFactory,
            )
        }

        file.write(content.toByteArray())
        file.close()
    }

    private fun generateComposableResultEntry(
        packageName: String,
        composableHolderName: String,
        argsQualifiedName: String?,
        resultQualifiedName: String,
        composableContent: String,
        composableEntryFactory: String,
    ) {
        @Suppress("SpreadOperator")
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = false,
                *resolver.getAllFiles().toList().toTypedArray(),
            ),
            packageName = packageName,
            fileName = composableHolderName,
        )

        val content = if (argsQualifiedName != null) {
            composableResultEntryWithArgsTemplate(
                packageName = packageName,
                composableHolderName = composableHolderName,
                argsQualifiedName = argsQualifiedName,
                resultTypeQualifiedName = resultQualifiedName,
                composableContent = composableContent,
                composableEntryFactory = composableEntryFactory,
            )
        } else {
            composableResultEntryWithNoArgsTemplate(
                packageName = packageName,
                composableHolderName = composableHolderName,
                resultTypeQualifiedName = resultQualifiedName,
                composableContent = composableContent,
                composableEntryFactory = composableEntryFactory,
            )
        }

        file.write(content.toByteArray())
        file.close()
    }
}
