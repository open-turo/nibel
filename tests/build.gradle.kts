plugins {
    id("nibel.android.library")
    id("nibel.android.compose")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.openturo.nibel.tests"
}

dependencies {
    ksp(projects.nibelCompiler)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.compose.navigation)
    implementation(projects.nibelRuntime)
    implementation(libs.junit)
    implementation(libs.kotest.assertions)

    testImplementation(libs.kctfork.ksp)
    testImplementation(projects.nibelCompiler) {
        // The stub's fake Parcelable would shadow Android's on the test classpath.
        exclude(module = "nibel-stub")
    }
}
