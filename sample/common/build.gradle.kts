plugins {
    id("sample.android.library")
    id("sample.android.compose")
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
