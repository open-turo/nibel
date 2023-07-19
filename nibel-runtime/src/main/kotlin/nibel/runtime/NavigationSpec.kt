package nibel.runtime

import androidx.compose.runtime.CompositionLocalProvider
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.compose.ComposeNavigator

/**
 * [NavigationSpec] defines a type and implementation details of a navigation between certain types
 * of screens.
 *
 * @param C is a simple object that holds a navigation context represented by an object with
 * components required for navigation in a certain spec.
 */
sealed interface NavigationSpec<C, E : Entry> {

    fun C.navigateTo(entry: E)
}

/**
 * If [NavigationController] requests a navigation to a fragment, a descendant of a [FragmentSpec]
 * defines implementation details of the navigation.
 */
interface FragmentSpec<C> : NavigationSpec<C, FragmentEntry>

/**
 * If [NavigationController] requests a navigation to a composable function directly, a descendant
 * of a [ComposeSpec] defines implementation details of the navigation.
 */
interface ComposeSpec<C> : NavigationSpec<C, ComposableEntry<*>>

/**
 * A simple class that holds components required to perform a fragment transaction.
 */
class FragmentTransactionContext(
    val fragmentManager: FragmentManager
)

/**
 * Implementation of a [FragmentSpec] that performs a fragment transaction.
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

class ComposeNavigationContext(
    val internalNavController: NavController,
    val exploredEntries: ExploredEntriesRegistry
)

open class ComposeNavigationSpec : ComposeSpec<ComposeNavigationContext> {

    override fun ComposeNavigationContext.navigateTo(entry: ComposableEntry<*>) {
        val route = if (entry.args != null) {
            "${entry.name}/" + Nibel.serializer.serialize(entry.args!!)
        } else {
            entry.name
        }

        if (internalNavController.graph.findNode(route) == null) {
            exploredEntries += entry

            val navigator = internalNavController.navigatorProvider
                .getNavigator(ComposeNavigator::class.java)

            val destination = ComposeNavigator.Destination(
                navigator = navigator,
                content = {
                    CompositionLocalProvider(LocalArgs provides entry.args) {
                        entry.ComposableEntry()
                    }
                }
            )

            if (entry.args != null) {
                destination.route = "${entry.name}/{${Nibel.argsKey}}"
                destination.addArgument(
                    argumentName = Nibel.argsKey,
                    argument = NavArgument.Builder()
                        .setType(type = ParcelableType(entry.args!!.javaClass))
                        .build()
                )
            } else {
                destination.route = entry.name
            }

            internalNavController.graph.addDestination(destination)
        }

        internalNavController.navigate(route)
    }
}
