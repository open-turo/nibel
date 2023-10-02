package nibel.tests.codegen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestArgs(val value: String) : Parcelable
