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
    ksp(libs.nibel.compiler)
    implementation(libs.androidx.fragment)
    implementation(libs.nibel.runtime)
    implementation(libs.junit)
    implementation(libs.kotest.assertions)
}
