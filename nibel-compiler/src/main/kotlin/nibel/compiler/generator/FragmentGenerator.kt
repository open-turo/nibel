package nibel.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import nibel.compiler.template.fragmentEntryTemplate
import nibel.compiler.template.fragmentExternalEntryFactoryTemplate
import nibel.compiler.template.fragmentInternalEntryFactoryTemplate

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

        val fragmentEntryFactory = when (metadata) {
            is ExternalEntryMetadata -> {
                entryFactoriesRegistry[metadata.destinationPackageName] =
                    EntryFactoryProviderMetadata(
                        packageName = metadata.destinationPackageName,
                        destinationName = metadata.destinationName,
                        destinationQualifiedName = metadata.destinationQualifiedName,
                        entryFactoryQualifiedName = "$packageName.$fragmentName.Companion",
                    )

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

        val composableContent = composableContentGenerator.generate(
            composableQualifiedName = composableQualifiedName,
            parameters = metadata.parameters,
        )

        generateFragmentFile(
            packageName = packageName,
            fragmentName = fragmentName,
            composableContent = composableContent,
            fragmentEntryFactory = fragmentEntryFactory,
        )
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
}
