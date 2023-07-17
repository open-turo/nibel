package com.turo.nibel.buildtools

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

typealias CommonExtension = com.android.build.api.dsl.CommonExtension<*, *, *, *>

// Makes 'libs' version catalog available for precompiled plugins in a type-safe manner.
// https://github.com/gradle/gradle/issues/15383#issuecomment-1245546796
val Project.libs get() = extensions.getByType<LibrariesForLibs>()

fun <T : CommonExtension> Project.android(body: T.() -> Unit) {
    @Suppress("UNCHECKED_CAST")
    (extensions.getByName("android") as T).apply(body)
}

fun CommonExtension.kotlinOptions(body: KotlinJvmOptions.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlinOptions", body)
}

fun <T> NamedDomainObjectContainer<T>.release(body: T.() -> Unit) {
    getByName("release", body)
}

fun DependencyHandlerScope.implementation(dependencyNotation: Any) {
    add("implementation", dependencyNotation)
}

fun DependencyHandlerScope.debugImplementation(dependencyNotation: Any) {
    add("debugImplementation", dependencyNotation)
}
