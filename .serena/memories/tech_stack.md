# Nibel Tech Stack

## Core Technologies

- **Language**: Kotlin with official code style (configured in gradle.properties)
- **Platform**: Android
- **Build System**: Gradle with Kotlin DSL
- **Framework**: Jetpack Compose + Android Fragments
- **Code Generation**: Kotlin Symbol Processing (KSP)
- **Architecture**: Multi-module project structure

## Sample App Technologies

- **Dependency Injection**: Hilt
- **State Management**: StateFlow + Compose collectAsStateWithLifecycle()
- **Navigation**: Nibel library (obviously)

## Testing Technologies

- **Unit Testing**: JUnit
- **Assertions**: Kotest assertions
- **Integration Testing**: Custom compilation tests for KSP code generation
- **Test Structure**: Located in `tests/` module with compilation and integration tests

## Build & Development Tools

- **KSP**: For annotation processing and code generation
- **Detekt**: Kotlin static analysis and linting
- **Android Lint**: Android-specific linting with lintFix capability
- **Pre-commit hooks**: Automated formatting, linting, and commit message validation
- **Prettier**: Code formatting
- **Commitlint**: Conventional commit message validation
- **Dokka**: Documentation generation

## Gradle Configuration

- **Convention Plugins**: Custom plugins in `build-tools/conventions/`
- **Version Catalogs**: Using `libs.*` for dependency management
- **Performance Optimizations**: Parallel builds, configuration cache, build cache enabled
