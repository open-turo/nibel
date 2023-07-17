package com.turo.nibel.compiler.generator

class EntryFactoriesRegistry(
    val values: MutableMap<String, MutableList<EntryFactoryProviderMetadata>> = mutableMapOf()
) {

    operator fun set(packageName: String, provider: EntryFactoryProviderMetadata) {
        if (packageName !in values) {
            values[packageName] = mutableListOf()
        }
        values[packageName]!! += provider
    }
}
