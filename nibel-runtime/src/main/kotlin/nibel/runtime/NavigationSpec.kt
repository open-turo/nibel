package nibel.runtime

import androidx.compose.runtime.CompositionLocalProvider
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.compose.ComposeNavigator

/**
 * [NavigationSpec] holds implementation details for navigation between certain types of screens.
 *
 * @param C a holder for classes required to perform a navigation in [navigateTo].
 */
sealed interface NavigationSpec<C, E : Entry> {

    fun C.navigateTo(entry: E)
}

/**
 * If [NavigationController] requests a navigation to a fragment, it is delegated to a descendant of
 * a [FragmentSpec].
 */
interface FragmentSpec<C> : NavigationSpec<C, FragmentEntry>

/**
 * If [NavigationController] requests a navigation to a composable function, it is delegated to a
 * descendant of [ComposeSpec].
 */
interface ComposeSpec<C> : NavigationSpec<C, ComposableEntry<*>>

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

/**
 * A holder for classes required to navigate with compose navigation library.
 */
class ComposeNavigationContext(
    val internalNavController: NavController,
    val exploredEntries: ExploredEntriesRegistry
)

/**
 * [ComposeSpec] that performs navigation with compose navigation library under-the-hood.
 */
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
