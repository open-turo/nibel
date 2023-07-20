package nibel.annotations

/**
 * [ImplementationType] is used in [UiEntry] and [UiExternalEntry] annotations to define what type
 * of entry class should be generated for the annotated composable function.
 *
 * Depending on the implementation type a generated entry will inherit completely different base
 * classes:
 * - [Fragment] - generates a fragment that uses the annotated composable as its content. It makes
 * the compose screen to appear as a fragment outside of compose and is crucial in
 * **fragment → compose** navigation scenarios.
 * - [Composable] - generates a simple wrapper around a composable function and reduces the
 * performance overhead by avoiding instantiation of fragment-related classes for this screen. It is
 * normally used in **compose → compose** and **compose → fragment** navigation scenarios.
 *
 * See [UiEntry] and [UiExternalEntry] to learn more about the generated code depending on
 * implementation types.
 */
enum class ImplementationType {
    /**
     * A fragment that uses the annotated composable as its content will be generated for the screen
     *
     * See [ImplementationType].
     */
    Fragment,

    /**
     * A simple wrapper around a composable function will be generated for the screen.
     *
     * See [ImplementationType].
     */
    Composable,
}
