import com.turo.nibel.buildtools.CommonExtension
import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.android

class SampleAndroidLibraryPlugin : NibelConventionPlugin({
    with(pluginManager) {
        apply("com.android.library")
        apply("nibel.android.common")
    }

    android<CommonExtension> {
        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
    }
})
