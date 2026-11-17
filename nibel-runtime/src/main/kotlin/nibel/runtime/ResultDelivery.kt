package nibel.runtime

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle

private const val NIBEL_REQUEST_KEY = "nibel_request_key"
private const val NIBEL_RESULT_TYPE = "nibel_result_type"
private const val NIBEL_RESULT = "nibel_result"
private const val REQUEST_PENDING = "pending"
private const val REQUEST_PAUSED_SINCE_LAUNCH = "paused_since_launch"

/**
 * Where a screen opened for a result sends it: the caller's request key and the result type the
 * caller expects. Stored on the opened screen, in its fragment arguments or back stack entry.
 */
internal data class ResultTarget(
    val requestKey: String,
    val resultTypeName: String,
) {
    fun writeTo(bundle: Bundle) {
        bundle.putString(NIBEL_REQUEST_KEY, requestKey)
        bundle.putString(NIBEL_RESULT_TYPE, resultTypeName)
    }

    fun writeTo(handle: SavedStateHandle) {
        handle[NIBEL_REQUEST_KEY] = requestKey
        handle[NIBEL_RESULT_TYPE] = resultTypeName
    }

    companion object {
        fun from(bundle: Bundle?): ResultTarget? {
            val requestKey = bundle?.getString(NIBEL_REQUEST_KEY) ?: return null
            val resultTypeName = bundle.getString(NIBEL_RESULT_TYPE) ?: return null
            return ResultTarget(requestKey, resultTypeName)
        }

        fun from(handle: SavedStateHandle?): ResultTarget? {
            val requestKey = handle?.get<String>(NIBEL_REQUEST_KEY) ?: return null
            val resultTypeName = handle.get<String>(NIBEL_RESULT_TYPE) ?: return null
            return ResultTarget(requestKey, resultTypeName)
        }
    }
}

/**
 * The request of a [ResultLauncher], saved with its caller.
 *
 * A request is pending from the moment a screen is opened with the launcher until a result or a
 * cancellation is delivered. A caller that resumes after being paused while its request is still
 * pending was left by its opened screen without a result, which counts as a cancellation.
 */
internal class ResultRequest(
    val key: String,
    var pending: Boolean = false,
    var pausedSinceLaunch: Boolean = false,
) {
    /**
     * Starts a request. [callerCovered] is `true` when the opened screen is already shown over the
     * caller, so the caller's pause may have happened before the request started.
     */
    fun launch(callerCovered: Boolean) {
        pending = true
        pausedSinceLaunch = callerCovered
    }

    fun toBundle() = Bundle().apply {
        putString(NIBEL_REQUEST_KEY, key)
        putBoolean(REQUEST_PENDING, pending)
        putBoolean(REQUEST_PAUSED_SINCE_LAUNCH, pausedSinceLaunch)
    }

    companion object {
        fun fromBundle(bundle: Bundle) = ResultRequest(
            key = bundle.getString(NIBEL_REQUEST_KEY)!!,
            pending = bundle.getBoolean(REQUEST_PENDING),
            pausedSinceLaunch = bundle.getBoolean(REQUEST_PAUSED_SINCE_LAUNCH),
        )
    }
}

/**
 * Sends [result] to [target], or a cancellation if [result] is `null`.
 */
internal fun FragmentManager.sendResult(target: ResultTarget, result: Parcelable?) {
    if (result != null) {
        val matchesType = runCatching {
            Class.forName(target.resultTypeName, false, result.javaClass.classLoader).isInstance(result)
        }.getOrDefault(false)
        require(matchesType) {
            "Result of type ${result.javaClass.name} doesn't match the expected result type " +
                "${target.resultTypeName}."
        }
    }
    setFragmentResult(target.requestKey, Bundle().apply { putParcelable(NIBEL_RESULT, result) })
}

/**
 * Delivers results and cancellations of [request] to [onResult] while [lifecycleOwner] is at least
 * started. Returns a function that stops observing.
 */
internal fun <R : Parcelable> FragmentManager.observeResults(
    request: ResultRequest,
    resultType: Class<R>,
    lifecycleOwner: LifecycleOwner,
    onResult: (R?) -> Unit,
): () -> Unit {
    // Registered first: the FragmentManager delivers a stored result on ON_START, so a result
    // always clears the request before the ON_RESUME check below can treat it as cancelled.
    setFragmentResultListener(request.key, lifecycleOwner) { _, bundle ->
        if (!request.pending) return@setFragmentResultListener
        request.pending = false
        // Restored bundles have no class loader after process death.
        bundle.classLoader = resultType.classLoader
        @Suppress("DEPRECATION")
        onResult(bundle.getParcelable<Parcelable>(NIBEL_RESULT)?.let(resultType::cast))
    }
    val cancellationObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_PAUSE -> if (request.pending) request.pausedSinceLaunch = true
            Lifecycle.Event.ON_RESUME -> if (request.pending && request.pausedSinceLaunch) {
                request.pending = false
                onResult(null)
            }

            else -> Unit
        }
    }
    lifecycleOwner.lifecycle.addObserver(cancellationObserver)
    return {
        clearFragmentResultListener(request.key)
        lifecycleOwner.lifecycle.removeObserver(cancellationObserver)
    }
}
