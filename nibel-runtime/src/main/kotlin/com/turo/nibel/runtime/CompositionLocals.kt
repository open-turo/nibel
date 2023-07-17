package com.turo.nibel.runtime

import android.os.Parcelable
import androidx.compose.runtime.compositionLocalOf
import com.turo.nibel.annotations.ImplementationType

/**
 * Provides instance of [NavigationController] to perform navigation.
 */
val LocalNavigationController = compositionLocalOf<NavigationController> {
    error("LocalNavigationController not present")
}

/**
 * Provides information about the [ImplementationType] of a current compose screen. In case the
 * composable function called directly, without Nibel navigation, a `null` is returned.
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
