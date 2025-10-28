# Nibel Project Overview

## Purpose

Nibel is a type-safe navigation library for seamless integration of Jetpack Compose in fragment-based Android apps. It enables gradual migration from Fragment-based Android apps to Jetpack Compose while maintaining compatibility with existing codebases.

## Key Features

- **Type-safe navigation** with compile-time checking between fragments and Compose screens
- **Multi-directional navigation**: fragment→compose, compose→compose, compose→fragment
- **Single-module and multi-module** navigation support
- **Result-based navigation** with Activity Result API pattern
- **Annotation processing** for code generation via KSP (Kotlin Symbol Processing)
- **Seamless integration** with existing fragment-based codebases

## Navigation Scenarios Supported

- fragment → compose (existing fragments to new Compose screens)
- compose → compose (between Compose screens)
- compose → fragment (Compose screens back to legacy fragments)

## Main Components

- **nibel-runtime**: Core runtime library with NavigationController
- **nibel-compiler**: KSP processor for generating entry classes
- **nibel-annotations**: Annotations (@UiEntry, @UiExternalEntry, etc.)
- **nibel-stub**: Stub classes for compilation
- **sample**: Multi-module sample application demonstrating usage patterns
- **tests**: Integration and compilation tests
- **build-tools**: Gradle convention plugins for consistent builds
