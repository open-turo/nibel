import com.android.build.api.dsl.ApplicationExtension
import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.android

class NibelAndroidApplicationPlugin : NibelConventionPlugin({
    with(pluginManager) {
        apply("com.android.application")
        apply("nibel.android.common")
    }

    android<ApplicationExtension> {
        defaultConfig {
            targetSdk = NibelMetadata.TARGET_SDK

            versionCode = 1
            versionName = "1.0"

            vectorDrawables {
                useSupportLibrary = true
            }
        }

        signingConfigs {
            create("release") {
                storeFile = file("release-keystore.jks")
                storePassword = "123456"
                keyAlias = "release"
                keyPassword = "123456"
            }
        }

        buildTypes {
            getByName("release") {
                signingConfig = signingConfigs.getByName("release")
            }
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }
})
