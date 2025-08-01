package nibel.runtime

/**
 * [ResultCallback] is a functional interface for handling results from result-based navigation.
 *
 * @param R The type of result being handled
 */
fun interface ResultCallback<R : Any> {
    /**
     * Called when a result is available from a result-based navigation.
     *
     * @param result The result returned from the screen, or null if cancelled/failed
     */
    fun onResult(result: R?)
}

/**
 * Registry for managing result callbacks during navigation lifecycle.
 * This ensures callbacks are properly handled even across configuration changes.
 */
class ResultCallbackRegistry {
    private val callbacks = mutableMapOf<String, ResultCallback<*>>()

    /**
     * Registers a result callback for a specific key.
     */
    fun <R : Any> register(key: String, callback: ResultCallback<R>) {
        callbacks[key] = callback
    }

    /**
     * Retrieves and removes a result callback for a specific key.
     */
    @Suppress("UNCHECKED_CAST")
    fun <R : Any> consume(key: String): ResultCallback<R>? {
        return callbacks.remove(key) as? ResultCallback<R>
    }

    /**
     * Clears all registered callbacks.
     */
    fun clear() {
        callbacks.clear()
    }
}
