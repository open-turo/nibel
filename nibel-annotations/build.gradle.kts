plugins {
    alias(libs.plugins.nibel.kotlin.jvm.library)
    alias(libs.plugins.nibel.maven.publish)
    alias(libs.plugins.dokka)
}

dependencies {
    compileOnly(projects.nibelStub)
}
