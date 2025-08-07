import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.libs
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class NibelDetektPlugin : NibelConventionPlugin({
    with(pluginManager) {
        apply(libs.plugins.detekt.get().pluginId)
    }

    configure<DetektExtension> {
        // Enable parallel processing
        parallel = true

        // Use type resolution for better analysis
        buildUponDefaultConfig = true

        // Configuration file
        config.setFrom("$rootDir/config/detekt/detekt.yml")

        // Baseline file for existing issues (commented out for now)
        // baseline = file("$rootDir/detekt-baseline.xml")

        // Enable auto correction where possible
        autoCorrect = false

        // Exclude generated code and build directories
        ignoredBuildTypes = listOf("release")
        ignoredFlavors = emptyList()

        // Source sets to analyze
        source.setFrom(
            "src/main/java",
            "src/main/kotlin",
            "src/test/java",
            "src/test/kotlin"
        )
    }

    dependencies {
        "detektPlugins"(libs.detekt.formatting)
    }

    // Configure Detekt tasks
    tasks.withType<Detekt>().configureEach {
        // Enable build cache
        setSource(files("src/main/kotlin", "src/test/kotlin"))
        exclude("**/generated/**", "**/build/**")

        // Set JVM target
        jvmTarget = NibelMetadata.JAVA_VERSION.toString()

        // Output reports
        reports {
            xml.required.set(true)
            html.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }

    tasks.withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = NibelMetadata.JAVA_VERSION.toString()
    }
})
