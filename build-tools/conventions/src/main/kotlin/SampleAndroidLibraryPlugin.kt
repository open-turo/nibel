import com.turo.nibel.buildtools.CommonExtension
import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.android
import com.turo.nibel.buildtools.libs

class SampleAndroidLibraryPlugin : NibelConventionPlugin({
    with(pluginManager) {
        apply(libs.plugins.android.library.get().pluginId)
        apply(libs.plugins.nibel.android.common.get().pluginId)
        apply(libs.plugins.nibel.detekt.get().pluginId)
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
