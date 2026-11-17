// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinx.binaryCompatibilityValidator)
}

apiValidation {
    // Only the modules consumers compile against have a public API to keep stable. KSP loads the
    // compiler's processors through a service loader, so its public classes are internal in practice.
    ignoredProjects += listOf(
        "nibel-compiler",
        "tests",
        "sample",
        "app",
        "common",
        "navigation",
        "feature-A",
        "feature-B",
        "feature-C",
    )
}
