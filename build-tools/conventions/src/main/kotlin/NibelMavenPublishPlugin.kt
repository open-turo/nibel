import com.turo.nibel.buildtools.NibelConventionPlugin
import com.turo.nibel.buildtools.libs
import com.turo.nibel.buildtools.mavenPublishing

class NibelMavenPublishPlugin : NibelConventionPlugin({
    with(pluginManager) {
        apply(libs.plugins.vanniktech.maven.publish.get().pluginId)
    }

    mavenPublishing {
        val version: String by properties
        coordinates(
            groupId = NibelMetadata.ARTIFACT_GROUP,
            artifactId = project.name,
            version = version
        )

        pom {
            name.set("Nibel")
            description.set("Type-safe navigation library for seamless adoption of Jetpack Compose in fragment-based Android apps.")
            inceptionYear.set("2023")
            url.set("https://github.com/open-turo/nibel")
            licenses {
                license {
                    name.set("The MIT License")
                    url.set("https://opensource.org/license/mit/")
                    distribution.set("https://opensource.org/license/mit/")
                }
            }
            developers {
                developer {
                    id.set("openturo")
                    name.set("Turo Open Source")
                    url.set("https://github.com/open-turo")
                }
            }
            scm {
                url.set("https://github.com/open-turo/nibel")
                connection.set("scm:git:git://github.com/open-turo/nibel.git")
                developerConnection.set("scm:git:ssh://git@github.com/open-turo/nibel.git")
            }
        }

        // SonatypeHost was removed in vanniktech 0.34.0; Central Portal is now the
        // only supported host. Read mavenCentralAutomaticPublishing from gradle.properties
        // to preserve the existing behaviour (currently true = auto-release after upload).
        val automaticRelease = (project.findProperty("mavenCentralAutomaticPublishing") as String?)
            ?.toBoolean() ?: false
        publishToMavenCentral(automaticRelease = automaticRelease)
        val signingKey = project.findProperty("signingInMemoryKey") as String?
        if (!signingKey.isNullOrBlank()) {
            signAllPublications()
        }
    }
})
