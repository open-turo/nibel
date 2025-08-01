package nibel.runtime

import androidx.fragment.app.Fragment
import nibel.annotations.InternalDestination

sealed interface Entry : InternalDestination

@JvmInline
value class FragmentEntry(val fragment: Fragment) : Entry

/**
 * [ResultEntry] represents a screen entry that can return a result after navigation.
 * This interface extends [Entry] to support Activity Result API integration.
 *
 * @param R The type of result that this entry returns
 */
interface ResultEntry<R : Any> : Entry {
    /**
     * The result type class for this entry.
     */
    val resultType: Class<R>
}
