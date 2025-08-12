package nibel.runtime

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.FragmentActivity

/**
 * [NibelResultContract] is an [ActivityResultContract] that handles result-based navigation
 * within Nibel. It launches a screen entry and expects a result of type [R].
 *
 * @param R The type of result expected from the launched screen
 */
class NibelResultContract<R : Any> : ActivityResultContract<ResultEntry<R>, R?>() {

    /**
     * Creates an intent to launch the result-based screen entry.
     * For Nibel's case, this returns null as we handle navigation internally through fragments
     * and compose navigation rather than separate activities.
     */
    override fun createIntent(context: Context, input: ResultEntry<R>): Intent {
        // For Nibel, we don't create separate activities but rather handle navigation
        // internally. This method is required by ActivityResultContract but we'll
        // handle the actual navigation in the NavigationController.
        return Intent()
    }

    /**
     * Parses the result from the intent data.
     * This extracts the result object that was set by the result-returning screen.
     */
    override fun parseResult(resultCode: Int, intent: Intent?): R? {
        if (resultCode != FragmentActivity.RESULT_OK || intent == null) {
            return null
        }

        return intent.extras?.let { bundle ->
            bundle.getParcelable(RESULT_KEY)
        }
    }

    companion object {
        /**
         * Key used to store and retrieve result data from Intent extras.
         */
        const val RESULT_KEY = "nibel_result"

        /**
         * Creates an intent with the result data for returning from a result-based screen.
         */
        fun <R : Any> createResultIntent(result: R): Intent {
            return Intent().apply {
                putExtra(RESULT_KEY, result as? Parcelable)
            }
        }
    }
}
