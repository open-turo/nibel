package nibel.runtime

import androidx.fragment.app.FragmentManager

/**
 * A holder for classes required to perform fragment transactions.
 */
class FragmentTransactionContext(
    val fragmentManager: FragmentManager
)

/**
 * [FragmentSpec] that performs fragment transactions under-the-hood.
 */
open class FragmentTransactionSpec(
    val replace: Boolean = true,
    val addToBackStack: Boolean = true,
    val containerId: Int = android.R.id.content,
) : FragmentSpec<FragmentTransactionContext> {

    override fun FragmentTransactionContext.navigateTo(entry: FragmentEntry) {
        val transaction = fragmentManager.beginTransaction()

        if (replace) {
            transaction.replace(containerId, entry.fragment)
        } else {
            transaction.add(containerId, entry.fragment)
        }

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }
}
