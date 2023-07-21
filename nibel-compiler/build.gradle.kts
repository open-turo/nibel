plugins {
    alias(libs.plugins.nibel.metadata)
    alias(libs.plugins.nibel.maven.publish)
    kotlin("jvm")
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
