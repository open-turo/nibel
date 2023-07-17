plugins {
    id("nibel.android.library")
    id("nibel.android.compose.base")
    id("kotlin-parcelize")
    id("org.jetbrains.dokka")
}

group = NibelMetadata.ARTIFACT_GROUP
version = NibelMetadata.ARIFACT_VERSION

android {
    namespace = "com.turo.nibel.runtime"
}

dependencies {
    api(projects.nibelAnnotations)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.fragment)
    implementation(libs.gson)
}
