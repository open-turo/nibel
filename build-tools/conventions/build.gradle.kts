import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    // Makes 'libs' version catalog visible and type-safe for precompiled plugins.
    // https://github.com/gradle/gradle/issues/15383#issuecomment-1245546796
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    compileOnly(libs.gradlePlugin.android.api)
    compileOnly(libs.gradlePlugin.kotlin)
}

@Suppress("UNUSED_VARIABLE")
gradlePlugin {
    plugins {
        val application by registering {
            id = "nibel.android.application"
            implementationClass = "NibelAndroidApplicationPlugin"
        }
        val library by registering {
            id = "nibel.android.library"
            implementationClass = "NibelAndroidLibraryPlugin"
        }
        val composeBase by registering {
            id = "nibel.android.compose.base"
            implementationClass = "NibelAndroidComposeBasePlugin"
        }
        val compose by registering {
            id = "nibel.android.compose"
            implementationClass = "NibelAndroidComposePlugin"
        }
        val common by registering {
            id = "nibel.android.common"
            implementationClass = "NibelAndroidCommonPlugin"
        }
        val metadata by registering {
            id = "nibel.metadata"
            implementationClass = "NibelMetadataPlugin"
        }
    }
}
