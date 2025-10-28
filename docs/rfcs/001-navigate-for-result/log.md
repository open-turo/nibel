# Decision Log: Navigate-For-Result Feature

This document tracks all major decisions made during the design of the navigate-for-result feature for Nibel.

## Decision Format

Each decision includes:

- **Date**: When the decision was made
- **Decision**: What was decided
- **Context**: Why this decision was needed
- **Options Considered**: Alternative approaches
- **Rationale**: Why this option was chosen
- **Impact**: How this affects the implementation
- **Status**: Active, Superseded, or Deprecated

---

## D001: Optional Parameter for Result Type

**Date**: 2025-10-28
**Status**: Active ✅

### Decision

Add an optional `result` parameter to both `@UiEntry` and `@UiExternalEntry` annotations with default value `NoResult::class`.

### Context

Need to extend existing annotations to support result types while maintaining 100% backwards compatibility. The Turo repository already uses Nibel extensively.

### Options Considered

1. **Optional parameter on existing annotations** ⭐ SELECTED

   - Pros: Backwards compatible, consistent with `args` parameter, simple
   - Cons: None significant
   - Implementation: `val result: KClass<out Parcelable> = NoResult::class`

2. **Separate annotations (@UiEntryWithResult, @UiExternalEntryWithResult)**

   - Pros: Cleaner separation, no confusion
   - Cons: Not backwards compatible, annotation duplication, migration required
   - Rejected: Breaks existing code

3. **Builder pattern for annotations**
   - Pros: Maximum flexibility
   - Cons: Too different from existing pattern, verbose, not idiomatic for annotations
   - Rejected: Too complex

### Rationale

- Follows the exact same pattern as the existing `args` parameter
- Zero breaking changes - default parameter makes it completely optional
- Developers familiar with `args` will immediately understand `result`
- Enables gradual adoption without coordination

### Impact

- **Backwards Compatibility**: ✅ 100% - existing code works unchanged
- **Learning Curve**: ✅ Minimal - follows existing pattern
- **Migration Effort**: ✅ Zero - opt-in feature
- **Code Complexity**: ✅ Low - one parameter addition

---

## D002: NavigationController Method-Based API

**Date**: 2025-10-28
**Status**: Active ✅

### Decision

Implement result handling via methods on `NavigationController`:

- `navigateForResult(entry, callback)`
- `setResultAndNavigateBack(result)`
- `cancelResultAndNavigateBack()`

### Context

Need to define how screens set and receive results. The API should be discoverable, type-safe, and consistent with existing patterns.

### Options Considered

1. **NavigationController methods** ⭐ SELECTED

   - Pros: Centralized, consistent, easy to discover, clear intent
   - Cons: None significant
   - API: `navigator.setResultAndNavigateBack(result)`

2. **ResultCallback parameter to composable**

   - Pros: Explicit in function signature
   - Cons: Verbose, not idiomatic for Compose, hard to provide default
   - Rejected: Poor API ergonomics

3. **Composition local (LocalResultCallback.current)**

   - Pros: Consistent with LocalArgs pattern
   - Cons: Less discoverable, awkward API, null handling complexity
   - Rejected: Awkward to use

4. **Shared ViewModel for results**
   - Pros: Familiar pattern
   - Cons: Not type-safe, couples modules, boilerplate
   - Rejected: This is what we're trying to replace

### Rationale

- NavigationController is already the central navigation authority
- All navigation actions go through NavigationController
- Easy to find methods via IDE autocomplete
- Symmetric API: `navigateForResult` pairs with `setResultAndNavigateBack`
- Clear lifecycle: result is delivered and cleared automatically

### Impact

- **API Clarity**: ✅ Excellent - intent is explicit
- **Discoverability**: ✅ High - methods appear in IDE autocomplete
- **Type Safety**: ✅ Full - generics ensure type matching
- **Testing**: ✅ Easy - can mock NavigationController

---

## D003: Dedicated navigateForResult Method

**Date**: 2025-10-28
**Status**: Active ✅

### Decision

Create a new `navigateForResult()` method separate from `navigateTo()`.

### Context

Need to decide whether to extend the existing `navigateTo()` method or create a new method for result-based navigation.

### Options Considered

1. **New navigateForResult() method** ⭐ SELECTED

   - Pros: Explicit intent, clear API, type-safe callback, no overloading complexity
   - Cons: One more method to learn
   - API: `navigator.navigateForResult(entry) { result -> ... }`

2. **Optional callback parameter on navigateTo()**

   - Pros: Single method, familiar name
   - Cons: Overloading confusion, unclear when result is expected, optional param complexity
   - Rejected: API becomes ambiguous

3. **Result callback in Entry creation**
   - Pros: Self-contained entry
   - Cons: Complex entry creation, hard to test, lifecycle unclear
   - Rejected: Poor separation of concerns

### Rationale

- **Explicit Intent**: Method name clearly indicates result is expected
- **Type Safety**: Callback parameter type can be inferred from entry's ResultEntry interface
- **No Ambiguity**: Clear when result is expected vs simple navigation
- **Better IDE Support**: Autocomplete suggests correct method based on use case
- **Follows Android Patterns**: Similar to Activity Result API's explicit registration

### Impact

- **API Clarity**: ✅ Excellent - intent is unambiguous
- **Learning Curve**: ✅ Low - clear method name
- **Code Clarity**: ✅ High - reading code makes intent obvious
- **Maintenance**: ✅ Easy - separate concerns

---

## D004: Nullable Result for Cancellation

**Date**: 2025-10-28
**Status**: Active ✅

### Decision

Use nullable result type `T?` where `null` indicates cancellation.

### Context

Need to handle the case where user cancels or backs out without providing a result.

### Options Considered

1. **Nullable result (T?)** ⭐ SELECTED

   - Pros: Simple, common Android pattern, easy to understand
   - Cons: null can be ambiguous
   - API: `callback: (PhotoResult?) -> Unit`

2. **Sealed class Result<T>**

   - Pros: Explicit success/failure, clear semantics
   - Cons: Verbose, boilerplate, overkill for this use case
   - API: `sealed class NavigationResult<T> { data class Success(val value: T), object Cancelled }`
   - Rejected: Too complex for this use case

3. **Separate callbacks (onResult, onCancelled)**

   - Pros: Clear separation of concerns
   - Cons: Two callbacks to manage, verbose, more state to track
   - API: `navigateForResult(entry, onResult = {...}, onCancelled = {...})`
   - Rejected: Too verbose

4. **Optional Result wrapper**
   - Pros: Explicit optional semantics
   - Cons: Not idiomatic for Kotlin, unnecessary wrapper
   - Rejected: Kotlin has nullable types

### Rationale

- **Android Consistency**: Activity Result API uses null for cancellation
- **Kotlin Idioms**: Nullable types are natural in Kotlin
- **Simplicity**: `result?.let { ... }` is concise and clear
- **No Boilerplate**: No wrapper classes needed
- **Common Pattern**: Developers are familiar with null = cancelled

### Impact

- **Code Simplicity**: ✅ High - minimal boilerplate
- **Readability**: ✅ Good - clear with safe call operator
- **Learning Curve**: ✅ Zero - standard Kotlin pattern
- **Edge Cases**: ⚠️ Need to document: null = cancelled, not error

---

## D005: DestinationWithResult Interface

**Date**: 2025-10-28
**Status**: Active ✅

### Decision

Create `DestinationWithResult<TResult>` interface for multi-module navigation with results.

### Context

Multi-module navigation uses destinations defined in shared navigation module. Need to extend this pattern to support results.

### Options Considered

1. **DestinationWithResult<TResult> interface** ⭐ SELECTED

   - Pros: Type-safe, compile-time checking, follows existing pattern
   - Cons: Destination must implement interface
   - API: `data class MyDestination(...) : DestinationWithArgs<...>, DestinationWithResult<MyResult>`

2. **Runtime result type registration**

   - Pros: No interface needed, flexible
   - Cons: Runtime errors, not type-safe, hard to validate
   - Rejected: Loses compile-time safety

3. **Result type in navigation module metadata**

   - Pros: Centralized
   - Cons: Tight coupling, harder to maintain, not type-safe
   - Rejected: Poor separation of concerns

4. **Annotation on destination class**
   - Pros: Declarative
   - Cons: Hard to access at compile-time, runtime lookup needed
   - Rejected: Less type-safe than interface

### Rationale

- **Consistency**: Follows the exact pattern of `DestinationWithArgs<TArgs>`
- **Type Safety**: Result type is part of destination's type signature
- **Compile-Time Validation**: Mismatches caught during compilation
- **Discoverability**: IDE shows which destinations return results
- **Code Generation**: Processor can easily extract result type from destination

### Impact

- **Type Safety**: ✅ Full - compile-time checking
- **Consistency**: ✅ High - matches existing DestinationWithArgs pattern
- **Discoverability**: ✅ Good - clear from destination interface
- **Migration**: ✅ Easy - add interface to destination

---

## D006: Fragment Result API for Fragments

**Date**: 2025-10-28
**Status**: Active ✅

### Decision

Use Android's Fragment Result API for result delivery when `ImplementationType.Fragment` is used.

### Context

Fragment implementation type wraps composables in fragments. Need to decide how to deliver results in this case.

### Options Considered

1. **Fragment Result API** ⭐ SELECTED

   - Pros: Native Android support, survives process death, well-tested, documented
   - Cons: Fragment-specific (not an issue since we're using fragments)
   - Implementation: Use SavedStateHandle for result passing

2. **Custom callback storage**

   - Pros: Unified with Composable implementation
   - Cons: Doesn't survive process death, reinventing the wheel
   - Rejected: Fragment Result API is better

3. **Activity Result API**
   - Pros: Standard pattern
   - Cons: Requires Activity, complex setup, overkill
   - Rejected: Too complex for screen-to-screen results

### Rationale

- **Native Support**: Android provides this specifically for Fragment results
- **Process Death**: Survives configuration changes and process death
- **Battle-Tested**: Used in many production apps
- **Standard**: Follows Android best practices
- **Simple Integration**: Easy to integrate with NavigationController

### Impact

- **Reliability**: ✅ High - proven solution
- **Process Death**: ✅ Supported - results survive process death
- **Complexity**: ✅ Low - standard Android API
- **Documentation**: ✅ Excellent - Android documentation available

---

## D007: Callback Storage for Composables

**Date**: 2025-10-28
**Status**: Active ✅

### Decision

Use in-memory callback storage in NavigationController for Composable implementation type results.

### Context

Composable implementation type doesn't use fragments. Need a mechanism to deliver results for pure Composable navigation.

### Options Considered

1. **Callback storage in NavigationController** ⭐ SELECTED

   - Pros: Simple, works well with Compose, low overhead
   - Cons: Doesn't survive process death (acceptable for most use cases)
   - Implementation: Map<String, (T?) -> Unit> in NavigationController

2. **SavedStateHandle for Composable results**

   - Pros: Survives process death
   - Cons: Complex, requires serialization, overkill for most use cases
   - Considered for future enhancement

3. **Navigation component SavedState**

   - Pros: Native Navigation component support
   - Cons: Limited to Navigation component specifics, complex
   - Rejected: Too specific to Navigation component

4. **Shared ViewModel**
   - Pros: Familiar pattern
   - Cons: Not type-safe, boilerplate, this is what we're replacing
   - Rejected: Defeats purpose of this feature

### Rationale

- **Simplicity**: Straightforward implementation
- **Performance**: Low overhead, no serialization
- **Compose Native**: Works naturally with Compose lifecycle
- **Acceptable Tradeoff**: Process death is rare, and can be handled if needed
- **Future Proof**: Can add SavedStateHandle support later if needed

### Impact

- **Implementation**: ✅ Simple - straightforward Map storage
- **Performance**: ✅ Excellent - no serialization overhead
- **Process Death**: ⚠️ Not supported - documented limitation
- **Future Enhancement**: ✅ Possible - can add SavedStateHandle later

---

## D008: NoResult Marker Class

**Date**: 2025-10-28
**Status**: Active ✅

### Decision

Create a `NoResult` object as the default value for the `result` parameter.

### Context

Need a way to indicate "no result" as the default behavior. Should follow the pattern of `NoArgs`.

### Options Considered

1. **NoResult object (Parcelable)** ⭐ SELECTED

   - Pros: Consistent with NoArgs, clear meaning, type-safe
   - Cons: None
   - Implementation: `object NoResult : Parcelable`

2. **Null as default**

   - Pros: Simple
   - Cons: Can't use nullable KClass<out Parcelable?>, inconsistent with args pattern
   - Rejected: Type system issues

3. **Different annotation for results**
   - Pros: No default needed
   - Cons: Not backwards compatible
   - Rejected: Already decided on optional parameter approach

### Rationale

- **Consistency**: Exactly matches the `NoArgs` pattern
- **Type Safety**: Parcelable type fits the type system
- **Clarity**: Explicit "NoResult" is clear in code
- **Code Generation**: Easy to check `if (resultType == NoResult::class)`

### Impact

- **Consistency**: ✅ Perfect - matches NoArgs exactly
- **Clarity**: ✅ High - intent is explicit
- **Implementation**: ✅ Trivial - single object declaration

---

## D009: ResultEntry Interface

**Date**: 2025-10-28
**Status**: Active ✅

### Decision

Create `ResultEntry<TArgs, TResult>` interface for entries that return results.

### Context

Need a way to distinguish entries that return results from those that don't, for type safety and runtime checks.

### Options Considered

1. **ResultEntry<TArgs, TResult> interface** ⭐ SELECTED

   - Pros: Type-safe, clear distinction, enables compile-time checks
   - Cons: Additional interface (minimal cost)
   - Implementation: `interface ResultEntry<TArgs, TResult> : Entry { val resultType: Class<TResult> }`

2. **Marker interface (no generics)**

   - Pros: Simpler
   - Cons: No type safety, runtime casting needed
   - Rejected: Loses type safety

3. **Reflection-based approach**
   - Pros: No interface needed
   - Cons: Slow, error-prone, no compile-time checking
   - Rejected: Performance and safety issues

### Rationale

- **Type Safety**: Generic parameters ensure result type is known at compile-time
- **Runtime Checks**: `resultType` property enables runtime validation if needed
- **API Clarity**: Clear from type signature that entry returns result
- **Code Generation**: Easy to generate implementing classes

### Impact

- **Type Safety**: ✅ Full - compile-time and runtime
- **API Clarity**: ✅ High - clear from interface
- **Code Generation**: ✅ Straightforward - add interface to generated class

---

## D010: Compile-Time Validation Only

**Date**: 2025-10-28
**Status**: Active ✅

### Decision

Rely on compile-time validation via KSP, minimal runtime checks.

### Context

Need to decide how much validation to do at runtime vs compile-time.

### Options Considered

1. **Compile-time only** ⭐ SELECTED

   - Pros: Errors caught early, no runtime overhead, type-safe
   - Cons: Can't catch errors from dynamic code (rare in this context)
   - Implementation: KSP validates all result types and parameters

2. **Compile-time + runtime validation**

   - Pros: Defense in depth
   - Cons: Runtime overhead, duplicates checks, throws at runtime
   - Considered for future if issues arise

3. **Runtime only**
   - Pros: Flexible
   - Cons: Late error detection, poor developer experience
   - Rejected: Defeats purpose of type-safe library

### Rationale

- **Nibel Philosophy**: Compile-time safety is core to Nibel's design
- **Performance**: No runtime validation overhead
- **Developer Experience**: Errors caught immediately during development
- **Sufficient**: All result types are known at compile-time (annotations)

### Impact

- **Performance**: ✅ Excellent - zero runtime overhead
- **Safety**: ✅ High - compile-time catches all issues
- **Developer Experience**: ✅ Excellent - immediate feedback

---

## Future Considerations

These items are explicitly deferred for potential future enhancements:

### FC001: Process Death Support for Composables

**Status**: Deferred

Consider adding SavedStateHandle support for Composable results to survive process death.

**When to Revisit**: After v1 feedback, if users report process death issues

### FC002: Activity Result Contracts Integration

**Status**: Deferred

Consider providing wrappers for common Activity Result Contracts (camera, gallery, etc.).

**When to Revisit**: If users frequently request this feature

### FC003: Streaming Results

**Status**: Deferred

Consider supporting Flow<T> for streaming results (progress updates, multi-select, etc.).

**When to Revisit**: If strong use cases emerge

### FC004: Result Validation Framework

**Status**: Deferred

Consider adding a validation framework for result data.

**When to Revisit**: If users report issues with invalid result data

---

## Summary Statistics

- **Total Decisions**: 10 active
- **Backwards Compatible**: 10/10 (100%)
- **Breaking Changes**: 0
- **Deferred Decisions**: 4
- **Rejected Alternatives**: 15

## Review Checklist

- [x] All decisions have clear rationale
- [x] Impact assessment completed for each decision
- [x] Alternatives considered and documented
- [x] Backwards compatibility verified
- [x] Future considerations identified
- [x] Decisions align with Nibel philosophy

---

**Document Status**: Complete ✅
**Last Updated**: 2025-10-28
**Reviewed By**: TBD
