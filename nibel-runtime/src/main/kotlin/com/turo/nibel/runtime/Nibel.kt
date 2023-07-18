@file:Suppress("ObjectPropertyName")

package com.turo.nibel.runtime

import com.turo.nibel.annotations.ExternalDestination
import com.turo.nibel.annotations.ImplementationType
import com.turo.nibel.annotations.UiEntry
import com.turo.nibel.annotations.UiExternalEntry
import com.turo.nibel.annotations.LegacyExternalEntry
import com.turo.nibel.annotations.LegacyEntry

/**
 * Nibel - is a type-safe navigation library for seamless adoption of Jetpack Compose in
 * fragment-based Android apps.
 *
 * By leveraging the power of annotation processing Nibel provides a unified way of navigating
 * between screens in the following navigation scenarios:
 * - **fragment → compose**
 * - **compose → compose**
 * - **compose → fragment**
 *
 * Nibel's core building blocks:
 * - [UiEntry] and [UiExternalEntry] annotations are used to mark composable functions as screen
 * entries.
 * - [NavigationController] performs a navigation between compose screens.
 * - [LegacyEntry] and [LegacyExternalEntry] annotation are applied to fragments to allow navigation
 * from compose screens to fragments.
 */
object Nibel {
    internal val serializer = Serializer()

    private val _destinations = mutableMapOf<Class<out ExternalDestination>, EntryFactory<*, *>>()
    private var _argsKey: String? = null
    private var _fragmentSpec: FragmentSpec<*>? = null
    private var _composeSpec: ComposeSpec<*>? = null
    private var _RootDelegate: RootDelegate? = null
    private var _NavigationDelegate: NavigationDelegate<*>? = null

    /**
     * Collection of resolved entries for their respective external destinations. Those are related
     * to screens annotated with [UiExternalEntry] or [LegacyExternalEntry].
     * The initial set of destinations optionally can be pre-populated in [configure]. Other than
     * that, they are being resolved lazily once a navigation to the destination is performed.
     */
    val destinations: Map<Class<out ExternalDestination>, EntryFactory<*, *>>
        get() = _destinations

    /**
     * Key for args used during fragment transactions and compose navigation.
     */
    val argsKey: String
        get() = _argsKey ?: notConfiguredError()

    /**
     * Default fragment navigation specification used by the [NavigationController].
     *
     * See [FragmentSpec]
     */
    val fragmentSpec: FragmentSpec<*>
        get() = _fragmentSpec ?: notConfiguredError()

    /**
     * Default compose navigation specification used by the [NavigationController].
     *
     * See [ComposeSpec]
     */
    val composeSpec: ComposeSpec<*>
        get() = _composeSpec ?: notConfiguredError()

    /**
     * [RootDelegate] that allows injecting custom composable code at the root of a generated
     * entry time a screen annotated with [ImplementationType.Fragment] is opened.
     *
     * One of the primary scenarios of its usage is applying a custom compose app theme.
     */
    val RootDelegate: RootDelegate
        get() = _RootDelegate ?: notConfiguredError()

    /**
     * [NavigationDelegate] that provides an under-the-hood implementation for navigation between
     * screens annotated with [ImplementationType.Composable].
     */
    val NavigationDelegate: NavigationDelegate<*>
        get() = _NavigationDelegate ?: notConfiguredError()

    /**
     * Nibel initializer that should be called before performing navigation to screen entries.
     */
    fun configure(
        destinations: Map<Class<out ExternalDestination>, EntryFactory<*, *>> = emptyMap(),
        argsKey: String = NIBEL_ARGS,
        fragmentSpec: FragmentSpec<*> = FragmentTransactionSpec(),
        composeSpec: ComposeSpec<*> = ComposeNavigationSpec(),
        rootDelegate: RootDelegate = EmptyRootDelegate,
        navigationDelegate: NavigationDelegate<*> = ComposeNavigationDelegate(),
    ) {
        this._destinations += destinations
        this._argsKey = argsKey
        this._fragmentSpec = fragmentSpec
        this._composeSpec = composeSpec
        this._RootDelegate = rootDelegate
        this._NavigationDelegate = navigationDelegate
    }

    /**
     * Finds a corresponding factory of an screen entry associated with a provided
     * external [destination].
     */
    fun <D : ExternalDestination> findEntryFactory(destination: D): EntryFactory<D, *>? {
        if (destination.javaClass !in destinations) {
            registerEntryFactory(destination)
        }
        @Suppress("UNCHECKED_CAST")
        return destinations[destination.javaClass] as? EntryFactory<D, *>
    }

    /**
     * Creates an instance of a [FragmentEntry] used for navigation. Primarily targets cases when
     * a multi-module navigation from existing fragment to a compose screen is required.
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

    private fun notConfiguredError(): Nothing = error("Nibel must be configured!")
}
