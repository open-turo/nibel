plugins {
    id("nibel.metadata")
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("org.jetbrains.dokka")
}

group = NibelMetadata.ARTIFACT_GROUP
version = NibelMetadata.ARIFACT_VERSION

dependencies {

    implementation(projects.nibelAnnotations)
    implementation(projects.nibelStub)

    implementation(libs.ksp.api)

    ksp(libs.autoService.ksp)
    implementation(libs.autoService.annotations)
}
