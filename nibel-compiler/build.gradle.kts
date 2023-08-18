plugins {
    alias(libs.plugins.nibel.kotlin.jvm.library)
    alias(libs.plugins.nibel.maven.publish)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dokka)
}

dependencies {
    implementation(projects.nibelAnnotations)
    implementation(projects.nibelStub)

    implementation(libs.ksp.api)

    ksp(libs.autoService.ksp)
    implementation(libs.autoService.annotations)
}
