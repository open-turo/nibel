package nibel.annotations

import android.os.Parcelable
import nibel.runtime.NoArgs
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.reflect.KClass

/**
 * [LegacyEntry] marks an existing fragment class and enables compose → fragment scenarios.
 * Its usage is completely optional as its main purpose is generating a simple factory for the
 * fragment.
 *
 * Its behavior is quite similar to [UiEntry] with [ImplementationType.Fragment].
 *
 * ### Basic usage
 * To define a legacy fragment as an entry, it must be annotated with [LegacyEntry].
 * ```
 * @LegacyEntry()
 * class FooFragment : Fragment() { ... }
 * ```
 * It is possible to declare a screen with arguments by declaring the args of `Parcelable` type in
 * [LegacyEntry] annotation.
 * ```
 * @LegacyEntry(args = BarArgs::class)
 * class BarFragment : Fragment() {
 *   ...
 *   arguments?.getNibelArgs<BarArgs>()
 * }
 * ```
 * ### Generated code
 * For a fragment annotated with [LegacyEntry] the following code generated.
 * ```
 * // generated code
 * object BarFragmentEntry {
 *
 *   fun newInstance(args: BarArgs): FragmentEntry {
 *     val fragment = BarFragment()
 *     fragment.arguments = args.asNibelArgs()
 *     return FragmentEntry(fragment)
 *   }
 * }
 * ```
 */
@Target(CLASS)
@MustBeDocumented
annotation class LegacyEntry(
    /**
     * Optional `Parcelable` args for the screen.
     * If not specified, the screen is considered to have no arguments.
     *
     * See [LegacyEntry].
     */
    val args: KClass<out Parcelable> = NoArgs::class,
)
