package com.turo.nibel.runtime

import androidx.compose.runtime.saveable.Saver

data class ExploredEntriesRegistry internal constructor(
    internal val entries: MutableMap<String, ComposableEntry<*>> = mutableMapOf(),
) {
    fun add(entry: ComposableEntry<*>) {
        entries[entry.name] = entry
    }

    operator fun plusAssign(entry: ComposableEntry<*>) = add(entry)

    operator fun iterator() = entries.values.iterator()
}

@Suppress("FunctionName")
fun ExploredEntriesSaver(): Saver<ExploredEntriesRegistry, *> = Saver(
    save = { it.entries },
    restore = { ExploredEntriesRegistry(it) },
)
