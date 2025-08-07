import com.turo.nibel.buildtools.CommonExtension
import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.android
import com.turo.nibel.buildtools.implementation
import com.turo.nibel.buildtools.libs
import org.gradle.kotlin.dsl.dependencies

class NibelAndroidComposePlugin : NibelConventionPlugin({
    with(pluginManager) {
        apply(libs.plugins.compose.compiler.get().pluginId)
    }
    android<CommonExtension> {
        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
        }
    }

    dependencies {
        implementation(platform(libs.bom.androidx.compose))
        implementation(libs.androidx.compose.runtime)
    }
})
