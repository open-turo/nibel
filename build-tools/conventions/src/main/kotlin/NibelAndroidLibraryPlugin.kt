import com.android.build.api.dsl.LibraryExtension
import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.android

class NibelAndroidLibraryPlugin : NibelConventionPlugin({
    with(pluginManager) {
        apply("com.android.library")
        apply("nibel.android.common")
    }

    android<LibraryExtension> {
        defaultConfig {
            consumerProguardFiles("consumer-rules.pro")
        }
    }
})
