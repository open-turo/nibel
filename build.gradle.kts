// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version libs.versions.androidGradlePlugin apply false
    id("com.android.library") version libs.versions.androidGradlePlugin apply false
    id("org.jetbrains.kotlin.android") version libs.versions.kotlin apply false
    id("com.google.devtools.ksp") version libs.versions.ksp apply false
    id("com.google.dagger.hilt.android") version libs.versions.dagger apply false
    id("org.jetbrains.dokka") version libs.versions.dokka apply false
}
