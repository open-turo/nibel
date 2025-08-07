package nibel.runtime

import nibel.annotations.ExternalDestination
import nibel.annotations.UiEntry
import nibel.annotations.UiExternalEntry

/**
 * [NavigationController] is a core component for navigation between screens of the app.
 * See [UiEntry], [UiExternalEntry]
 *
 * ```
 * @UiEntry(...)
 * @Composable
 * fun FooScreen(navigator: NavigationController) {
 *
 *   navigator.navigateTo(BarScreenEntry.newInstance())
 *   // OR
 *   navigator.navigatorTo(BarDestination)
 * }
 * ```
 */
abstract class NavigationController(
    val fragmentSpec: FragmentSpec<*> = Nibel.fragmentSpec,
    val composeSpec: ComposeSpec<*> = Nibel.composeSpec
) {

    /**
     * Perform back navigation.
     */
    abstract fun navigateBack()

    /**
     * Perform navigation to an external entry declared in another module.
     */
    abstract fun navigateTo(
        externalDestination: ExternalDestination,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
    )

    /**
     * Perform a navigation to a screen by having a direct reference to its generated entry type.
     */
    abstract fun navigateTo(
        entry: Entry,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
    )
}
