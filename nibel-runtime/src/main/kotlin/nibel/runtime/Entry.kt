package nibel.runtime

import androidx.fragment.app.Fragment
import nibel.annotations.InternalDestination

/**
 * See [FragmentEntry] and [ComposableEntry].
 */
sealed interface Entry : InternalDestination

@JvmInline
value class FragmentEntry(val fragment: Fragment) : Entry
