import com.android.build.api.dsl.LibraryExtension
import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.android
import com.turo.nibel.buildtools.libs

class NibelAndroidLibraryPlugin : NibelConventionPlugin({
    with(pluginManager) {
        apply(libs.plugins.android.library.get().pluginId)
        apply(libs.plugins.nibel.android.common.get().pluginId)
    }

    android<LibraryExtension> {
        defaultConfig {
            consumerProguardFiles("consumer-rules.pro")
        }
    }
})
