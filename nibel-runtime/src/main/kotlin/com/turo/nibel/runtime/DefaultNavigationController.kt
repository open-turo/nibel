package com.turo.nibel.runtime

import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.turo.nibel.annotations.ExternalDestination
import com.turo.nibel.annotations.ImplementationType

/**
 * Default implementation of [NavigationController]. Depending on the
 * - fragment transactions for navigation to fragments or compose screens annotated with [ImplementationType.Fragment]
 * - compose navigation library for navigation
 */
open class DefaultNavigationController(
    val internalNavController: NavController,
    val fragmentManager: FragmentManager,
    val onBackPressedDispatcher: OnBackPressedDispatcher,
    val exploredEntries: ExploredEntriesRegistry
) : NavigationController() {

    private val fragmentTransactionContext =
        FragmentTransactionContext(fragmentManager)

    private val composeNavigationContext =
        ComposeNavigationContext(internalNavController, exploredEntries)

    override fun navigateBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun navigateTo(
        externalDestination: ExternalDestination,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>
    ) {
        val destinationEntry = Nibel
            .findEntryFactory(externalDestination)
            ?.newInstance(externalDestination)
            ?: error("Unable to find destination '${externalDestination.javaClass}'")
        navigateTo(entry = destinationEntry, fragmentSpec, composeSpec)
    }

    override fun navigateTo(
        entry: Entry,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>
    ) {
        when (entry) {
            is ComposableEntry<*> -> when (composeSpec) {
                is ComposeNavigationSpec -> with(composeSpec) {
                    composeNavigationContext.navigateTo(entry)
                }

                else -> error("Unknown compose navigation spec '${composeSpec.javaClass}'")
            }

            is FragmentEntry -> when (fragmentSpec) {
                is FragmentTransactionSpec -> with(fragmentSpec) {
                    fragmentTransactionContext.navigateTo(entry)
                }

                else -> error("Unknown fragment navigation spec '${fragmentSpec.javaClass}'")
            }
        }
    }
}
