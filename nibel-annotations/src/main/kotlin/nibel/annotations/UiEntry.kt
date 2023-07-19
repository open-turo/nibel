package nibel.annotations

import android.os.Parcelable
import nibel.annotations.ImplementationType.Composable
import nibel.annotations.ImplementationType.Fragment
import nibel.runtime.NoArgs
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

/**
 * [UiEntry] marks a composable function as a screen that is used by Nibel for navigation.
 * For each annotated composable Nibel's annotation processor generates a `{ComposableName}Entry`
 * class to provide a unified and type-safe way of navigating between screens in the following
 * navigation scenarios:
 * - fragment → compose
 * - compose → compose
 * - compose → fragment
 *
 * The type of the generated entry differs depending on the [ImplementationType] specified in the
 * annotation. Each type serves a specific scenario and can be either [Fragment] or [Composable].
 *
 * ### Basic usage
 * To define a screen entry, a composable function must be annotated with [UiEntry].
 * ```
 * @UiEntry(type = ImplementationType.Fragment)
 * @Composable
 * fun FooScreen() { ... }
 * ```
 * It is possible to declare a screen with arguments by declaring the args of `Parcelable` type in
 * [UiEntry] annotation.
 *
 * ```
 * @UiEntry(
 *   type = ImplementationType.Composable,
 *   args = BarArgs::class
 * )
 * @Composable
 * fun BarScreen(args: BarArgs) { ... }
 * ```
 * If the type of args both in the annotation and the composable params don't match, a compile
 * time error will be thrown.
 *
 * ### Composable function params
 *
 * A composable function annotated with [UiEntry] can have any number of params as long as they
 * have default values.
 * In addition, there are special types allowed as params of the annotated composable:
 * - Args of the type specified in the [UiEntry] annotation.
 * - `NavigationController`
 * - [ImplementationType]
 *
 * The example below is a compilable code where Nibel's code generator knows how to automatically
 * provide the required arguments of a composable function.
 * ```
 * @UiEntry(..., args = BarArgs::class)
 * @Composable
 * fun BarScreen(
 *   args: BarArgs,
 *   navigator: NavigationController,
 *   type: ImplementationType,
 *   viewModel: FooViewModel = viewModel()
 * ) { ... }
 * ```
 * ### Composition locals
 *
 * Alternatively, the params of the types described above could be retrieved as composition locals.
 *
 * ```
 * @UiEntry(..., args = BarArgs::class)
 * @Composable
 * fun BarScreen(viewModel: BarViewModel = viewModel()) {
 *   val args = LocalArgs.current as BarArgs
 *   val navigator = LocalNavigationController.current
 *   val type = LocalImplementationType.current
 * }
 * ```
 *
 * ### Generated code
 * Depending on the implementation type used in [UiEntry] a different class is generated as a screen
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
 *   companion object {
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
 *   companion object {
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
annotation class UiEntry(
    /**
     * Type of a generated screen entry for the annotated composable. Can be either [Fragment] or [Composable].
     *
     * See [UiEntry], [ImplementationType].
     */
    val type: ImplementationType,
    /**
     * Optional `Parcelable` args for the screen.
     * If not specified, the screen is considered to have no arguments.
     *
     * See [UiEntry].
     */
    val args: KClass<out Parcelable> = NoArgs::class
)
