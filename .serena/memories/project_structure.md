# Nibel Project Structure

## Root Directory Structure

```
nibel/
├── nibel-annotations/     # Annotations for code generation
├── nibel-compiler/        # KSP processor for generating entry classes
├── nibel-runtime/         # Core runtime library with NavigationController
├── nibel-stub/           # Stub classes for compilation
├── tests/                # Integration and compilation tests
├── sample/               # Multi-module sample application
├── build-tools/          # Gradle convention plugins
├── config/               # Configuration files
├── docs/                 # Documentation
└── .github/              # GitHub workflows and templates
```

## Core Modules

### nibel-annotations/

- Contains `@UiEntry`, `@UiExternalEntry`, `@LegacyEntry` annotations
- Defines annotation parameters and validation rules
- Minimal dependencies, used at compile-time only

### nibel-compiler/

- KSP (Kotlin Symbol Processing) implementation
- Generates entry classes, destination factories, result handlers
- Validates annotation usage and argument type matching
- Produces type-safe navigation boilerplate code

### nibel-runtime/

- Core `NavigationController` implementation
- Entry interfaces (`ComposableEntry`, `FragmentEntry`, `ResultEntry`)
- Destination resolution and factory management
- Result-based navigation infrastructure

### nibel-stub/

- Stub implementations for generated classes during compilation
- Ensures clean compilation before code generation completes

## Sample Application Structure

```
sample/
├── app/             # Main application module
├── navigation/      # Shared navigation destinations
├── feature-A/       # Independent feature module
├── feature-B/       # Independent feature module
├── feature-C/       # Independent feature module
└── common/         # Shared utilities and components
```

### Multi-Module Dependencies

- Features depend on `navigation/` for shared destinations
- Features do NOT depend on each other directly
- `app/` module depends on all features for final assembly

## Testing Structure

```
tests/src/test/kotlin/nibel/tests/
├── codegen/                    # Code generation tests
│   ├── InternalEntryCompileTest.kt
│   ├── ExternalEntryCompileTest.kt
│   ├── ResultEntryCompileTest.kt
│   └── TestArgs.kt
└── ExternalDestinationSearchTest.kt
```

## Build Tools Structure

```
build-tools/conventions/
├── src/main/kotlin/
│   ├── NibelAndroidCommonPlugin.kt
│   ├── NibelKotlinJvmLibraryPlugin.kt
│   └── SampleAndroidApplicationPlugin.kt
└── build.gradle.kts
```

## Configuration Files

- `.pre-commit-config.yaml`: Pre-commit hooks configuration
- `gradle.properties`: Gradle and Kotlin configuration
- `settings.gradle.kts`: Project module configuration
- `CLAUDE.md`: Development guidelines and instructions
