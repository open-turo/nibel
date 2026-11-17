plugins {
    alias(libs.plugins.sample.android.application)
    alias(libs.plugins.sample.android.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
}

android {
    namespace = "com.turo.nibel.sample"

    defaultConfig {
        applicationId = "com.turo.nibel.sample"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(projects.sample.common)
    implementation(projects.sample.navigation)
    implementation(projects.sample.featureA)
    implementation(projects.sample.featureB)
    implementation(projects.sample.featureC)

    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.compose.activity)
    implementation(libs.nibel.runtime)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    androidTestImplementation(platform(libs.bom.androidx.compose))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    kspAndroidTest(libs.nibel.compiler)
}
