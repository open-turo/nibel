package nibel.annotations

import nibel.annotations.ImplementationType.Composable
import nibel.annotations.ImplementationType.Fragment
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

/**
 * [UiExternalEntry] includes all the functionality of [UiEntry] but additionally enables a
 * multi-module navigation.
 *
 * Normally, a multi-module app consists of a number of feature modules that do not depend on each
 * directly. This makes it impossible to have a direct reference to a generated screen's entry and
 * perform a navigation.
 *
 * The key component in multi-module navigation is a destination which is a lightweight data type
 * that serves as a navigation intent. See [ExternalDestination].
 *
 * Destinations are declared in a separate navigation module and each is associated with exactly one
 * screen. When a screen from another feature module performs a navigation, it uses a corresponding
 * destination instance to open its associated screen.
 *
 * ```
 * featureA          featureB
 *  module            module
 *    │                  │
 *    └──► navigation ◄──┘
 *           module
 * ```
 *
 * ### Basic usage
 * For each external entry, a corresponding destination type should be declared. In the simplest
 * case it should be an `object` that implements [DestinationWithNoArgs].
 *
 * ```
 * // navigation module, available to other feature modules
 * object FooDestination : DestinationWithNoArgs
 * ```
 * Each destination should be associated with exactly one screen using a [UiExternalEntry] annotation
 * ```
 * // feature module
 * @UiExternalEntry(
 *   type = ImplementationType.Fragment
 *   destination = FooDestination::class
 * )
 * @Composable
 * fun FooScreen() { ... }
 * ```
 * It is possible to declare a destination for a screen with arguments. To do so, a `data class`
 * destination that implements [DestinationWithArgs] should be declared. Args must be `Parcelable`.
 * ```
 * // navigation module, available to other feature modules
 * data class BarDestination(override val args: BarArgs) : DestinationWithArgs<BarArgs>
 * ```
 * ```
 * // feature module
 * @UiExternalEntry(
 *   type = ImplementationType.Composable,
 *   destination = BarDestination::class
 * )
 * @Composable
 * fun FooScreen(args: BarArgs) { ... }
 * ```
 * If the args types in the annotation and the composable params don't match, a compile time error
 * will be thrown.
 *
 * ### Composable function params
 *
 * A composable function annotated with [UiExternalEntry] can have any number of params as long as
 * they have default values.
 * In addition, there are special types allowed as params of the annotated composable:
 * - Args of the type specified in the destination type.
 * - `NavigationController`
 * - [ImplementationType]
 *
 * The example below is a compilable code where Nibel's code generator knows how to automatically
 * provide the required arguments of a composable function.
 * ```
 * @UiExternalEntry(..., destination = BarDestination::class)
 * @Composable
 * fun BarScreen(
 *   args: BarArgs,
 *   navigator: NavigationController,
 *   type: ImplementationType,
 *   viewModel: BarViewModel = viewModel()
 * ) { ... }
 * ```
 * ### Composition locals
 *
 * Alternatively, the params of the types described above could be retrieved as composition locals.
 *
 * ```
 * @UiExternalEntry(..., destination = BarDestination::class)
 * @Composable
 * fun BarScreen(viewModel: BarViewModel = viewModel()) {
 *   val args = LocalArgs.current as BarArgs
 *   val navigator = LocalNavigationController.current
 *   val type = LocalImplementationType.current
 * }
 * ```
 *
 * ### Generated code
 * Depending on the `ImplementationType` in [UiEntry] a different class is generated as a screen
 * entry.
 *
 * #### [ImplementationType.Fragment]
 *
 * ```
 * // generated code
 * class FooScreenEntry : ComposableFragment() {
 *
 *   @Composable
 *   override fun ComposableContent() {
 *      FooScreen()
 *   }
 *
 *   companion object: FragmentEntryFactory<FooDestination> {
 *
 *     override fun newInstance(destination: FooDestination): FragmentEntry {
 *       val fragment = FooScreenEntry()
 *       return FragmentEntry(fragment)
 *     }
 *
 *     fun newInstance(): FragmentEntry {
 *       val fragment = FooScreenEntry()
 *       return FragmentEntry(fragment)
 *     }
 *   }
 * }
 * ```
 * #### [ImplementationType.Composable]
 *
 * ```
 * // generated code
 * @Parcelize
 * class BarScreenEntry(
 *   override val args: BarArgs,
 *   override val name: String,
 * ) : ComposableEntry<BarArgs>(args, name) {
 *
 *   @Composable
 *   override fun ComposableContent() {
 *     BarScreen(args = LocalArgs.current as BarArgs)
 *   }
 *
 *   companion object: ComposableEntryFactory<BarDestination> {
 *
 *     override fun newInstance(destination: BarDestination): ComposableEntry<BarArgs> {
 *       return BarScreenEntry(
 *         args = destination.args,
 *         name = buildRouteName(BarScreenEntry::class.qualifiedName!!, destination.args),
 *       )
 *     }
 *
 *     fun newInstance(args: BarArgs): ComposableEntry<BarArgs> {
 *       return BarScreenEntry(
 *         args = args,
 *         name = buildRouteName(BarScreenEntry::class.qualifiedName!!, args),
 *       )
 *     }
 *   }
 * }
 * ```
 */
@Target(FUNCTION)
@MustBeDocumented
annotation class UiExternalEntry(
    /**
     * Type of a generated screen entry for the annotated composable. Can be either [Fragment] or
     * [Composable].
     *
     * See [UiExternalEntry], [ImplementationType].
     */
    val type: ImplementationType,
    /**
     * A unique destination associated with the screen.
     *
     * See [UiExternalEntry], [ExternalDestination].
     */
    val destination: KClass<out ExternalDestination>
)
