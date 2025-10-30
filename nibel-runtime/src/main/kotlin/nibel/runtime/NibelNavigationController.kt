package nibel.runtime

import android.os.Parcelable
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import nibel.annotations.ExternalDestination
import java.util.UUID

/**
 * Default implementation of [NavigationController]. Depending on circumstances, relies on 2 tools
 * for navigation:
 * - Fragment transaction manager.
 * - Compose navigation library.
 */
open class NibelNavigationController(
    val internalNavController: NavController,
    val fragmentManager: FragmentManager,
    val onBackPressedDispatcher: OnBackPressedDispatcher,
    val exploredEntries: ExploredEntriesRegistry,
    fragmentSpec: FragmentSpec<*> = Nibel.fragmentSpec,
    composeSpec: ComposeSpec<*> = Nibel.composeSpec,
) : NavigationController(fragmentSpec, composeSpec) {

    protected val fragmentTransactionContext =
        FragmentTransactionContext(fragmentManager)

    protected val composeNavigationContext =
        ComposeNavigationContext(internalNavController, exploredEntries)

    // Note: Result callbacks are now stored in ResultCallbackRegistry singleton
    // to persist across Fragment/NavigationController instance recreation

    /**
     * Storage for the current screen's request key (if navigated via navigateForResult).
     * Used to deliver results back to the caller.
     */
    private var currentRequestKey: String? = null

    /**
     * Sets the request key for this navigation controller instance.
     * Used when a Fragment is navigated to via navigateForResult.
     */
    fun setRequestKeyFromFragment(key: String?) {
        currentRequestKey = key
    }

    override fun navigateBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun navigateTo(
        externalDestination: ExternalDestination,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>,
    ) {
        val destinationEntry = Nibel.findEntryFactory(externalDestination)
            ?.newInstance(externalDestination)
            ?: error("Unable to find destination '${externalDestination.javaClass}'")
        navigateTo(entry = destinationEntry, fragmentSpec, composeSpec)
    }

    override fun navigateTo(
        entry: Entry,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>,
    ) {
        when (entry) {
            is ComposableEntry<*> -> navigateTo(entry, composeSpec)
            is FragmentEntry -> navigateTo(entry, fragmentSpec)
        }
    }

    open fun navigateTo(entry: FragmentEntry, fragmentSpec: FragmentSpec<*>) {
        when (fragmentSpec) {
            is FragmentTransactionSpec -> with(fragmentSpec) {
                fragmentTransactionContext.navigateTo(entry)
            }

            else -> error("Unknown fragment navigation spec '${fragmentSpec.javaClass}'")
        }
    }

    open fun navigateTo(entry: ComposableEntry<*>, composeSpec: ComposeSpec<*>) {
        when (composeSpec) {
            is ComposeNavigationSpec -> with(composeSpec) {
                composeNavigationContext.navigateTo(entry)
            }

            else -> error("Unknown compose navigation spec '${composeSpec.javaClass}'")
        }
    }

    override fun <TResult : Parcelable> navigateForResult(
        entry: Entry,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>,
        callback: (TResult?) -> Unit,
    ) {
        // Generate unique request key for this navigation
        val requestKey = UUID.randomUUID().toString()

        // Store the callback with type erasure (we trust compile-time type safety)
        @Suppress("UNCHECKED_CAST")
        ResultCallbackRegistry.storeCallback(requestKey, callback as (Any?) -> Unit)

        // Store request key so the callee can use it to return results
        // For Fragment entries, pass it through Bundle arguments
        // For Composable entries, store it in currentRequestKey
        val previousRequestKey = currentRequestKey
        currentRequestKey = requestKey

        // Inject request key into Fragment arguments for result-based navigation
        if (entry is FragmentEntry) {
            val args = entry.fragment.arguments ?: android.os.Bundle()
            args.putRequestKey(requestKey)
            entry.fragment.arguments = args
        }

        try {
            // Perform the navigation
            navigateTo(entry, fragmentSpec, composeSpec)
        } catch (e: IllegalStateException) {
            // If navigation fails, clean up and restore state
            ResultCallbackRegistry.removeCallback(requestKey)
            currentRequestKey = previousRequestKey
            throw e
        } catch (e: IllegalArgumentException) {
            // If navigation fails, clean up and restore state
            ResultCallbackRegistry.removeCallback(requestKey)
            currentRequestKey = previousRequestKey
            throw e
        }
    }

    override fun <TResult : Parcelable> navigateForResult(
        externalDestination: ExternalDestination,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>,
        callback: (TResult?) -> Unit,
    ) {
        // Find the entry for this destination
        val destinationEntry = Nibel.findEntryFactory(externalDestination)
            ?.newInstance(externalDestination)
            ?: error("Unable to find destination '${externalDestination.javaClass}'")

        // Delegate to the entry-based navigateForResult
        navigateForResult(destinationEntry, fragmentSpec, composeSpec, callback)
    }

    override fun <TResult : Parcelable> setResultAndNavigateBack(result: TResult) {
        val requestKey = currentRequestKey
            ?: error(
                "setResultAndNavigateBack() called but no request key found. " +
                    "This screen was not navigated to via navigateForResult().",
            )

        // Deliver the result through the callback
        deliverResult(requestKey, result)

        // Navigate back
        navigateBack()
    }

    override fun cancelResultAndNavigateBack() {
        val requestKey = currentRequestKey

        if (requestKey != null) {
            // Deliver null result to indicate cancellation
            deliverResult(requestKey, null)
        }

        // Navigate back (this works even if no request key, like regular back)
        navigateBack()
    }

    /**
     * Delivers a result to the callback associated with the given request key.
     * Cleans up the callback after delivery.
     */
    private fun deliverResult(requestKey: String, result: Any?) {
        val callback = ResultCallbackRegistry.removeCallback(requestKey)
        callback?.invoke(result)

        // Clear current request key if it matches
        if (currentRequestKey == requestKey) {
            currentRequestKey = null
        }
    }
}
