package com.turo.nibel.runtime

import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType

/**
 * A [NavType] which implementation is similar to [NavType.ParcelableType] but it allows using [Parcelable] as
 * argument type in compose navigation library which was not possible by default.
 */
class ParcelableType<P : Parcelable>(
    private val type: Class<P>
) : NavType<P>(isNullableAllowed = false) {

    override val name: String
        get() = type.name

    override fun put(bundle: Bundle, key: String, value: P) {
        type.cast(value)
        bundle.putParcelable(key, value)
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    override fun get(bundle: Bundle, key: String): P? {
        return bundle[key] as P?
    }

    override fun parseValue(value: String): P {
        return Nibel.serializer.deserialize(value, type)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as com.turo.nibel.runtime.ParcelableType<*>
        return type == that.type
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }
}
