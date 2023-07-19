plugins {
    id("nibel.metadata")
    id("nibel.maven.publish")
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(projects.nibelAnnotations)
    implementation(projects.nibelStub)

    implementation(libs.ksp.api)

    ksp(libs.autoService.ksp)
    implementation(libs.autoService.annotations)
}
