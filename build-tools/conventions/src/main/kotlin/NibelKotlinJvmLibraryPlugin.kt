import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.kotlin
import com.turo.nibel.buildtools.libs
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class NibelKotlinJvmLibraryPlugin : NibelConventionPlugin({
    with(pluginManager) {
        apply(libs.plugins.kotlin.jvm.get().pluginId)
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = NibelMetadata.JAVA_VERSION.toString()
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        targetCompatibility = NibelMetadata.JAVA_VERSION.toString()
        sourceCompatibility = NibelMetadata.JAVA_VERSION.toString()
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(NibelMetadata.JAVA_VERSION.toString()))
        }
    }
})
