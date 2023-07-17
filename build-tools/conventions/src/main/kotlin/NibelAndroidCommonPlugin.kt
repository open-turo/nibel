import com.turo.nibel.buildtools.CommonExtension
import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.android
import com.turo.nibel.buildtools.implementation
import com.turo.nibel.buildtools.kotlinOptions
import com.turo.nibel.buildtools.libs
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class NibelAndroidCommonPlugin : NibelConventionPlugin({
    with(pluginManager) {
        apply("org.jetbrains.kotlin.android")
    }

    android<CommonExtension> {
        compileSdk = NibelMetadata.COMPILE_SDK

        defaultConfig {
            minSdk = NibelMetadata.MIN_SDK

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }

        compileOptions {
            sourceCompatibility = NibelMetadata.JAVA_VERSION
            targetCompatibility = NibelMetadata.JAVA_VERSION
        }
        kotlinOptions {
            jvmTarget = NibelMetadata.JAVA_VERSION.toString()
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = NibelMetadata.JAVA_VERSION.toString()
        }
    }

    dependencies {
        implementation(platform(libs.bom.kotlin))
    }
})
