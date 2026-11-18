package nibel.runtime

import android.os.Parcelable
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.ComposeNavigator

/**
 * A holder for classes required to navigate with compose navigation library.
 */
class ComposeNavigationContext(
    val internalNavController: NavController,
    val exploredEntries: ExploredEntriesRegistry,
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
                content = entryDestinationContent(entry),
            )

            if (entry.args != null) {
                destination.route = "${entry.name}/{${Nibel.argsKey}}"
                destination.addArgument(
                    argumentName = Nibel.argsKey,
                    argument = NavArgument.Builder()
                        .setType(type = ParcelableType(entry.args!!.javaClass))
                        .build(),
                )
            } else {
                destination.route = entry.name
            }

            internalNavController.graph.addDestination(destination)
        }

        internalNavController.navigate(route)
    }
}

/**
 * Content of the destination of a composable [entry]. Created in one place for every destination,
 * so the composition, and therefore the saved state of the screen, is the same whether the
 * destination was added on navigation or registered again after the activity is recreated.
 */
internal fun entryDestinationContent(
    entry: ComposableEntry<*>,
): @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = {
    val args = it.arguments?.getNibelArgs<Parcelable>()
    CompositionLocalProvider(LocalArgs provides args) {
        entry.ComposableEntry()
    }
}
