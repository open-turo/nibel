package nibel.runtime

import android.os.Parcelable
import androidx.compose.runtime.compositionLocalOf
import nibel.annotations.ImplementationType

/**
 * Provides instance of [NavigationController] to perform navigation.
 */
val LocalNavigationController = compositionLocalOf<NavigationController> {
    error("LocalNavigationController not present")
}

/**
 * Provides information about the [ImplementationType] of a current compose screen. In case
 * composable function is called directly, `null` is returned.
 */
val LocalImplementationType = compositionLocalOf<ImplementationType?> {
    null
}

/**
 * Provides args of the current compose screen or `null` if it has no args.
 */
val LocalArgs = compositionLocalOf<Parcelable?> {
    null
}

/**
 * Provides request key for result-based navigation or `null` if not navigated via navigateForResult.
 */
val LocalRequestKey = compositionLocalOf<String?> {
    null
}
