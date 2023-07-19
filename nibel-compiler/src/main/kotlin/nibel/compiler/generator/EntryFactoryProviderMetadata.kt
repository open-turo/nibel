package nibel.compiler.generator

data class EntryFactoryProviderMetadata(
    val packageName: String,
    val destinationName: String,
    val destinationQualifiedName: String,
    val entryFactoryQualifiedName: String
)
