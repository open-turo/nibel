package com.turo.nibel.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.turo.nibel.compiler.template.fragmentExternalEntryFactoryTemplate
import com.turo.nibel.compiler.template.fragmentInternalEntryFactoryTemplate
import com.turo.nibel.compiler.template.legacyEntryTemplate

class LegacyEntryGenerator(
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val entryFactoriesRegistry: EntryFactoriesRegistry
) {

    fun generate(classDeclaration: KSClassDeclaration, metadata: EntryMetadata) {
        val packageName = classDeclaration.packageName.asString()

        val fragmentName = classDeclaration.simpleName.asString()
        val legacyEntryName = "${fragmentName}Entry"
        val objectName = "object $legacyEntryName"

        val legacyEntryFactory = when (metadata) {
            is ExternalEntryMetadata -> {
                entryFactoriesRegistry[metadata.destinationPackageName] =
                    EntryFactoryProviderMetadata(
                        packageName = metadata.destinationPackageName,
                        destinationName = metadata.destinationName,
                        destinationQualifiedName = metadata.destinationQualifiedName,
                        entryFactoryQualifiedName = "${packageName}.${legacyEntryName}"
                    )

                fragmentExternalEntryFactoryTemplate(
                    objectName = objectName,
                    fragmentName = fragmentName,
                    destinationQualifiedName = metadata.destinationQualifiedName,
                    hasArgs = metadata.argsQualifiedName != null,
                    argsQualifiedName = metadata.argsQualifiedName,
                )
            }

            is InternalEntryMetadata -> {
                if (metadata.argsQualifiedName != null) {
                    fragmentInternalEntryFactoryTemplate(
                        objectName = objectName,
                        fragmentName = fragmentName,
                        argsQualifiedName = metadata.argsQualifiedName!!,
                    )
                } else {
                    fragmentInternalEntryFactoryTemplate(
                        objectName = objectName,
                        fragmentName = fragmentName,
                    )
                }
            }
        }

        generateLegacyEntryFile(
            packageName = packageName,
            legacyEntryName = legacyEntryName,
            fragmentEntryFactory = legacyEntryFactory.trimIndent(),
        )
    }

    private fun generateLegacyEntryFile(
        packageName: String,
        legacyEntryName: String,
        fragmentEntryFactory: String,
    ) {
        @Suppress("SpreadOperator")
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = false,
                *resolver.getAllFiles().toList().toTypedArray(),
            ),
            packageName = packageName,
            fileName = legacyEntryName,
        )

        val content = legacyEntryTemplate(
            packageName = packageName,
            fragmentEntryFactory = fragmentEntryFactory,
        )

        file.write(content.toByteArray())
        file.close()
    }
}
