import com.turo.nibel.buildtools.NibelConventionPlugin
import org.gradle.api.JavaVersion

class NibelMetadataPlugin : NibelConventionPlugin({})

object NibelMetadata {
    const val MIN_SDK = 24
    const val TARGET_SDK = 35
    const val COMPILE_SDK = 35

    val JAVA_VERSION = JavaVersion.VERSION_11

    const val ARTIFACT_GROUP = "com.openturo.nibel"
}
