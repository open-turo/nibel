plugins {
    alias(libs.plugins.sample.android.library)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.turo.nibel.sample.navigation"
}

dependencies {
    implementation(libs.nibel.runtime)
}
