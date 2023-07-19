package nibel.runtime

import nibel.annotations.ExternalDestination

sealed interface EntryFactory<D : ExternalDestination, E : Entry> {
    fun newInstance(destination: D): E
}

interface FragmentEntryFactory<D : ExternalDestination> : EntryFactory<D, FragmentEntry>

interface ComposableEntryFactory<D : ExternalDestination> : EntryFactory<D, ComposableEntry<*>>
