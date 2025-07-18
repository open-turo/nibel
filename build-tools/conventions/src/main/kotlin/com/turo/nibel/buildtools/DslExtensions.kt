package com.turo.nibel.buildtools

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinTarget

typealias CommonExtension = com.android.build.api.dsl.CommonExtension<*, *, *, *, *, *>

// Makes 'libs' version catalog available for precompiled plugins in a type-safe manner.
// https://github.com/gradle/gradle/issues/15383#issuecomment-1245546796
val Project.libs get() = extensions.getByType<LibrariesForLibs>()

fun <T : CommonExtension> Project.android(body: T.() -> Unit) {
    @Suppress("UNCHECKED_CAST")
    (extensions.getByName("android") as T).apply(body)
}

fun Project.kotlin(body: KotlinSingleTargetExtension<AbstractKotlinTarget>.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("kotlin", body)

fun CommonExtension.kotlinOptions(body: KotlinJvmOptions.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlinOptions", body)
}

fun <T> NamedDomainObjectContainer<T>.release(body: T.() -> Unit) {
    getByName("release", body)
}

val Project.sourceSets: SourceSetContainer
    get() = (this as ExtensionAware).extensions
        .getByName("sourceSets") as SourceSetContainer

fun Project.mavenPublishing(configure: MavenPublishBaseExtension.() -> Unit) {
    (this as ExtensionAware).extensions.configure("mavenPublishing", configure)
}


fun DependencyHandlerScope.implementation(dependencyNotation: Any) {
    add("implementation", dependencyNotation)
}

fun DependencyHandlerScope.debugImplementation(dependencyNotation: Any) {
    add("debugImplementation", dependencyNotation)
}
