package com.turo.nibel.runtime

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.turo.nibel.annotations.ImplementationType

/**
 *
 */
abstract class ComposableEntry<A : Parcelable>(
    open val args: A?,
    open val name: String,
) : Entry, Parcelable {

    @Composable
    abstract fun ComposableContent()

    @SuppressLint("NotConstructor")
    @Suppress("MemberNameEqualsClassName")
    @Composable
    fun ComposableEntry() {
        CompositionLocalProvider(
            LocalImplementationType provides ImplementationType.Composable
        ) {
            ComposableContent()
        }
    }
}

fun buildRouteName(base: String, args: Parcelable? = null): String = base
