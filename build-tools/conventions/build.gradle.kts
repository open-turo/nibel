import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    // Makes 'libs' version catalog visible and type-safe for precompiled plugins.
    // https://github.com/gradle/gradle/issues/15383#issuecomment-1245546796
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    compileOnly(libs.gradlePlugin.android.api)
    compileOnly(libs.gradlePlugin.kotlin)
    compileOnly(libs.gradlePlugin.mavenPublish)
}

@Suppress("UNUSED_VARIABLE")
gradlePlugin {
    plugins {
        val metadata by registering {
            id = "nibel.metadata"
            implementationClass = "NibelMetadataPlugin"
        }
        val library by registering {
            id = "nibel.android.library"
            implementationClass = "NibelAndroidLibraryPlugin"
        }
        val compose by registering {
            id = "nibel.android.compose"
            implementationClass = "NibelAndroidComposePlugin"
        }
        val common by registering {
            id = "nibel.android.common"
            implementationClass = "NibelAndroidCommonPlugin"
        }
        val kotlinJvmLibrary by registering {
            id = "nibel.kotlin.jvm.library"
            implementationClass = "NibelKotlinJvmLibraryPlugin"
        }
        val mavenPublish by registering {
            id = "nibel.maven.publish"
            implementationClass = "NibelMavenPublishPlugin"
        }
        val sampleApplication by registering {
            id = "sample.android.application"
            implementationClass = "SampleAndroidApplicationPlugin"
        }
        val sampleLibrary by registering {
            id = "sample.android.library"
            implementationClass = "SampleAndroidLibraryPlugin"
        }
        val sampleCompose by registering {
            id = "sample.android.compose"
            implementationClass = "SampleAndroidComposePlugin"
        }
    }
}
