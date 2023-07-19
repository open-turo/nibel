package nibel.runtime

import android.os.Parcelable
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private const val ROOT_ROUTE = "@root"

interface NavigationDelegate<T> {

    @Composable
    fun rememberNavigationController(args: T): NavigationController

    @Composable
    fun Content(rootArgs: Parcelable?, content: @Composable () -> Unit)
}

data class NavigationControllerArgs(
    val internalNavController: NavController,
    val exploredEntries: ExploredEntriesRegistry,
)

open class ComposeNavigationDelegate : NavigationDelegate<NavigationControllerArgs> {

    @Composable
    override fun rememberNavigationController(args: NavigationControllerArgs): NavigationController {
        val activity = LocalContext.current as FragmentActivity
        val onBackPressedDispatcher =
            LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher

        return remember(args.internalNavController) {
            DefaultNavigationController(
                fragmentManager = activity.supportFragmentManager,
                onBackPressedDispatcher = onBackPressedDispatcher,
                internalNavController = args.internalNavController,
                exploredEntries = args.exploredEntries
            )
        }
    }

    @Suppress("DEPRECATION")
    @Composable
    override fun Content(rootArgs: Parcelable?, content: @Composable () -> Unit) {
        val internalNavController = rememberNavController()
        val exploredEntries = rememberSaveable(
            saver = ExploredEntriesSaver(),
            init = { ExploredEntriesRegistry() },
        )

        val navController = rememberNavigationController(
            NavigationControllerArgs(
                internalNavController = internalNavController,
                exploredEntries = exploredEntries
            )
        )

        CompositionLocalProvider(LocalNavigationController provides navController) {
            NavHost(internalNavController, startDestination = ROOT_ROUTE) {
                registerComposable(
                    name = ROOT_ROUTE,
                    argsType = rootArgs?.javaClass,
                    defaultArgs = rootArgs,
                ) {
                    val args = it.arguments?.getParcelable<Parcelable?>(Nibel.argsKey)
                    CompositionLocalProvider(LocalArgs provides args) {
                        content()
                    }
                }
                for (entry in exploredEntries) {
                    registerComposable(
                        name = entry.name,
                        argsType = entry.args?.javaClass,
                        defaultArgs = null
                    ) {
                        val args = it.arguments?.getParcelable<Parcelable?>(Nibel.argsKey)
                        CompositionLocalProvider(LocalArgs provides args) {
                            entry.ComposableEntry()
                        }
                    }
                }
            }
        }
    }

    private fun <A : Parcelable> NavGraphBuilder.registerComposable(
        name: String,
        argsType: Class<A>?,
        defaultArgs: A?,
        content: @Composable (NavBackStackEntry) -> Unit
    ) {
        if (argsType == null) {
            composable(route = name) {
                content(it)
            }
        } else {
            val route = if (defaultArgs == null) {
                "${name}/{${Nibel.argsKey}}"
            } else {
                "${name}?${Nibel.argsKey}={${Nibel.argsKey}}"
            }
            composable(
                route = route,
                arguments = listOf(
                    navArgument(Nibel.argsKey) {
                        type = ParcelableType(argsType)
                        if (defaultArgs != null) {
                            defaultValue = defaultArgs
                        }
                    }
                )
            ) {
                content(it)
            }
        }
    }
}
