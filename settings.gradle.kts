pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("build-tools")
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // To use local maven repository, use the command below to publish artifacts.
        // ./gradlew publishToMavenLocal --no-configuration-cache
        // Don't forget to disable dependency substitution in sample/build.gradle.kts file.
        mavenLocal()
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "nibel"
include(
    ":nibel-annotations",
    ":nibel-compiler",
    ":nibel-runtime",
    ":nibel-stub",
)
include(
    ":tests"
)
include(
    ":sample",
    ":sample:app",
    ":sample:common",
    ":sample:navigation",
    ":sample:feature-A",
    ":sample:feature-B",
    ":sample:feature-C",
)
