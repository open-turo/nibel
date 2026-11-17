package nibel.runtime

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.findFragment
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.UUID

/**
 * A screen entry typed with the result its screen returns, for
 * [NavigationController.navigateForResult].
 *
 * Screens that declare a `result` in their annotation get a `newInstanceForResult` factory that
 * returns one. To open a plain fragment for a result, wrap it explicitly:
 * ```
 * navigator.navigateForResult(ResultEntry<PickedDate>(FragmentEntry(DatePickerFragment())), launcher)
 * ```
 */
class ResultEntry<R : Parcelable>(val entry: Entry)

/**
 * Receives the result of type [R] of screens opened with [NavigationController.navigateForResult].
 * Create one with [rememberResultLauncher] in compose or [registerForNibelResult] in a fragment.
 *
 * Results survive configuration changes and process death, and are delivered once the caller is
 * at least started again. `null` means the screen was closed without a result: it was cancelled
 * with [NavigationController.cancelResultAndNavigateBack], left with system or predictive back, or
 * otherwise removed. A launcher has at most one request in flight; opening another screen with it
 * replaces the previous request.
 */
class ResultLauncher<R : Parcelable> internal constructor(
    internal val resultType: Class<R>,
    internal val request: ResultRequest,
) {
    internal val target: ResultTarget
        get() = ResultTarget(request.key, resultType.name)
}

/**
 * Remembers a [ResultLauncher] whose [onResult] is invoked with the result of the screens it opens,
 * or `null` if a screen is closed without a result.
 */
@Composable
inline fun <reified R : Parcelable> rememberResultLauncher(noinline onResult: (R?) -> Unit): ResultLauncher<R> =
    rememberResultLauncher(R::class.java, onResult)

@PublishedApi
@Composable
internal fun <R : Parcelable> rememberResultLauncher(
    resultType: Class<R>,
    onResult: (R?) -> Unit,
): ResultLauncher<R> {
    val request = rememberSaveable(saver = ResultRequestSaver) { ResultRequest(UUID.randomUUID().toString()) }
    val fragmentManager = LocalContext.current.findFragmentActivity().supportFragmentManager
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val currentOnResult by rememberUpdatedState(onResult)

    DisposableEffect(fragmentManager, request, lifecycleOwner) {
        val hostFragment = { runCatching { view.findFragment<Fragment>() }.getOrNull() }
        val stopObserving = fragmentManager.observeResults(request, resultType, lifecycleOwner, hostFragment) {
            currentOnResult(it)
        }
        onDispose { stopObserving() }
    }
    return remember(request) { ResultLauncher(resultType, request) }
}

private val ResultRequestSaver = Saver<ResultRequest, Bundle>(
    save = { it.toBundle() },
    restore = { ResultRequest.fromBundle(it) },
)

/**
 * Registers a [ResultLauncher] for a fragment caller. Call it in `onCreate` on every creation of the
 * fragment, so that results sent while it was destroyed are delivered after it is recreated.
 *
 * [name] distinguishes several launchers of the same fragment and must be unique within it.
 *
 * @throws IllegalStateException if a launcher with the same [name] is already registered.
 */
inline fun <reified R : Parcelable> Fragment.registerForNibelResult(
    name: String = "default",
    noinline onResult: (R?) -> Unit,
): ResultLauncher<R> = registerForNibelResult(R::class.java, name, onResult)

@PublishedApi
internal fun <R : Parcelable> Fragment.registerForNibelResult(
    resultType: Class<R>,
    name: String,
    onResult: (R?) -> Unit,
): ResultLauncher<R> {
    // Saved with this fragment instance, so two instances of one fragment class get their own keys.
    val registryKey = "nibel_result_launcher:$name"
    check(savedStateRegistry.getSavedStateProvider(registryKey) == null) {
        "A result launcher named '$name' is already registered in ${javaClass.name}. " +
            "Give each launcher of a fragment its own name."
    }
    val request = savedStateRegistry.consumeRestoredStateForKey(registryKey)
        ?.let(ResultRequest::fromBundle)
        ?: ResultRequest(UUID.randomUUID().toString())
    savedStateRegistry.registerSavedStateProvider(registryKey) { request.toBundle() }
    requireActivity().supportFragmentManager.observeResults(request, resultType, this, { this }, onResult)
    return ResultLauncher(resultType, request)
}

/**
 * Prepares the fragment of [entry] to return its result to this launcher, for callers that open it
 * with their own fragment transaction instead of a [NavigationController]. Commit the returned
 * fragment right away.
 */
fun <R : Parcelable> ResultLauncher<R>.prepareFragment(entry: ResultEntry<R>): Fragment {
    val fragmentEntry = entry.entry as? FragmentEntry
        ?: throw IllegalArgumentException("Only fragment entries can be committed with a fragment transaction.")
    attachTo(fragmentEntry)
    request.launch(callerCovered = false)
    return fragmentEntry.fragment
}

internal fun ResultLauncher<*>.attachTo(entry: FragmentEntry) {
    val arguments = entry.fragment.arguments ?: Bundle()
    target.writeTo(arguments)
    entry.fragment.arguments = arguments
}

/**
 * Sends [result] to the caller of this fragment if it was opened for a result, or a cancellation if
 * [result] is `null`. Doesn't navigate back. Does nothing if the fragment wasn't opened for a result.
 *
 * @throws IllegalArgumentException if [result] isn't of the type the caller expects.
 */
fun Fragment.setNibelResult(result: Parcelable?) {
    val target = ResultTarget.from(arguments) ?: return
    requireActivity().supportFragmentManager.sendResult(target, result)
}

private fun Context.findFragmentActivity(): FragmentActivity {
    var context = this
    while (context !is Activity) {
        context = (context as? ContextWrapper)?.baseContext
            ?: error("Nibel results require a FragmentActivity context.")
    }
    return context as? FragmentActivity ?: error("Nibel results require a FragmentActivity context.")
}
