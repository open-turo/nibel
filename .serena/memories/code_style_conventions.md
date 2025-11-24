# Nibel Code Style & Conventions

## Kotlin Style Guidelines

- **Official Kotlin code style** configured in `gradle.properties` (`kotlin.code.style=official`)
- **4-space indentation** for all code
- **PascalCase** for classes and Composable functions
- **camelCase** for functions and variables
- **UPPER_SNAKE_CASE** for constants
- **Comprehensive KDoc** required for all public APIs

## Android-Specific Conventions

- **Composable functions**: PascalCase (e.g., `FirstScreen`, `PhotoPickerScreen`)
- **State management**: Use `collectAsStateWithLifecycle()` for observing state
- **Hilt integration**: `@HiltViewModel` and `hiltViewModel()` for dependency injection

## Package Structure

- `nibel.runtime` - Core runtime components
- `com.turo.nibel.sample.featureX` - Sample feature modules
- Feature-based module organization
- Clear separation of concerns (screens, ViewModels, navigation)

## Annotation Patterns

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
```

## Naming Conventions

- **Entry classes**: `{ComposableName}Entry` (auto-generated)
- **Destination classes**: `{Screen}Destination`
- **Args classes**: `{Screen}Args`
- **Result classes**: `{Screen}Result`
- **ViewModels**: `{Screen}ViewModel` (in sample app)

## File Organization

- Keep related functionality together in feature modules
- Prefer editing existing files over creating new ones
- Use symbolic tools for precise code modifications
- Follow existing patterns when adding new code
