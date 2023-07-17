plugins {
    id("nibel.android.library")
    id("nibel.android.compose")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.turo.nibel.sample.featureA"
}

dependencies {
    implementation(projects.sample.navigation)
    implementation(projects.sample.common)

    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.lifecycle.runtime)
    implementation(libs.androidx.compose.navigation.hilt)
    implementation(libs.nibel.runtime)
    implementation(libs.hilt.android)

    kapt(libs.hilt.android.compiler)

    ksp(libs.nibel.compiler)
}
