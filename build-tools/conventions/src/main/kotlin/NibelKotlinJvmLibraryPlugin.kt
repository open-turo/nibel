import com.turo.nibel.buildtools.NibelConventionPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class NibelKotlinJvmLibraryPlugin : NibelConventionPlugin({
    with(pluginManager) {
        apply("org.jetbrains.kotlin.jvm")
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
})
