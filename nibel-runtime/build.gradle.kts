plugins {
    alias(libs.plugins.nibel.android.library)
    alias(libs.plugins.nibel.android.compose)
    alias(libs.plugins.nibel.maven.publish)
    id("kotlin-parcelize")
    alias(libs.plugins.dokka)
}

android {
    namespace = "nibel.runtime"
}

dependencies {
    api(projects.nibelAnnotations)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.fragment)
    implementation(libs.gson)
}
