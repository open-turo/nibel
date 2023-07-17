plugins {
    id("nibel.metadata")
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

group = NibelMetadata.ARTIFACT_GROUP
version = NibelMetadata.ARIFACT_VERSION

dependencies {
    compileOnly(projects.nibelStub)
}
