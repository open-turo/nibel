import com.turo.nibel.buildtools.NibelConventionPlugin
import org.gradle.api.JavaVersion

class NibelMetadataPlugin : NibelConventionPlugin({})

object NibelMetadata {
    const val MIN_SDK = 24
    const val TARGET_SDK = 33
    const val COMPILE_SDK = 33

    val JAVA_VERSION = JavaVersion.VERSION_1_8

    const val ARTIFACT_GROUP = "com.turo.nibel"
    const val ARIFACT_VERSION = "0.0.1"
}
