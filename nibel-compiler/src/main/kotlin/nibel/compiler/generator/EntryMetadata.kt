package nibel.compiler.generator

sealed interface EntryMetadata {
    val argsQualifiedName: String?
    val parameters: Map<ParameterType, ParameterMetadata>
}

data class ExternalEntryMetadata(
    val destinationName: String,
    val destinationPackageName: String,
    val destinationQualifiedName: String,
    override val argsQualifiedName: String?,
    override val parameters: Map<ParameterType, ParameterMetadata>,
) : EntryMetadata

data class InternalEntryMetadata(
    override val argsQualifiedName: String?,
    override val parameters: Map<ParameterType, ParameterMetadata>
) : EntryMetadata
