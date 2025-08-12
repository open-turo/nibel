package nibel.runtime

import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import nibel.annotations.ExternalDestination

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
            else -> error("Unknown entry type: ${entry.javaClass}")
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

    // Result navigation support
    private var currentResultKey: String? = null
    private var currentResultCallback: ResultCallback<*>? = null

    override fun <R : Any> navigateForResult(
        entry: ResultEntry<R>,
        callback: ResultCallback<R>,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>,
    ) {
        // Generate a unique key for this result navigation
        val resultKey = "result_${System.currentTimeMillis()}_${entry.hashCode()}"

        // Store the callback
        resultCallbackRegistry.register(resultKey, callback)
        currentResultKey = resultKey
        currentResultCallback = callback

        // Navigate normally - the result will be handled when setResultAndNavigateBack is called
        navigateTo(entry as Entry, fragmentSpec, composeSpec)
    }

    override fun <R : Any> navigateForResult(
        destination: ExternalDestination,
        callback: ResultCallback<R>,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>,
    ) {
        val destinationEntry = Nibel.findEntryFactory(destination)
            ?.newInstance(destination) as? ResultEntry<R>
            ?: error("Unable to find result destination '${destination.javaClass}' or destination is not a ResultEntry")

        navigateForResult(destinationEntry, callback, fragmentSpec, composeSpec)
    }

    override fun <R : Any> setResultAndNavigateBack(result: R) {
        currentResultKey?.let { key ->
            val callback = resultCallbackRegistry.consume<R>(key)
            callback?.onResult(result)
        }
        clearCurrentResult()
        navigateBack()
    }

    override fun cancelResultAndNavigateBack() {
        currentResultKey?.let { key ->
            val callback = resultCallbackRegistry.consume<Any>(key)
            callback?.onResult(null)
        }
        clearCurrentResult()
        navigateBack()
    }

    private fun clearCurrentResult() {
        currentResultKey = null
        currentResultCallback = null
    }
}
