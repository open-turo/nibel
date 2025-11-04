# Nibel Architecture & Design Patterns

## Core Architecture Patterns

### Navigation Controller Pattern

- **NavigationController**: Central navigation component injected into Composables
- **Entry Classes**: Generated navigation entry points (e.g., `MyScreenEntry`)
- **Type-safe**: Compile-time checking for navigation arguments and types

### Annotation-Driven Code Generation

- **@UiEntry**: Internal screen entries (single module)
- **@UiExternalEntry**: External entries (multi-module)
- **@LegacyEntry/@LegacyExternalEntry**: Fragment integration
- **KSP Processor**: Generates entry classes and navigation boilerplate

### Multi-Module Navigation Pattern

```
 featureA          featureB
  module            module
    │                  │
    └──► navigation ◄──┘
           module
```

- **Destinations**: Type-safe navigation intents in shared navigation module
- **DestinationWithNoArgs**: Simple object destinations
- **DestinationWithArgs<T>**: Parameterized destinations

### Result-Based Navigation Pattern

- **Activity Result API**: Integration for type-safe result handling
- **ResultEntry<T>**: Generated interfaces for result-returning screens
- **Callbacks**: Strongly typed result callbacks with null handling for cancellation

## State Management Pattern (Sample App)

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(MyState())
    val state: StateFlow<MyState> = _state.asStateFlow()

    private val _sideEffects = Channel<MySideEffect>()
    val sideEffects: Flow<MySideEffect> = _sideEffects.receiveAsFlow()
}
```

## Implementation Types Strategy

- **ImplementationType.Composable**: Preferred for performance (avoids fragment overhead)
- **ImplementationType.Fragment**: Required when navigating FROM existing fragments
- **Gradual Migration**: Start with fragments, migrate to Composable over time

## Code Generation Patterns

- **Entry Factories**: Generated companion objects with `newInstance()` methods
- **Type Safety**: Compile-time verification of argument types between annotations and function parameters
- **Destination Resolution**: Cached multi-module destination lookup after first access

## Testing Patterns

- **Compilation Tests**: Verify generated code has correct interfaces and methods
- **Integration Tests**: Test actual navigation flows end-to-end
- **Type Validation**: Ensure argument type matching between annotations and function parameters

## Performance Considerations

- Use `ImplementationType.Composable` when possible
- Generated entry classes optimized for minimal runtime cost
- Multi-module destination resolution cached after first lookup
