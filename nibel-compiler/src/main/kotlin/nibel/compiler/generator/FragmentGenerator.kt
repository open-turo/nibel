package nibel.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import nibel.compiler.template.fragmentEntryTemplate
import nibel.compiler.template.fragmentExternalEntryFactoryTemplate
import nibel.compiler.template.fragmentExternalResultEntryFactoryTemplate
import nibel.compiler.template.fragmentInternalEntryFactoryTemplate
import nibel.compiler.template.fragmentResultEntryTemplate
import nibel.compiler.template.fragmentResultInternalEntryFactoryTemplate

@Suppress("unused")
class FragmentGenerator(
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
        val fragmentName = "${composableSimpleName}Entry"
        val composableQualifiedName = function.qualifiedName!!.asString()

        val fragmentEntryFactory = generateFragmentEntryFactory(metadata, packageName, fragmentName)
        val composableContent = composableContentGenerator.generate(
            composableQualifiedName = composableQualifiedName,
            parameters = metadata.parameters,
        )

        val resultQualifiedName = metadata.resultQualifiedName
        if (resultQualifiedName != null) {
            val resultEntryFactory = generateResultEntryFactory(
                metadata,
                packageName,
                fragmentName,
                resultQualifiedName,
            )
            generateFragmentResultFile(
                packageName = packageName,
                fragmentName = fragmentName,
                resultQualifiedName = resultQualifiedName,
                composableContent = composableContent,
                fragmentResultEntryFactory = resultEntryFactory,
            )
        } else {
            generateFragmentFile(
                packageName = packageName,
                fragmentName = fragmentName,
                composableContent = composableContent,
                fragmentEntryFactory = fragmentEntryFactory,
            )
        }
    }

    private fun generateFragmentEntryFactory(
        metadata: EntryMetadata,
        packageName: String,
        fragmentName: String,
    ): String {
        return when (metadata) {
            is ExternalEntryMetadata -> {
                // Only register non-result factories - result factories are registered separately
                if (metadata.resultQualifiedName == null) {
                    entryFactoriesRegistry[metadata.destinationPackageName] =
                        EntryFactoryProviderMetadata(
                            packageName = metadata.destinationPackageName,
                            destinationName = metadata.destinationName,
                            destinationQualifiedName = metadata.destinationQualifiedName,
                            entryFactoryQualifiedName = "$packageName.$fragmentName.Companion",
                        )
                }

                fragmentExternalEntryFactoryTemplate(
                    objectName = "companion object",
                    fragmentName = fragmentName,
                    destinationQualifiedName = metadata.destinationQualifiedName,
                    hasArgs = metadata.argsQualifiedName != null,
                    argsQualifiedName = metadata.argsQualifiedName,
                )
            }

            is InternalEntryMetadata -> {
                if (metadata.argsQualifiedName != null) {
                    fragmentInternalEntryFactoryTemplate(
                        objectName = "companion object",
                        fragmentName = fragmentName,
                        argsQualifiedName = metadata.argsQualifiedName!!,
                    )
                } else {
                    fragmentInternalEntryFactoryTemplate(
                        objectName = "companion object",
                        fragmentName = fragmentName,
                    )
                }
            }
        }
    }

    private fun generateResultEntryFactory(
        metadata: EntryMetadata,
        packageName: String,
        fragmentName: String,
        resultQualifiedName: String,
    ): String {
        return when (metadata) {
            is InternalEntryMetadata -> {
                if (metadata.argsQualifiedName != null) {
                    fragmentResultInternalEntryFactoryTemplate(
                        objectName = "companion object",
                        fragmentName = fragmentName,
                        argsQualifiedName = metadata.argsQualifiedName!!,
                        resultQualifiedName = resultQualifiedName,
                    )
                } else {
                    fragmentResultInternalEntryFactoryTemplate(
                        objectName = "companion object",
                        fragmentName = fragmentName,
                        resultQualifiedName = resultQualifiedName,
                    )
                }
            }
            is ExternalEntryMetadata -> {
                // Register the result entry factory
                entryFactoriesRegistry[metadata.destinationPackageName] =
                    EntryFactoryProviderMetadata(
                        packageName = metadata.destinationPackageName,
                        destinationName = metadata.destinationName,
                        destinationQualifiedName = metadata.destinationQualifiedName,
                        entryFactoryQualifiedName = "$packageName.${fragmentName}Fragment.Companion",
                    )

                // For external result entries, create result-specific factory
                fragmentExternalResultEntryFactoryTemplate(
                    objectName = "companion object",
                    fragmentName = fragmentName,
                    destinationQualifiedName = metadata.destinationQualifiedName,
                    hasArgs = metadata.argsQualifiedName != null,
                    argsQualifiedName = metadata.argsQualifiedName,
                    resultQualifiedName = resultQualifiedName,
                )
            }
        }
    }

    private fun generateFragmentFile(
        packageName: String,
        fragmentName: String,
        composableContent: String,
        fragmentEntryFactory: String,
    ) {
        @Suppress("SpreadOperator")
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = false,
                *resolver.getAllFiles().toList().toTypedArray(),
            ),
            packageName = packageName,
            fileName = fragmentName,
        )

        val content = fragmentEntryTemplate(
            packageName = packageName,
            fragmentName = fragmentName,
            composableContent = composableContent,
            fragmentEntryFactory = fragmentEntryFactory,
        )

        file.write(content.toByteArray())
        file.close()
    }

    private fun generateFragmentResultFile(
        packageName: String,
        fragmentName: String,
        resultQualifiedName: String,
        composableContent: String,
        fragmentResultEntryFactory: String,
    ) {
        @Suppress("SpreadOperator")
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = false,
                *resolver.getAllFiles().toList().toTypedArray(),
            ),
            packageName = packageName,
            fileName = fragmentName,
        )

        val content = fragmentResultEntryTemplate(
            packageName = packageName,
            fragmentName = fragmentName,
            resultTypeQualifiedName = resultQualifiedName,
            composableContent = composableContent,
            fragmentResultEntryFactory = fragmentResultEntryFactory,
        )

        file.write(content.toByteArray())
        file.close()
    }
}
