import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.debugImplementation
import com.turo.nibel.buildtools.implementation
import com.turo.nibel.buildtools.libs
import org.gradle.kotlin.dsl.dependencies

class NibelAndroidComposePlugin : NibelConventionPlugin({
    with(pluginManager) {
        apply("nibel.android.compose.base")
    }

    dependencies {
        implementation(libs.androidx.compose.foundation)
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.lifecycle.viewModel)
        implementation(libs.androidx.compose.ui.tooling.preview)
        debugImplementation(libs.androidx.compose.ui.tooling)
    }
})
