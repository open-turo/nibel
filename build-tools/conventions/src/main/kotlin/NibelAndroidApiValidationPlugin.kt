import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.libs
import kotlinx.validation.KotlinApiBuildTask
import kotlinx.validation.KotlinApiCompareTask
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Registers the binary-compatibility-validator tasks (`apiBuild`, `apiCheck`, `apiDump`) for an
 * Android library, checking the public API of its release variant.
 *
 * Workaround: binary-compatibility-validator 0.18.2 only configures Android modules that apply
 * `org.jetbrains.kotlin.android`, which AGP 9's built-in Kotlin doesn't. Remove this plugin once
 * the validator supports AGP 9. Requires the validator to be applied to the root project.
 */
class NibelAndroidApiValidationPlugin : NibelConventionPlugin({
    val apiFileName = "${project.name}.api"
    // The validator adds this to its worker classpath only for modules it configures itself.
    dependencies.add("bcv-rt-jvm-cp", "org.jetbrains.kotlin:kotlin-metadata-jvm:${libs.versions.kotlin.get()}")

    val apiBuild = tasks.register("apiBuild", KotlinApiBuildTask::class.java) {
        // AGP registers variant tasks after plugins are applied, so look it up lazily.
        val compileRelease = tasks.named("compileReleaseKotlin", KotlinCompile::class.java)
        inputClassesDirs.from(compileRelease.flatMap { it.destinationDirectory })
        outputApiFile.set(layout.buildDirectory.file("api/$apiFileName"))
        runtimeClasspath.from(configurations.named("bcv-rt-jvm-cp-resolver"))
    }
    val apiCheck = tasks.register("apiCheck", KotlinApiCompareTask::class.java) {
        group = "verification"
        description = "Checks the public API of ${project.name} against api/$apiFileName"
        projectApiFile.set(layout.projectDirectory.file("api/$apiFileName"))
        generatedApiFile.set(apiBuild.flatMap { it.outputApiFile })
    }
    tasks.register("apiDump", Copy::class.java) {
        group = "other"
        description = "Writes the public API of ${project.name} to api/$apiFileName"
        from(apiBuild.flatMap { it.outputApiFile })
        into(layout.projectDirectory.dir("api"))
    }
    tasks.named("check") { dependsOn(apiCheck) }
})
