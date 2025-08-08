package nibel.runtime

import android.os.Parcelable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavArgument
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
                content = {
                    val args = it.arguments?.getNibelArgs<Parcelable>()
                    CompositionLocalProvider(LocalArgs provides args) {
                        entry.ComposableEntry()
                    }
                },
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
