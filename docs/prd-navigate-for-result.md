# Navigate-For-Result: Type-Safe Result Handling

**Priority**: High
**Summary**: Add type-safe result handling to Nibel's navigation system, enabling screens to return data to their callers with compile-time validation
**Jira**: TBD
**Branch**: `f/result-navigation-feature`
**Status**: PLANNING

## Subagents

TBD - Will coordinate with technical experts for:

- Android Architecture Review (lifecycle and process death handling)
- API Design Review (type safety and ergonomics)
- Code Generation Review (KSP implementation)
- Testing Strategy Review (coverage and test scenarios)

## Description

This feature adds native support for navigate-for-result patterns in Nibel, allowing screens to return typed data to their callers with compile-time validation. Similar to how arguments flow into screens (via the `args` parameter), this feature enables data to flow back out through results.

The implementation follows Android's established Activity Result API pattern, providing a familiar developer experience while leveraging Nibel's existing type-safety guarantees through KSP code generation.

## Details

### Problem Statement

Modern Android applications frequently need screens to return data to their callers, including:

- **User selection flows**: Photo picker, contact selector, location picker
- **Form submissions**: Collecting user input across screens
- **Decision dialogs**: Approval/rejection, option selection
- **Data creation**: Creating items and returning their IDs
- **Multi-step workflows**: Gathering information across multiple screens

Currently, developers must use workarounds:

- **Shared ViewModels**: Couples modules, breaks encapsulation, doesn't scale across feature boundaries
- **Event buses**: Lacks type safety, difficult to track data flow
- **Navigation arguments in reverse**: Complex, error-prone, feels unnatural
- **Direct callback passing**: Doesn't survive process death or configuration changes

These approaches are either unsafe, unmaintainable, or don't fit well with Nibel's multi-module architecture.

### Goals

1. **Type Safety**: Compile-time validation that result types match between caller and callee
2. **Zero Breaking Changes**: Existing code works without modifications
3. **Multi-Module Support**: Works seamlessly across feature module boundaries
4. **Consistency**: Follows Nibel's existing design patterns (similar to args handling)
5. **Opt-in**: Feature is optional, not required for simple navigation
6. **Android Integration**: Leverages Activity Result API pattern for familiar developer experience

### Non-Goals

1. **Process death recovery for Composable results**: Initial version won't support result delivery after process death for Composable entries (Fragment entries will support this via Fragment Result API)
2. **Activity Result Contract integration**: Won't provide wrappers for system Activity Result Contracts in initial release
3. **Streaming results**: Won't support multiple result deliveries (Flow-based results)
4. **Result validation framework**: No built-in validation beyond type checking

### User Stories

#### As a feature developer, I want to...

1. **Create a photo picker screen that returns selected photos**

   - Define a result type with selected photo URIs
   - Navigate to the picker and receive results
   - Handle both successful selection and cancellation

2. **Build a multi-step form that returns completed data**

   - Create multiple screens that collect information
   - Return aggregate data when user completes the flow
   - Handle cancellation at any step

3. **Show a confirmation dialog and receive the user's decision**

   - Display a dialog asking for confirmation
   - Return true/false based on user choice
   - Distinguish between explicit "no" and cancellation

4. **Navigate across feature modules with results**

   - Call screens in other feature modules
   - Receive typed results back across module boundaries
   - Maintain type safety without tight coupling

5. **Create a user selector that returns selected user data**
   - Display a list of users
   - Return the selected user's information
   - Handle case where no user is selected

### Success Metrics

#### Development Experience

- **Compilation time**: No significant increase (< 5% impact)
- **Generated code size**: Minimal increase for entries with results
- **API discoverability**: Developers can find and use the API without extensive documentation

#### Code Quality

- **Type safety**: 100% compile-time validation of result types
- **Test coverage**: 90%+ coverage for new code
- **Backwards compatibility**: Zero breaking changes to existing code

#### Adoption

- **Migration effort**: < 30 minutes to add results to an existing screen
- **Documentation clarity**: Developers understand the feature from examples
- **Error messages**: Clear, actionable error messages when types mismatch

## Functional Requirements

### FR-1: Annotation Support for Results

#### FR-1.1: @UiEntry Result Parameter

- **MUST** add optional `result` parameter to `@UiEntry` annotation
- **MUST** default to `NoResult::class` for backwards compatibility
- **MUST** accept any `Parcelable` type as result
- **MUST** validate result type is Parcelable at compile-time

#### FR-1.2: @UiExternalEntry Result Parameter

- **MUST** add optional `result` parameter to `@UiExternalEntry` annotation
- **MUST** default to `NoResult::class` for backwards compatibility
- **MUST** work with multi-module destinations
- **MUST** support both `DestinationWithArgs` and `DestinationWithNoArgs`

#### FR-1.3: NoResult Marker Class

- **MUST** create `NoResult` marker object that implements `Parcelable`
- **MUST** use `NoResult` as default for result parameter
- **MUST** be located in `nibel-annotations` module

### FR-2: NavigationController API

#### FR-2.1: Navigate For Result

- **MUST** add `navigateForResult()` method accepting an Entry
- **MUST** add `navigateForResult()` method accepting an ExternalDestination
- **MUST** require a callback parameter of type `(TResult?) -> Unit`
- **MUST** support both Fragment and Composable implementation types
- **MUST** provide type-safe callback with correct result type

#### FR-2.2: Set Result and Navigate Back

- **MUST** add `setResultAndNavigateBack()` method accepting result
- **MUST** validate result type matches entry's result type
- **MUST** deliver result to stored callback
- **MUST** automatically navigate back after setting result
- **MUST** clean up callback storage after delivery

#### FR-2.3: Cancel Result and Navigate Back

- **MUST** add `cancelResultAndNavigateBack()` method with no parameters
- **MUST** deliver `null` to callback when cancelled
- **MUST** automatically navigate back after cancellation
- **MUST** clean up callback storage after cancellation

### FR-3: Code Generation

#### FR-3.1: Result Entry Interface

- **MUST** generate `ResultEntry<TArgs, TResult>` interface implementation
- **MUST** include `resultType: Class<TResult>` property
- **MUST** extend appropriate base entry type (ComposableEntry or FragmentEntry)
- **MUST** maintain all existing entry functionality

#### FR-3.2: Entry Factory Updates

- **MUST** update companion object factory methods for result entries
- **MUST** return correct type (ResultEntry vs Entry) from newInstance()
- **MUST** initialize result-specific properties
- **MUST** maintain backwards compatibility for non-result entries

#### FR-3.3: External Entry Factory Support

- **MUST** update EntryFactoryProvider for result-aware destinations
- **MUST** associate destinations with result types
- **MUST** generate factory methods that handle results
- **MUST** support DestinationWithResult interface

### FR-4: Multi-Module Support

#### FR-4.1: Destination Result Interface

- **MUST** create `DestinationWithResult<TResult>` interface
- **MUST** include `resultType: Class<TResult>` property
- **MUST** allow destinations to declare result types
- **MUST** work with both args and no-args destinations

#### FR-4.2: Cross-Module Result Delivery

- **MUST** deliver results across module boundaries
- **MUST** maintain type safety across modules
- **MUST** not require tight coupling between modules
- **MUST** support runtime destination resolution

### FR-5: Lifecycle and State Management

#### FR-5.1: Fragment Result Handling

- **MUST** use Fragment Result API for Fragment implementation types
- **MUST** survive process death for Fragment results
- **MUST** survive configuration changes for Fragment results
- **MUST** clean up listeners when fragments are destroyed

#### FR-5.2: Composable Result Handling

- **MUST** store callbacks in NavigationController for Composables
- **MUST** survive configuration changes when NavigationController is ViewModel-scoped
- **SHOULD** document that Composable results don't survive process death
- **MUST** clean up callbacks when results are delivered

#### FR-5.3: Callback Management

- **MUST** generate unique request keys for each navigation
- **MUST** store callbacks with request keys
- **MUST** deliver results using request keys
- **MUST** remove callbacks after delivery or cancellation
- **MUST** prevent memory leaks from stored callbacks

## Technical Requirements

### TR-1: Type Safety

#### TR-1.1: Compile-Time Validation

- **MUST** validate result types are Parcelable at compile-time
- **MUST** validate result type in annotation matches function signature
- **MUST** generate compile errors for type mismatches
- **MUST** provide clear error messages for type issues

#### TR-1.2: Runtime Type Safety

- **MUST** maintain type safety at runtime through generics
- **MUST** store result types in generated classes
- **MUST** enable runtime type checking if needed
- **SHOULD** avoid unchecked casts in generated code

### TR-2: Backwards Compatibility

#### TR-2.1: API Compatibility

- **MUST** maintain 100% backwards compatibility
- **MUST NOT** modify existing annotation parameters
- **MUST NOT** change existing method signatures
- **MUST NOT** deprecate any existing APIs
- **MUST NOT** require changes to existing code

#### TR-2.2: Generated Code Compatibility

- **MUST** generate identical code for entries without results
- **MUST** maintain existing entry class hierarchies
- **MUST** preserve existing factory method signatures
- **MUST** not break binary compatibility

### TR-3: Performance

#### TR-3.1: Compilation Performance

- **MUST** add < 5% to KSP processing time
- **MUST** not significantly increase generated code size
- **SHOULD** optimize callback storage data structures
- **SHOULD** avoid reflection where possible

#### TR-3.2: Runtime Performance

- **MUST** add minimal overhead to navigation operations
- **MUST** use efficient callback storage (O(1) lookup)
- **MUST** clean up callbacks promptly to avoid memory leaks
- **SHOULD** minimize allocations in hot paths

### TR-4: Testing

#### TR-4.1: Compilation Tests

- **MUST** test all annotation combinations with results
- **MUST** verify generated code structure and interfaces
- **MUST** validate type safety in generated code
- **MUST** test both internal and external entries

#### TR-4.2: Integration Tests

- **MUST** test result delivery end-to-end
- **MUST** test cancellation handling
- **MUST** test multi-module result navigation
- **MUST** test both Fragment and Composable types
- **MUST** test configuration change scenarios

#### TR-4.3: Sample App Demonstrations

- **MUST** add photo picker example to sample app
- **MUST** add confirmation dialog example
- **MUST** demonstrate multi-module results
- **MUST** show both implementation types

## API Design

### Annotation API

```kotlin
// Add result parameter to @UiEntry
@UiEntry(
    type = ImplementationType.Composable,
    args = PhotoPickerArgs::class,
    result = PhotoPickerResult::class  // NEW
)
@Composable
fun PhotoPickerScreen(
    args: PhotoPickerArgs,
    navigator: NavigationController
)

// Add result parameter to @UiExternalEntry
@UiExternalEntry(
    type = ImplementationType.Fragment,
    destination = PhotoPickerDestination::class,
    result = PhotoPickerResult::class  // NEW
)
@Composable
fun PhotoPickerScreen(
    args: PhotoPickerArgs,
    navigator: NavigationController
)

// NoResult marker (default)
@Parcelize
object NoResult : Parcelable
```

### NavigationController API

```kotlin
abstract class NavigationController {

    // NEW: Navigate for result
    abstract fun <TResult : Parcelable> navigateForResult(
        entry: Entry,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
        callback: (TResult?) -> Unit
    )

    // NEW: Navigate to external destination for result
    abstract fun <TResult : Parcelable> navigateForResult(
        externalDestination: ExternalDestination,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
        callback: (TResult?) -> Unit
    )

    // NEW: Set result and go back
    abstract fun <TResult : Parcelable> setResultAndNavigateBack(result: TResult)

    // NEW: Cancel and go back (delivers null)
    abstract fun cancelResultAndNavigateBack()
}
```

### Generated Code Interface

```kotlin
// NEW: Interface for entries with results
interface ResultEntry<TArgs : Parcelable, TResult : Parcelable> : Entry {
    val resultType: Class<TResult>
}

// NEW: Interface for destinations with results
interface DestinationWithResult<TResult : Parcelable> {
    val resultType: Class<TResult>
}
```

### Usage Examples

#### Example 1: Photo Picker (Internal Entry)

```kotlin
// Define result type
@Parcelize
data class PhotoPickerResult(val photoUri: String) : Parcelable

// Define screen with result
@UiEntry(
    type = ImplementationType.Composable,
    result = PhotoPickerResult::class
)
@Composable
fun PhotoPickerScreen(navigator: NavigationController) {
    Button(onClick = {
        navigator.setResultAndNavigateBack(
            PhotoPickerResult("content://photo/123")
        )
    }) {
        Text("Select Photo")
    }
}

// Call from another screen
Button(onClick = {
    navigator.navigateForResult(
        entry = PhotoPickerScreenEntry.newInstance()
    ) { result: PhotoPickerResult? ->
        result?.let { updatePhoto(it.photoUri) }
    }
}) {
    Text("Change Photo")
}
```

#### Example 2: Confirmation Dialog (Fragment Type)

```kotlin
@Parcelize
data class ConfirmationResult(val confirmed: Boolean) : Parcelable

@UiEntry(
    type = ImplementationType.Fragment,
    args = ConfirmationArgs::class,
    result = ConfirmationResult::class
)
@Composable
fun ConfirmationDialog(
    args: ConfirmationArgs,
    navigator: NavigationController
) {
    AlertDialog(
        onDismissRequest = { navigator.cancelResultAndNavigateBack() },
        confirmButton = {
            Button(onClick = {
                navigator.setResultAndNavigateBack(
                    ConfirmationResult(confirmed = true)
                )
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = {
                navigator.setResultAndNavigateBack(
                    ConfirmationResult(confirmed = false)
                )
            }) {
                Text("Cancel")
            }
        },
        text = { Text(args.message) }
    )
}
```

#### Example 3: Multi-Module Result

```kotlin
// navigation module: Define destination
data class PhotoPickerDestination(
    override val args: NoArgs
) : DestinationWithNoArgs,
    DestinationWithResult<PhotoPickerResult>

// feature-photo module: Implement screen
@UiExternalEntry(
    type = ImplementationType.Composable,
    destination = PhotoPickerDestination::class,
    result = PhotoPickerResult::class
)
@Composable
fun PhotoPickerScreen(navigator: NavigationController) { ... }

// feature-profile module: Call with result
navigator.navigateForResult(PhotoPickerDestination) { result: PhotoPickerResult? ->
    result?.let { updateProfilePhoto(it.photoUri) }
}
```

## Questions

### Architecture & Design

- [ ] Should we support result delivery after process death for Composable entries?

  - [ ] **Recommended**: Start without support, document limitation
    - Most use cases don't require process death recovery
    - Fragment entries already support this via Fragment Result API
    - Can add SavedStateHandle support in future version if needed
    - Reduces initial implementation complexity
    - Risk: Some apps may need this for critical flows
  - [ ] Alternative: Implement SavedStateHandle support from the start
    - Provides consistent behavior across implementation types
    - More complex implementation and testing
    - Risk: Delayed feature delivery, increased maintenance burden

- [ ] How should we handle nested navigate-for-result (Screen A → Screen B for result → Screen C for result)?

  - [ ] **Recommended**: Support naturally with callback stacking
    - Each screen maintains its own result callback
    - Natural behavior developers would expect
    - No special handling required
    - Risk: Potential confusion with multiple callbacks in flight
  - [ ] Alternative: Disallow nested results with runtime error
    - Simpler mental model
    - Prevents potential issues
    - Risk: Limits legitimate use cases

- [ ] Should we add runtime type validation in addition to compile-time checks?
  - [ ] **Recommended**: Compile-time only (current proposal)
    - Type safety guaranteed by KSP code generation
    - Better performance (no runtime overhead)
    - Sufficient for correct usage
    - Risk: No safety net if generated code has bugs
  - [ ] Alternative: Add runtime type checking with exceptions
    - Additional safety layer
    - Helpful for debugging
    - Risk: Performance overhead, false sense of security

### Multi-Module Support

- [ ] Should destinations be required to implement DestinationWithResult interface?
  - [ ] **Recommended**: Yes, require interface implementation
    - Compile-time validation of result types
    - Clear declaration of destination capabilities
    - Type-safe across module boundaries
    - Risk: Slightly more boilerplate in destination definitions
  - [ ] Alternative: Use runtime result type registration
    - Less boilerplate
    - No interface requirements
    - Risk: Runtime errors, not type-safe, harder to discover

### API Ergonomics

- [ ] Should we provide a way to differentiate explicit cancellation from null results?

  - [ ] **Recommended**: Use nullable results (null = cancelled)
    - Simple, consistent with Android Activity Result API
    - One callback to implement
    - Common pattern developers understand
    - Risk: Ambiguous if result type can legitimately be "no data"
  - [ ] Alternative: Use sealed class Result<T> (Success/Cancelled)
    - Explicit success/failure states
    - No ambiguity
    - Risk: Verbose, more boilerplate, unfamiliar pattern

- [ ] Should navigateForResult be a separate method or overload of navigateTo?
  - [ ] **Recommended**: Separate method (current proposal)
    - Clear intent when reading code
    - Easier to discover in IDE
    - No confusion with optional callback parameter
    - Risk: One more method to learn
  - [ ] Alternative: Add optional callback parameter to navigateTo
    - Single navigation method
    - Fewer methods to learn
    - Risk: Ambiguous when callback is omitted, overloading complexity

### Testing & Validation

- [ ] How should we handle configuration changes for Composable results?
  - [ ] **Recommended**: Require ViewModel-scoped NavigationController
    - Survives configuration changes
    - Consistent with modern Android architecture
    - Document this requirement clearly
    - Risk: Developers might not follow best practice
  - [ ] Alternative: Implement automatic callback restoration
    - More robust
    - Works regardless of NavigationController scope
    - Risk: Complex implementation, potential memory leaks

### Future Enhancements

- [ ] Should we plan for Activity Result Contract integration?

  - [ ] **Recommended**: Add in future version based on demand
    - Initial release focuses on core navigate-for-result
    - Developers can call Activity Result API directly
    - Evaluate real-world use cases before designing API
    - Risk: Developers create their own wrappers
  - [ ] Alternative: Include in initial release
    - Complete solution from day one
    - Consistent API for all result scenarios
    - Risk: Delayed release, increased complexity

- [ ] Should we support streaming results (Flow-based)?
  - [ ] **Recommended**: Not in initial release
    - Use SharedViewModel or event bus for complex scenarios
    - Collect real-world use cases first
    - Simpler initial implementation
    - Risk: Developers may create workarounds
  - [ ] Alternative: Add resultFlow variant
    - Supports progress updates, multi-select
    - More complete solution
    - Risk: Significant complexity increase

## Implementation Plan

### Phase 1: Annotations & Runtime Foundation (3 days)

- [ ] Add `result` parameter to `@UiEntry` annotation with default `NoResult::class`
- [ ] Add `result` parameter to `@UiExternalEntry` annotation with default `NoResult::class`
- [ ] Create `NoResult` marker class in `nibel-annotations` module
- [ ] Update annotation KDoc with result examples
- [ ] Create `ResultEntry<TArgs, TResult>` interface in `nibel-runtime`
- [ ] Create `DestinationWithResult<TResult>` interface in `nibel-runtime`
- [ ] **Testing**: Verify annotations compile, default parameter works

### Phase 2: NavigationController API (5 days)

- [ ] Add `navigateForResult(entry, callback)` method to NavigationController
- [ ] Add `navigateForResult(externalDestination, callback)` method to NavigationController
- [ ] Add `setResultAndNavigateBack(result)` method to NavigationController
- [ ] Add `cancelResultAndNavigateBack()` method to NavigationController
- [ ] Implement callback storage mechanism in NibelNavigationController
- [ ] Implement result delivery for Fragment entries using Fragment Result API
- [ ] Implement result delivery for Composable entries using callback storage
- [ ] Add request key generation and management
- [ ] Implement callback cleanup after delivery/cancellation
- [ ] **Testing**: Unit tests for result delivery mechanism, mock navigation scenarios

### Phase 3: Compiler Code Generation (7 days)

- [ ] Update `EntryMetadata` to include `resultQualifiedName` field
- [ ] Modify `EntryGeneratingVisitor` to extract result type from annotations
- [ ] Add validation that result types are Parcelable
- [ ] Update `ComposableGenerator` to generate ResultEntry implementations
- [ ] Add `resultType` property to generated Composable entries with results
- [ ] Update companion object factory methods to return ResultEntry type
- [ ] Update `FragmentGenerator` to generate ResultEntry implementations for Fragments
- [ ] Add `resultType` property to generated Fragment entries with results
- [ ] Modify `EntryFactoryProviderGenerator` for external entries with results
- [ ] Generate destination extensions for result type access
- [ ] **Testing**: Compilation tests for all scenarios, verify generated code structure

### Phase 4: Integration Testing & Sample App (7 days)

- [ ] Create `ResultEntryCompileTest.kt` with all annotation combinations
- [ ] Verify generated code implements ResultEntry interface correctly
- [ ] Create `ResultNavigationTest.kt` for end-to-end testing
- [ ] Test result delivery for Composable entries
- [ ] Test result delivery for Fragment entries
- [ ] Test cancellation handling (null results)
- [ ] Test multi-module result navigation
- [ ] Test configuration change scenarios
- [ ] Add PhotoPickerScreen example to sample app (feature-A)
- [ ] Add ProfileScreen caller example to sample app (feature-B)
- [ ] Add ConfirmationDialog example to sample app
- [ ] Run full regression test suite (`./gradlew test`)
- [ ] Run full build verification (`./gradlew build`)
- [ ] Verify sample app builds and runs correctly
- [ ] **Testing**: All tests pass, sample app demonstrates all scenarios

### Phase 5: Documentation & Release Preparation (3 days)

- [ ] Update main README.md with navigate-for-result section
- [ ] Add comprehensive KDoc to all new public APIs
- [ ] Create usage examples document with common patterns
- [ ] Write migration guide for adding results to existing screens
- [ ] Document lifecycle considerations and limitations
- [ ] Document process death behavior differences (Fragment vs Composable)
- [ ] Update CHANGELOG with new feature
- [ ] Create release notes
- [ ] Final code review and polish
- [ ] Run pre-commit hooks and linting (`./gradlew lintFix`, `pre-commit run --all-files`)
- [ ] **Testing**: Documentation review, final QA pass

### Estimated Timeline

- **Total Duration**: 25 days (~5 weeks)
- **Phase 1**: Days 1-3
- **Phase 2**: Days 3-8
- **Phase 3**: Days 8-15
- **Phase 4**: Days 15-22
- **Phase 5**: Days 22-25

### Milestones

- **M1** (Day 3): Annotations and interfaces complete
- **M2** (Day 8): NavigationController API implementation complete
- **M3** (Day 15): Code generation complete and tested
- **M4** (Day 22): Sample app and integration tests complete
- **M5** (Day 25): Documentation complete, ready for release

## Testing Strategy

### Compilation Tests

**Location**: `tests/src/test/kotlin/nibel/tests/codegen/ResultEntryCompileTest.kt`

Test scenarios:

- [ ] Internal entry (Composable) with result
- [ ] Internal entry (Fragment) with result
- [ ] External entry (Composable) with result
- [ ] External entry (Fragment) with result
- [ ] Entry with result but no args
- [ ] Entry with both args and result
- [ ] Verify generated code implements ResultEntry interface
- [ ] Verify resultType property is correct
- [ ] Verify companion object signatures

### Integration Tests

**Location**: `tests/src/test/kotlin/nibel/tests/integration/ResultNavigationTest.kt`

Test scenarios:

- [ ] Navigate for result delivers result correctly
- [ ] Navigate for result handles cancellation (null)
- [ ] Multi-module result navigation works
- [ ] Fragment implementation type delivers results
- [ ] Composable implementation type delivers results
- [ ] Configuration changes preserve callbacks
- [ ] Nested navigate-for-result works
- [ ] Multiple results in sequence work correctly

### Sample App Examples

**Location**: `sample/` modules

Examples to implement:

- [ ] PhotoPickerScreen (feature-A) - demonstrates Composable result entry
- [ ] ProfileScreen (feature-B) - demonstrates calling with results
- [ ] ConfirmationDialog - demonstrates Fragment result entry
- [ ] Multi-module flow - demonstrates external entries with results

### Test Coverage Goals

- **Unit tests**: 90%+ coverage for new code
- **Compilation tests**: 100% coverage of annotation combinations
- **Integration tests**: All navigation flows with results covered
- **Regression tests**: All existing tests continue to pass

## Release Plan

### Version

**Target**: Nibel 2.1.0 (minor version, new feature)

### Pre-Release Checklist

- [ ] All tests passing (`./gradlew test`)
- [ ] Full build successful (`./gradlew build`)
- [ ] Sample app builds and runs
- [ ] Documentation complete and reviewed
- [ ] CHANGELOG updated
- [ ] Migration guide written
- [ ] No breaking changes confirmed

### Release Phases

**Phase 1: Internal Alpha (Week 1)**

- Release to internal team for testing
- Gather feedback on API ergonomics
- Test in real-world scenarios
- Fix critical issues

**Phase 2: Beta Release (Week 2)**

- Release to select early adopters
- Gather broader feedback
- Monitor for edge cases
- Refine documentation based on questions

**Phase 3: General Availability (Week 3)**

- Release to all users
- Announce in release notes
- Update website documentation
- Monitor adoption and issues

### Rollback Plan

- Feature is opt-in, no breaking changes
- If critical issues found, can:
  1. Document workarounds
  2. Fix in patch release (2.1.1)
  3. Deprecate in future major version if necessary
- Zero impact on users not adopting the feature

### Communication Plan

- **Announcement**: Blog post with examples and use cases
- **Documentation**: Update README and add migration guide
- **Sample Code**: Add examples to sample app
- **Support**: Monitor GitHub issues for questions

### Success Criteria for Release

- [ ] Zero breaking changes confirmed
- [ ] All tests passing
- [ ] Documentation complete
- [ ] Sample app demonstrates all scenarios
- [ ] Performance benchmarks meet targets
- [ ] No critical issues in beta testing

---

## Appendix: Technical Details

### Generated Code Example

**Input annotation**:

```kotlin
@UiEntry(
    type = ImplementationType.Composable,
    args = PhotoPickerArgs::class,
    result = PhotoPickerResult::class
)
@Composable
fun PhotoPickerScreen(args: PhotoPickerArgs, navigator: NavigationController)
```

**Generated entry class**:

```kotlin
@Parcelize
class PhotoPickerScreenEntry(
    override val args: PhotoPickerArgs,
    override val name: String,
    internal var requestKey: String? = null,
) : ComposableEntry<PhotoPickerArgs>(args, name),
    ResultEntry<PhotoPickerArgs, PhotoPickerResult> {

    override val resultType: Class<PhotoPickerResult>
        get() = PhotoPickerResult::class.java

    @Composable
    override fun ComposableContent() {
        PhotoPickerScreen(
            args = LocalArgs.current as PhotoPickerArgs,
            navigator = LocalNavigationController.current
        )
    }

    companion object {
        fun newInstance(args: PhotoPickerArgs): ResultEntry<PhotoPickerArgs, PhotoPickerResult> {
            return PhotoPickerScreenEntry(
                args = args,
                name = buildRouteName(PhotoPickerScreenEntry::class.qualifiedName!!, args),
            )
        }
    }
}
```

### Module Changes Summary

**nibel-annotations**:

- Add `result` parameter to `@UiEntry`
- Add `result` parameter to `@UiExternalEntry`
- Add `NoResult` marker class

**nibel-runtime**:

- Add `ResultEntry<TArgs, TResult>` interface
- Add `DestinationWithResult<TResult>` interface
- Add `navigateForResult()` methods to NavigationController
- Add `setResultAndNavigateBack()` method
- Add `cancelResultAndNavigateBack()` method
- Implement result delivery in NibelNavigationController

**nibel-compiler**:

- Update EntryMetadata with result type
- Update EntryGeneratingVisitor to extract results
- Update ComposableGenerator for ResultEntry
- Update FragmentGenerator for ResultEntry
- Update EntryFactoryProviderGenerator for external results

**tests**:

- Add ResultEntryCompileTest.kt
- Add ResultNavigationTest.kt
- Add regression tests

**sample**:

- Add PhotoPickerScreen example
- Add ProfileScreen with result handling
- Add ConfirmationDialog example

### Backwards Compatibility Guarantee

This feature guarantees 100% backwards compatibility:

1. **No breaking changes**: All existing code compiles without modification
2. **Opt-in feature**: Default behavior is unchanged (result = NoResult::class)
3. **Generated code**: Entries without results generate identical code
4. **API additions only**: No existing APIs modified or deprecated
5. **Binary compatibility**: No changes to existing class hierarchies

### Risk Mitigation

**Risk**: Complexity of result delivery across Fragment/Composable boundaries
**Mitigation**: Use established Android APIs (Fragment Result API), comprehensive testing

**Risk**: Memory leaks from stored callbacks
**Mitigation**: Automatic cleanup after delivery, lifecycle-aware storage, leak detection in tests

**Risk**: Type safety edge cases
**Mitigation**: Compile-time validation via KSP, runtime type information in generated classes

**Risk**: Process death handling inconsistency
**Mitigation**: Clear documentation, use Fragment Result API for critical flows

**Risk**: Developer confusion about API usage
**Mitigation**: Comprehensive examples, clear documentation, IDE-friendly API design
