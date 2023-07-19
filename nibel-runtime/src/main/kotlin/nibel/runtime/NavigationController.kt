package nibel.runtime

import nibel.annotations.ExternalDestination
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.annotations.UiExternalEntry

/**
 * [NavigationController] is a core component of Nibel that enables navigation between screens of
 * the app.
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
     * Perform navigation back.
     */
    abstract fun navigateBack()

    /**
     * Perform navigation to an external entry declared in a different feature module.
     *
     * @param externalDestination destination used for multi-module navigation
     * @param fragmentSpec specification used when navigation to a fragment is required. This could
     * be a regular fragment or compose screen entry annotated with [ImplementationType.Fragment].
     * @param composeSpec specification used when navigation to a compose screen is required.
     */
    abstract fun navigateTo(
        externalDestination: ExternalDestination,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
    )

    /**
     * Perform a navigation to a screen by having a direct reference to its generated entry type.
     *
     * @param entry screen entry reference used for navigation
     * @param fragmentSpec specification used when navigation to a fragment is required. This could
     * be a regular fragment or compose screen entry annotated with [ImplementationType.Fragment].
     * @param composeSpec specification used when navigation to a compose screen is required.
     */
    abstract fun navigateTo(
        entry: Entry,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
    )
}
