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
    ":sample",
    ":sample:app",
    ":sample:common",
    ":sample:navigation",
    ":sample:feature-A",
    ":sample:feature-B",
    ":sample:feature-C",
)
