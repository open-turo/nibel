import com.turo.nibel.buildtools.CommonExtension
import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.android
import com.turo.nibel.buildtools.implementation
import com.turo.nibel.buildtools.libs
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class NibelAndroidCommonPlugin : NibelConventionPlugin({
    android<CommonExtension> {
        compileSdk = NibelMetadata.COMPILE_SDK

        defaultConfig.minSdk = NibelMetadata.MIN_SDK
        defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        compileOptions.sourceCompatibility = NibelMetadata.JAVA_VERSION
        compileOptions.targetCompatibility = NibelMetadata.JAVA_VERSION
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll("-Xstring-concat=inline")
        }
    }

    dependencies {
        implementation(platform(libs.bom.kotlin))
    }
})
