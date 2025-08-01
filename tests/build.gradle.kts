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
    kspTest(projects.nibelCompiler)
    implementation(projects.nibelAnnotations)
    testImplementation(projects.nibelAnnotations)
    implementation(libs.androidx.fragment)
    implementation(projects.nibelRuntime)
    testImplementation(projects.nibelRuntime)
    implementation(libs.junit)
    implementation(libs.kotest.assertions)
}
