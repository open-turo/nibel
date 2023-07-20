package nibel.runtime

import nibel.annotations.ExternalDestination
import nibel.annotations.ImplementationType

/**
 * Creates instance of an screen entry.
 */
sealed interface EntryFactory<D : ExternalDestination, E : Entry> {
    fun newInstance(destination: D): E
}

/**
 * Creates instance of a fragment entry.
 *
 * See [ImplementationType.Fragment].
 */
interface FragmentEntryFactory<D : ExternalDestination> : EntryFactory<D, FragmentEntry>

/**
 * Creates instance of a composable entry.
 *
 * See [ImplementationType.Composable].
 */
interface ComposableEntryFactory<D : ExternalDestination> : EntryFactory<D, ComposableEntry<*>>
