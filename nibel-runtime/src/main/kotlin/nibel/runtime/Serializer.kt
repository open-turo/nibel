package nibel.runtime

import android.net.Uri
import android.os.Parcelable
import com.google.gson.Gson

class Serializer {

    private val gson by lazy {
        Gson()
    }

    fun <P : Parcelable> serialize(value: P): String =
        Uri.encode(gson.toJson(value))

    fun <P : Parcelable> deserialize(value: String, type: Class<P>): P =
        gson.fromJson(value, type)
}

inline fun <reified P : Parcelable> Serializer.deserialize(value: String): P =
    deserialize(value, P::class.java)
