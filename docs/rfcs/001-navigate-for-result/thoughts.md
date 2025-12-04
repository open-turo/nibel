# Thoughts & Research: Navigate-For-Result Feature

## Initial Context

The Nibel library currently supports type-safe navigation between screens with optional arguments, but lacks a mechanism for screens to return results to their callers. This is a common pattern in Android applications (Activity Result API, Fragment Result API) and is essential for many user flows.

## Research Summary

### Current Nibel Architecture

#### 1. Annotations

The library uses two main annotations for marking screens:

**@UiEntry** - For internal (single-module) navigation:

```kotlin
annotation class UiEntry(
    val type: ImplementationType,
    val args: KClass<out Parcelable> = NoArgs::class
)
```

**@UiExternalEntry** - For multi-module navigation:

```kotlin
annotation class UiExternalEntry(
    val type: ImplementationType,
    val destination: KClass<out ExternalDestination>
)
```

Both support two implementation types:

- `ImplementationType.Fragment` - Generates a Fragment wrapper
- `ImplementationType.Composable` - Generates a lightweight Composable wrapper

#### 2. Code Generation (KSP)

The code generation flow:

1. `UiEntryProcessor` - Main KSP processor that handles both annotations
2. `EntryGeneratingVisitor` - Visits annotated functions and collects metadata
3. Generator classes:
   - `ComposableGenerator` - Generates Composable entry classes
   - `FragmentGenerator` - Generates Fragment entry classes
   - `EntryFactoryProviderGenerator` - Generates factory registration for external entries

Key metadata structure:

```kotlin
sealed interface EntryMetadata {
    val argsQualifiedName: String?
    val parameters: Map<ParameterType, ParameterMetadata>
}

data class InternalEntryMetadata(...)
data class ExternalEntryMetadata(
    val destinationName: String,
    val destinationPackageName: String,
    val destinationQualifiedName: String,
    ...
)
```

#### 3. Navigation Flow

Current navigation methods in `NavigationController`:

```kotlin
abstract fun navigateBack()
abstract fun navigateTo(entry: Entry, ...)
abstract fun navigateTo(externalDestination: ExternalDestination, ...)
```

Entry classes are generated with companion objects containing `newInstance()` factory methods:

```kotlin
companion object {
    fun newInstance(args: MyArgs): ComposableEntry<MyArgs> {
        return MyScreenEntry(args, name)
    }
}
```

#### 4. Multi-Module Support

Destinations are defined in a shared navigation module:

- `DestinationWithNoArgs` - Simple object destinations
- `DestinationWithArgs<TArgs>` - Destinations with arguments

Feature modules implement screens with `@UiExternalEntry`, referencing destinations.
The main app module depends on all features and can navigate between them.

### Testing Infrastructure

**Compilation Tests** (`tests/src/test/kotlin/nibel/tests/codegen/`):

- `InternalEntryCompileTest.kt` - Tests @UiEntry code generation
- `ExternalEntryCompileTest.kt` - Tests @UiExternalEntry code generation
- `InternalEntryParamCompileTest.kt` - Tests parameter handling
- `ExternalEntryParamCompileTest.kt` - Tests external parameter handling

Tests verify that:

- Generated entry classes implement correct interfaces
- Companion object factory methods have correct signatures
- Argument types match between annotation and function parameters

**Sample App** (`sample/`):

- Multi-module structure demonstrating real-world usage
- feature-A, feature-B, feature-C independent feature modules
- Shared navigation module with destinations
- Uses Hilt for dependency injection
- State management with StateFlow and side effects via Channel

### Key Findings

1. **Arguments Pattern**: The existing `args` parameter is optional with default `NoArgs::class`. This provides a clear pattern for adding `result` parameter.

2. **Type Safety**: Compile-time validation is critical. The processor validates that argument types in annotations match function parameters.

3. **Generated Code**: Entry classes extend base interfaces (`ComposableEntry<TArgs>`, `FragmentEntry`). We can extend this with `ResultEntry<TArgs, TResult>`.

4. **Multi-Module**: Destinations need to be extended to support result types for external entries.

5. **No Result Mechanism**: Currently there is NO built-in result mechanism. Search for "navigateForResult", "setResult", "ResultEntry" returned no matches in nibel-runtime.

## Requirements & Constraints

### Must Have

1. **Backwards Compatibility**: Existing code MUST work without changes. The Turo repository already uses Nibel.
2. **Type Safety**: Result types must be validated at compile-time.
3. **Works for both annotations**: @UiEntry and @UiExternalEntry
4. **Supports both implementation types**: Fragment and Composable
5. **Multi-module support**: Results must work across feature modules

### Should Have

1. **Clear API**: Easy to understand and discover
2. **Consistent**: Follows existing Nibel patterns
3. **Testable**: Easy to write tests for result handling
4. **Documentation**: Clear examples and migration guide

### Nice to Have

1. **Process death handling**: Results survive process death (for Fragment implementation)
2. **Lifecycle awareness**: Proper cleanup of result callbacks
3. **Debugging support**: Clear error messages for misuse

## Design Decisions

### 1. API Design: Optional Parameter ✅

**Decision**: Add optional `result` parameter to existing annotations

**Rationale**:

- 100% backwards compatible
- Consistent with how `args` works
- Simple and easy to understand
- No annotation duplication

**Alternative Considered**: Separate `@UiEntryWithResult` annotation

- Rejected: Not backwards compatible, creates duplication

### 2. Callback API: NavigationController Methods ✅

**Decision**: Add methods to NavigationController:

- `navigateForResult(entry, callback)`
- `setResultAndNavigateBack(result)`
- `cancelResultAndNavigateBack()`

**Rationale**:

- Centralized in one place (NavigationController)
- Easy to discover
- Consistent with existing navigation methods
- Clear intent

**Alternative Considered**: ResultCallback as composable parameter

- Rejected: Not idiomatic for Compose, verbose

**Alternative Considered**: Composition local for result callback

- Rejected: Less discoverable, awkward API

### 3. Caller API: New navigateForResult Method ✅

**Decision**: Add new `navigateForResult()` method separate from `navigateTo()`

**Rationale**:

- Makes intent explicit (expecting a result)
- Type-safe callback parameter
- Doesn't clutter existing `navigateTo()` API
- Clear separation of concerns

**Alternative Considered**: Optional callback parameter on `navigateTo()`

- Rejected: Overloading complexity, unclear when result is expected

### 4. Cancellation: Nullable Result ✅

**Decision**: Callback receives `T?` where `null` indicates cancellation

**Rationale**:

- Simple and common Android pattern (Activity Result API uses this)
- No additional types needed
- Easy to understand: `result?.let { ... }`

**Alternative Considered**: Sealed class `Result<T>`

- Rejected: More verbose, adds boilerplate

**Alternative Considered**: Separate onResult and onCancelled callbacks

- Rejected: Two callbacks to manage, verbose

### 5. Multi-Module: DestinationWithResult Interface ✅

**Decision**: Create `DestinationWithResult<TResult>` interface

**Rationale**:

- Type-safe at compile-time
- Follows existing pattern (DestinationWithArgs)
- Clear declaration of result type
- Easy to validate in code generation

### 6. Implementation: Fragment Result API for Fragments ✅

**Decision**: Use Android's Fragment Result API for Fragment implementation type

**Rationale**:

- Native Android support
- Survives process death
- Well-tested and documented
- Standard approach

**Implementation Note**: For Composable implementation type, use callback storage in NavigationController (doesn't survive process death but acceptable for most use cases).

## Implementation Strategy

### Phase 1: Foundation

- Add `result` parameter to annotations (default `NoResult::class`)
- Create `NoResult` marker class
- Create `ResultEntry<TArgs, TResult>` interface
- Create `DestinationWithResult<TResult>` interface

### Phase 2: NavigationController API

- Add `navigateForResult()` methods
- Add `setResultAndNavigateBack()` method
- Add `cancelResultAndNavigateBack()` method
- Implement result delivery mechanism in `NibelNavigationController`

### Phase 3: Code Generation

- Update `EntryMetadata` to include `resultQualifiedName`
- Modify `EntryGeneratingVisitor` to extract result parameter
- Update generators to produce result-aware entry classes
- Implement result type validation

### Phase 4: Testing

- Create `ResultEntryCompileTest.kt`
- Add integration tests for result delivery
- Add sample app demonstrations
- Test all combinations (internal/external, fragment/composable, args/no-args, result/no-result)

### Phase 5: Documentation

- Update KDoc for all public APIs
- Write migration guide
- Update README with examples
- Create comprehensive usage guide

## Open Questions

### 1. Process Death for Composables

Should Composable entries support result delivery after process death?

**Current Approach**: No (use callback storage, doesn't survive process death)
**Future Enhancement**: Use SavedStateHandle for Composable results (complex)

**Decision**: Start without process death support for Composables, evaluate based on user feedback.

### 2. Activity Result Contracts

Should we integrate with Activity Result Contracts for system features (camera, gallery, permissions)?

**Decision**: Not in initial release. Users can call Activity Result API directly. Consider as future enhancement.

### 3. Multiple Results / Streaming

Should we support streaming multiple results (e.g., progress updates)?

**Decision**: No. For complex scenarios, use SharedViewModel or event bus. Keep initial feature simple.

### 4. Nested Navigate-For-Result

What happens when screen A → B (for result) → C (for result)?

**Expected Behavior**: Should work naturally with callback stacking. Each screen tracks its own result callback.

**Decision**: Support naturally, test in integration tests.

## Success Metrics

1. **Zero Breaking Changes**: All existing tests pass without modification
2. **Comprehensive Testing**: New feature has >90% test coverage
3. **Clear Documentation**: Usage examples for all scenarios
4. **Performance**: No significant performance impact on navigation
5. **Adoption**: Feature is easy to adopt incrementally

## References

- Activity Result API: https://developer.android.com/training/basics/intents/result
- Fragment Result API: https://developer.android.com/guide/fragments/communicate#fragment-result
- Navigation Result Handling: https://developer.android.com/guide/navigation/navigation-programmatic#returning_a_result
- Nibel Architecture Patterns (memory)
- Nibel Project Structure (memory)

## Timeline Estimate

- Phase 1 (Foundation): 3 days
- Phase 2 (NavigationController): 5 days
- Phase 3 (Code Generation): 7 days
- Phase 4 (Testing): 7 days
- Phase 5 (Documentation): 3 days

**Total: ~5 weeks**

## Risks & Mitigation

### Risk: Breaking Existing Code

**Mitigation**: Optional parameter with default value, extensive regression testing

### Risk: Type Safety Gaps

**Mitigation**: Compile-time validation via KSP, comprehensive type checking tests

### Risk: Process Death Edge Cases

**Mitigation**: Document limitations for Composable implementation, use Fragment Result API for Fragment implementation

### Risk: Complex Multi-Module Setup

**Mitigation**: Clear examples in sample app, comprehensive documentation

### Risk: Memory Leaks from Callbacks

**Mitigation**: Clear callbacks after use, ensure NavigationController is ViewModel-scoped, add leak detection

## Next Steps

1. Review and approve RFC with team
2. Create tracking issue for implementation
3. Set up project board with phases
4. Begin Phase 1 implementation
5. Regular check-ins on progress

---

**Document Status**: Research Complete ✅
**Last Updated**: 2025-10-28
**Ready for**: RFC Review
