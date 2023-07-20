package nibel.runtime

import androidx.fragment.app.Fragment
import nibel.annotations.InternalDestination

sealed interface Entry : InternalDestination

@JvmInline
value class FragmentEntry(val fragment: Fragment) : Entry
