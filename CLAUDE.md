# Nibel - Type-Safe Navigation Library

## About This Project

**Nibel** is a type-safe navigation library for seamless integration of Jetpack Compose in fragment-based Android apps. It enables gradual migration from Fragment-based Android apps to Jetpack Compose while maintaining compatibility with existing codebases.

### Navigation Scenarios Supported

- **fragment → compose** - Navigate from existing fragments to Compose screens
- **compose → compose** - Navigate between Compose screens
- **compose → fragment** - Navigate from Compose screens back to fragments

### Key Features

- **Type-safe navigation** with compile-time checking
- **Single-module and multi-module** navigation support
- **Result-based navigation** with Activity Result API pattern
- **Annotation processing** for code generation via KSP
- **Seamless integration** with existing fragment-based codebases

## Tech Stack

- **Language**: Kotlin with official code style
- **Platform**: Android
- **Build System**: Gradle with Kotlin DSL
- **Framework**: Jetpack Compose + Android Fragments
- **Code Generation**: Kotlin Symbol Processing (KSP)
- **Architecture**: Multi-module project structure
- **Dependency Injection**: Hilt (in sample app)
- **Testing**: JUnit + Kotest assertions

## Project Structure

```
nibel/
├── nibel-annotations/     # Annotations for code generation (@UiEntry, @UiExternalEntry, etc.)
├── nibel-compiler/        # KSP processor for generating entry classes
├── nibel-runtime/         # Core runtime library with NavigationController
├── nibel-stub/           # Stub classes for compilation
├── tests/                # Integration and compilation tests
├── sample/               # Multi-module sample application
│   ├── app/             # Main sample application
│   ├── navigation/      # Shared navigation destinations
│   ├── feature-A/       # Sample feature module A
│   ├── feature-B/       # Sample feature module B
│   └── feature-C/       # Sample feature module C
└── build-tools/          # Gradle convention plugins for consistent build
    └── conventions/
```

## Essential Commands

### Development Workflow

```bash
# After making changes - run all verification
./gradlew check

# Full build verification (used in CI)
./gradlew build

# Fix lint issues automatically
./gradlew lintFix

# Run pre-commit hooks manually
pre-commit run --all-files
```

### Testing

```bash
# Run all unit tests
./gradlew test

# Run specific test variants
./gradlew testDebugUnitTest
./gradlew testReleaseUnitTest

# Run instrumentation tests (requires device)
./gradlew connectedAndroidTest
```

### Module-Specific Commands

```bash
# Build specific modules
./gradlew :nibel-runtime:build
./gradlew :nibel-compiler:build
./gradlew :sample:app:build

# Test changes locally
./gradlew publishToMavenLocal --no-configuration-cache
```

## Core Components

### Annotations

- **@UiEntry** - Mark composable functions as internal screen entries
- **@UiExternalEntry** - Mark composable functions as external (multi-module) screen entries
- **@LegacyEntry** / **@LegacyExternalEntry** - Apply to fragments for compose→fragment navigation

### Navigation

- **NavigationController** - Main navigation component for screen transitions
- **Entry classes** - Generated navigation entry points (e.g., `MyScreenEntry`)
- **Destinations** - Type-safe navigation targets for multi-module setups
- **Result API** - Type-safe result handling for screen interactions

## Code Style Guidelines

### Kotlin Conventions

- **Official Kotlin code style** configured in `gradle.properties`
- **4-space indentation** for all code
- **PascalCase** for classes and Composable functions
- **camelCase** for functions and variables
- **UPPER_SNAKE_CASE** for constants
- **Comprehensive KDoc** for all public APIs

### Package Structure

- `nibel.runtime` - Core runtime components
- `com.turo.nibel.sample.featureX` - Sample feature modules
- Feature-based module organization
- Clear separation of concerns (screens, ViewModels, navigation)

### Android-Specific

- **Composable functions**: PascalCase (e.g., `FirstScreen`, `PhotoPickerScreen`)
- **State management**: Use `collectAsStateWithLifecycle()` for observing state
- **Hilt integration**: `@HiltViewModel` and `hiltViewModel()` for dependency injection

## Development Patterns

### Entry Definition Patterns

```kotlin
// Internal entry (single module)
@UiEntry(type = ImplementationType.Fragment)
@Composable
fun MyScreen() { ... }

// External entry (multi-module)
@UiExternalEntry(
    type = ImplementationType.Composable,
    destination = MyScreenDestination::class
)
@Composable
fun MyScreen(args: MyScreenArgs) { ... }

// Result-returning screen
@UiEntry(
    type = ImplementationType.Composable,
    args = MyArgs::class,
    result = MyResult::class
)
@Composable
fun MyScreen(
    args: MyArgs,
    navigator: NavigationController
) { ... }
```

### Navigation Controller Usage

```kotlin
@Composable
fun MyScreen(navigator: NavigationController) {
    // Navigate to another screen
    navigator.navigateTo(TargetScreenEntry.newInstance(args))

    // Navigate for result
    navigator.navigateForResult(
        entry = ResultScreenEntry.newInstance(args),
        callback = { result: MyResult? ->
            // Handle result (null if cancelled)
        }
    )

    // Return result and navigate back
    navigator.setResultAndNavigateBack(result)
    navigator.cancelResultAndNavigateBack()
}
```

### Multi-Module Destinations

```kotlin
// No args destination
object MyScreenDestination : DestinationWithNoArgs

// With args destination
data class MyScreenDestination(
    override val args: MyScreenArgs
) : DestinationWithArgs<MyScreenArgs>
```

### State Management Pattern (Sample App)

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(MyState())
    val state: StateFlow<MyState> = _state.asStateFlow()

    private val _sideEffects = Channel<MySideEffect>()
    val sideEffects: Flow<MySideEffect> = _sideEffects.receiveAsFlow()

    fun onAction() {
        _state.value = _state.value.copy(/* updates */)
        // or emit side effect
        _sideEffects.trySend(MySideEffect.Navigate)
    }
}
```

## Task Completion Checklist

When completing development tasks, ensure you:

### Code Quality ✅

1. **Run verification**: `./gradlew check` - Must pass
2. **Fix lint issues**: `./gradlew lintFix` - Apply automatic fixes
3. **Verify tests**: `./gradlew test` - All tests must pass
4. **Pre-commit hooks**: `pre-commit run --all-files`

### Integration Testing ✅

5. **Build verification**: `./gradlew build` - Full build success
6. **Sample app**: `./gradlew :sample:app:build` - Test in context
7. **Local publishing**: `./gradlew publishToMavenLocal` (for library changes)
8. **Clean build**: `./gradlew clean build` - No stale artifacts

### Documentation & Testing ✅

9. **Update KDoc** for new public APIs
10. **Add unit tests** for new functionality
11. **Add integration tests** for new navigation patterns
12. **Update README.md** if public interface changes

### Git Best Practices ✅

13. **Conventional commits**: Use `feat:`, `fix:`, `docs:`, `refactor:` prefixes
14. **Branch hygiene**: Keep feature branches focused and small
15. **Rebase against main** before creating PR

## Build Tool Configuration

### Convention Plugins (in `build-tools/`)

- `nibel.android.library` - Standard Android library setup
- `nibel.kotlin.jvm.library` - JVM-only Kotlin library setup
- `nibel.maven.publish` - Publishing configuration
- `nibel.android.compose` - Jetpack Compose configuration

### Dependency Management

- **Version catalogs**: Use `libs.*` for consistent dependency versions
- **Project references**: Use `projects.*` syntax (e.g., `projects.nibelRuntime`)
- **KSP processors**: Configure with `ksp()` and `kspTest()` dependencies

## Testing Strategy

### Compilation Tests (`tests/src/test/kotlin/nibel/tests/codegen/`)

- Test code generation with various annotation combinations
- Verify generated classes have correct interfaces and methods
- Validate argument type matching between annotations and function parameters

### Integration Tests

- Test actual navigation flows end-to-end
- Test result-based navigation callback handling
- Test multi-module destination resolution
- Located alongside compilation tests

## Performance Considerations

### Gradle Configuration (`gradle.properties`)

- **Parallel builds** enabled (`org.gradle.parallel=true`)
- **Configuration cache** enabled (`org.gradle.unsafe.configuration-cache=true`)
- **Build cache** enabled (`org.gradle.caching=true`)
- **Kotlin incremental compilation** enabled
- **Resource optimization** for Android builds

### Navigation Performance

- Use `ImplementationType.Composable` when possible (avoids fragment overhead)
- Generated entry classes are optimized for minimal runtime cost
- Multi-module destination resolution cached after first lookup

## Debugging Tips

### Common Issues

1. **"Nibel not configured"**: Ensure `Nibel.configure()` is called in `Application.onCreate()`
2. **Missing entry factory**: Destination not associated with `@UiExternalEntry`
3. **Compilation errors**: Check argument types match between annotation and function parameters
4. **Navigation not working**: Verify correct `ImplementationType` for your navigation scenario

### Development Tools

- **Android lint** catches common navigation issues
- **KSP error messages** provide clear guidance for annotation problems
- **Sample app** demonstrates all navigation patterns
- **Pre-commit hooks** catch formatting and basic issues early

---

**Important**: Always run `./gradlew check` before considering any task complete. This ensures code quality, tests pass, and the project builds correctly across all modules.
