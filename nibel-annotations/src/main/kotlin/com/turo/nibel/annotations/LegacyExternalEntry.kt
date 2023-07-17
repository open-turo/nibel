package com.turo.nibel.annotations

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.reflect.KClass

/**
 * [LegacyExternalEntry] includes all the functionality of [LegacyEntry] but additionally enables a
 * multi-module navigation. Its behavior is similar to [UiExternalEntry] with
 * [ImplementationType.Fragment].
 *
 * Unlike [LegacyEntry] using [LegacyExternalEntry] is mandatory if a multi-module navigation is
 * required.
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
 * @LegacyExternalEntry(destination = FooDestination::class)
 * class FooFragment : Fragment() { ... }
 * ```
 * It is possible to declare a destination for a screen with arguments. To do so, a `data class`
 * destination that implements [DestinationWithArgs] should be declared. Args must be `Parcelable`.
 * ```
 * // navigation module, available to other feature modules
 * data class BarDestination(override val args: BarArgs) : DestinationWithArgs<BarArgs>
 * ```
 * ```
 * // feature module
 * @LegacyExternalEntry(destination = BarDestination::class)
 * class BarFragment : Fragment() {
 *   arguments?.getNibelArgs<BarArgs>()
 * }
 * ```
 *
 * ### Generated code
 * For a fragment annotated with [LegacyExternalEntry] the following code generated.
 * ```
 * // generated code
 * object BarFragmentEntry: FragmentEntryFactory<BarDestination> {
 *
 *   override fun newInstance(destination: BarDestination): FragmentEntry {
 *     val fragment = BarFragment()
 *     fragment.arguments = destination.args.asNibelArgs()
 *     return FragmentEntry(fragment)
 *   }
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
annotation class LegacyExternalEntry(
    /**
     * A unique destination associated with the screen.
     *
     * See [UiExternalEntry], [ExternalDestination].
     */
    val destination: KClass<out ExternalDestination>,
)
