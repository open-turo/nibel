plugins {
    id("nibel.android.library")
    id("nibel.android.compose")
    id("nibel.maven.publish")
    id("kotlin-parcelize")
    id("org.jetbrains.dokka")
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
