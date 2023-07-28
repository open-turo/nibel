plugins {
    alias(libs.plugins.sample.android.library)
    alias(libs.plugins.sample.android.compose)
}

android {
    namespace = "com.turo.nibel.sample.common"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.material3)
    implementation(libs.nibel.runtime)
}
