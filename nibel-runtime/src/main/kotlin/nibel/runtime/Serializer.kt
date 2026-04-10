package nibel.runtime

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64

class Serializer {

    fun <P : Parcelable> serialize(value: P): String {
        val parcel = Parcel.obtain()
        try {
            parcel.writeParcelable(value, 0)
            val bytes = parcel.marshall()
            val base64 = Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
            return Uri.encode(base64)
        } finally {
            parcel.recycle()
        }
    }

    @Suppress("DEPRECATION")
    fun <P : Parcelable> deserialize(value: String, type: Class<P>): P {
        val bytes = Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP)
        val parcel = Parcel.obtain()
        try {
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            val result = parcel.readParcelable<P>(type.classLoader)
            return result ?: throw IllegalArgumentException(
                "Failed to deserialize Parcelable of type ${type.name}",
            )
        } finally {
            parcel.recycle()
        }
    }
}

inline fun <reified P : Parcelable> Serializer.deserialize(value: String): P =
    deserialize(value, P::class.java)
