@file:Suppress("ObjectPropertyName")

package nibel.runtime

import androidx.compose.runtime.Composable
import nibel.annotations.DestinationWithArgs
import nibel.annotations.ExternalDestination
import nibel.annotations.ImplementationType
import nibel.annotations.LegacyEntry
import nibel.annotations.LegacyExternalEntry
import nibel.annotations.UiEntry
import nibel.annotations.UiExternalEntry

/**
 * Nibel - is a type-safe navigation library for seamless adoption of Jetpack Compose in
 * fragment-based Android apps.
 *
 * By leveraging the power of annotation processing and code generation Nibel provides a unified way
 * of navigating between screens in the following navigation scenarios:
 * - **fragment → compose**
 * - **compose → compose**
 * - **compose → fragment**
 *
 * The core building blocks of Nibel are:
 * - [UiEntry] and [UiExternalEntry] annotations are used to mark composable functions as screen
 * entries.
 * - [NavigationController] performs a navigation between screens.
 * - [LegacyEntry] and [LegacyExternalEntry] annotations are applied to fragments to allow
 * navigation from compose screens to fragments.
 */
object Nibel {
    val serializer = Serializer()

    private val _destinations = mutableMapOf<Class<out ExternalDestination>, EntryFactory<*, *>>()
    private var _argsKey: String? = null
    private var _fragmentSpec: FragmentSpec<*>? = null
    private var _composeSpec: ComposeSpec<*>? = null
    private var _RootDelegate: RootDelegate? = null
    private var _NavigationDelegate: NavigationDelegate<*>? = null

    /**
     * A collection of resolved entries for their corresponding destinations so far.
     */
    val destinations: Map<Class<out ExternalDestination>, EntryFactory<*, *>>
        get() = _destinations

    /**
     * Default key for navigation arguments.
     */
    val argsKey: String
        get() = _argsKey ?: notConfiguredError()

    /**
     * Default fragment navigation specification used by the [NavigationController].
     */
    val fragmentSpec: FragmentSpec<*>
        get() = _fragmentSpec ?: notConfiguredError()

    /**
     * Default compose navigation specification used by the [NavigationController].
     */
    val composeSpec: ComposeSpec<*>
        get() = _composeSpec ?: notConfiguredError()

    /**
     * A root composable function that wraps the content of every [ComposableFragment].
     */
    val RootDelegate: RootDelegate
        get() = _RootDelegate ?: notConfiguredError()

    /**
     * [NavigationDelegate] that provides an under-the-hood implementation for navigation between
     * screens annotated with [ImplementationType.Composable].
     */
    val NavigationDelegate: NavigationDelegate<*>
        get() = _NavigationDelegate ?: notConfiguredError()

    fun configure(
        destinations: Map<Class<out ExternalDestination>, EntryFactory<*, *>> = emptyMap(),
        argsKey: String = NIBEL_ARGS,
        fragmentSpec: FragmentSpec<*> = FragmentTransactionSpec(),
        composeSpec: ComposeSpec<*> = ComposeNavigationSpec(),
        rootDelegate: RootDelegate = EmptyRootDelegate,
        navigationDelegate: NavigationDelegate<*> = ComposeNavigationDelegate(),
    ) {
        _destinations += destinations
        _argsKey = argsKey
        _fragmentSpec = fragmentSpec
        _composeSpec = composeSpec
        _RootDelegate = rootDelegate
        _NavigationDelegate = navigationDelegate
    }

    /**
     * Finds an instance of a screen entry factory associated with a provided external [destination].
     */
    fun <D : ExternalDestination> findEntryFactory(destination: D): EntryFactory<D, *>? {
        if (destination.javaClass !in destinations) {
            registerEntryFactory(destination)
        }
        @Suppress("UNCHECKED_CAST")
        return destinations[destination.javaClass] as? EntryFactory<D, *>
    }

    /**
     * Creates an instance of a [FragmentEntry] from its associated [destination].
     */
    fun <D : ExternalDestination> newFragmentEntry(destination: D): FragmentEntry? {
        val factory = findEntryFactory(destination) as? FragmentEntryFactory
        return factory?.newInstance(destination)
    }

    /**
     * Creates an instance of a [ComposableEntry] from its associated [destination].
     */
    internal fun <D : ExternalDestination> newComposableEntry(destination: D): ComposableEntry<*>? {
        val factory = findEntryFactory(destination) as? ComposableEntryFactory
        return factory?.newInstance(destination)
    }

    /**
     * Renders the composable screen associated with a [destination], setting up
     * the required navigation context. Use this to embed a cross-module composable
     * screen directly without wrapping it in a Fragment.
     *
     * The embedded screen runs in its own isolated navigation scope — back presses
     * and navigation calls inside it do not propagate to the enclosing navigation stack.
     *
     * The [destination] is consumed once at initial composition. Recomposing with a
     * different [destination] instance has no effect on the already-rendered screen.
     */
    @Composable
    fun <D : ExternalDestination> ExternalContent(destination: D) {
        val entry = newComposableEntry(destination)
            ?: error(
                "${destination::class.qualifiedName} is not associated with " +
                    "@UiExternalEntry(ImplementationType.Composable).",
            )
        val rootArgs = (destination as? DestinationWithArgs<*>)?.args
        NavigationDelegate.Content(rootArgs = rootArgs) {
            entry.ComposableContent()
        }
    }

    private fun <D : ExternalDestination> registerEntryFactory(destination: D) {
        val destinationClass = destination.javaClass
        val providerClass = try {
            Class.forName(entryFactoryProviderClassName(destinationClass))
        } catch (_: ClassNotFoundException) {
            error("${destination::class.qualifiedName} is not associated with any @UiExternalEntry.")
        }
        val provideMethod = providerClass.getMethod("provide")
        val entryFactory = provideMethod.invoke(null) as EntryFactory<*, *>
        _destinations[destinationClass] = entryFactory
    }

    private fun <D : ExternalDestination> entryFactoryProviderClassName(
        destinationClass: Class<D>,
    ): String {
        val packageName = destinationClass.`package`?.name.orEmpty()
        val className = destinationClass.canonicalName!!.replace(".", "_")
        return if (packageName.isNotEmpty()) {
            "$packageName._$className"
        } else {
            "_$className"
        }
    }

    private fun notConfiguredError(): Nothing = error(
        "Nibel is not configured. Use Nibel.configure() function before navigation.",
    )
}
