@file:Suppress("ObjectPropertyName")

package nibel.runtime

import nibel.annotations.ExternalDestination
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.annotations.UiExternalEntry
import nibel.annotations.LegacyExternalEntry
import nibel.annotations.LegacyEntry

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

    private fun <D : ExternalDestination> registerEntryFactory(destination: D) {
        val destinationClass = destination.javaClass
        val providerClass = Class.forName(
            entryFactoryProviderClassName(destinationClass)
        )
        val provideMethod = providerClass.getMethod("provide")
        val entryFactory = provideMethod.invoke(null) as EntryFactory<*, *>
        _destinations[destinationClass] = entryFactory
    }

    private fun <D : ExternalDestination> entryFactoryProviderClassName(
        destinationClass: Class<D>
    ): String {
        val packageName = destinationClass.`package`?.name.orEmpty()
        val className = destinationClass.canonicalName!!.replace(".", "_")
        return if (packageName.isNotEmpty()) {
            "$packageName._${className}"
        } else {
            "_${className}"
        }
    }

    private fun notConfiguredError(): Nothing = error(
        "Nibel is not configured. Use Nibel.configure() function before navigation."
    )
}
