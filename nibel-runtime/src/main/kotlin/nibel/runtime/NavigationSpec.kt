package nibel.runtime

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
