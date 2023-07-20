plugins {
    id("nibel.kotlin.jvm.library")
    id("nibel.maven.publish")
    id("org.jetbrains.dokka")
}

dependencies {
    compileOnly(projects.nibelStub)
}
