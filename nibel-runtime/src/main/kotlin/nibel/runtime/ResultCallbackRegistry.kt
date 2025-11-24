package nibel.runtime

import java.util.concurrent.ConcurrentHashMap

/**
 * Global registry for result callbacks that persists across Fragment/Activity recreation.
 * This ensures callbacks survive navigation and configuration changes.
 *
 * Production-hardened with:
 * - TTL-based cleanup (5-minute expiry) to prevent memory leaks
 * - Max callback limit (50) to prevent DoS attacks
 * - Thread-safe implementation using ConcurrentHashMap
 */
internal object ResultCallbackRegistry {
    private const val MAX_CALLBACKS = 50
    private const val TTL_MILLIS = 5 * 60 * 1000L // 5 minutes

    private val callbacks = ConcurrentHashMap<String, CallbackEntry>()

    /**
     * Stores a callback with timestamp for TTL-based cleanup.
     */
    fun storeCallback(requestKey: String, callback: (Any?) -> Unit) {
        // Cleanup stale callbacks if we're at the limit
        if (callbacks.size >= MAX_CALLBACKS) {
            cleanupStaleCallbacks()

            // If still at limit after cleanup, drop the oldest callback
            if (callbacks.size >= MAX_CALLBACKS) {
                val oldestKey = callbacks.entries
                    .minByOrNull { it.value.timestamp }
                    ?.key
                oldestKey?.let { callbacks.remove(it) }
            }
        }

        callbacks[requestKey] = CallbackEntry(callback, System.currentTimeMillis())
    }

    /**
     * Removes and returns a callback, or null if not found.
     */
    fun removeCallback(requestKey: String): ((Any?) -> Unit)? {
        return callbacks.remove(requestKey)?.callback
    }

    /**
     * Checks if a callback exists for the given request key.
     */
    fun hasCallback(requestKey: String): Boolean {
        return callbacks.containsKey(requestKey)
    }

    /**
     * Clears all callbacks. Should only be used in tests or emergency cleanup.
     */
    fun clear() {
        callbacks.clear()
    }

    /**
     * Removes callbacks older than TTL_MILLIS to prevent memory leaks
     * from abandoned navigation flows.
     */
    private fun cleanupStaleCallbacks() {
        val now = System.currentTimeMillis()
        val staleKeys = callbacks.filterValues { entry ->
            now - entry.timestamp > TTL_MILLIS
        }.keys
        staleKeys.forEach { callbacks.remove(it) }
    }

    /**
     * Internal data class to track callback with creation timestamp.
     */
    private data class CallbackEntry(
        val callback: (Any?) -> Unit,
        val timestamp: Long,
    )
}
