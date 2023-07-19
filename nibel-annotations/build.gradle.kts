plugins {
    id("nibel.metadata")
    id("nibel.maven.publish")
    kotlin("jvm")
    id("org.jetbrains.dokka")
}


dependencies {
    compileOnly(projects.nibelStub)
}
