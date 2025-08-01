# Nibel Compilation Tests

This directory contains compilation tests for the Nibel navigation library's KSP (Kotlin Symbol Processing) annotation processor. These tests verify that the `@UiEntry` and `@UiExternalEntry` annotations generate correct code for various navigation scenarios.

## Test Philosophy

These are **compilation tests** - they pass if the annotated Composable functions compile successfully and generate the expected Entry classes without compilation errors. The actual functionality is tested elsewhere; these tests focus on ensuring the code generation works correctly.

## Test Coverage

### Core Entry Types

| Test File                          | Purpose                          | Coverage                                        |
| ---------------------------------- | -------------------------------- | ----------------------------------------------- |
| `InternalEntryCompileTest.kt`      | Basic internal entries           | Fragment/Composable entries with/without args   |
| `ExternalEntryCompileTest.kt`      | Basic external entries           | Cross-module entries with destinations          |
| `InternalEntryParamCompileTest.kt` | Internal entries with parameters | Parameter injection for internal navigation     |
| `ExternalEntryParamCompileTest.kt` | External entries with parameters | Parameter injection for cross-module navigation |
| `ResultEntryCompileTest.kt`        | Result-based entries             | Result navigation with type safety              |

### Entry Variations Tested

- **Implementation Types**: `Fragment` vs `Composable`
- **Arguments**: With and without navigation arguments
- **Parameters**: Additional Composable function parameters (navigator, type, defaults)
- **Results**: Return values from navigation targets (Activity Result API pattern)
- **Scope**: Internal (intra-module) vs External (cross-module) entries

## Generated Code Expectations

### Internal Entries (`@UiEntry`)

- Generate `{FunctionName}Entry` classes
- Extend appropriate base classes (`ComposableEntry<T>` or `FragmentEntry`)
- Include companion object factory methods
- Handle parameter injection in `ComposableContent()`

### External Entries (`@UiExternalEntry`)

- Generate `{FunctionName}Entry` classes with factory companions
- Implement `ComposableEntryFactory<DestinationType>` or `FragmentEntryFactory<DestinationType>`
- Support cross-module navigation through destination classes
- Register entry factories for runtime lookup

### Result Entries (with `result` parameter)

- Implement `ResultEntry<ResultType>` interface
- Include `resultType: Class<ResultType>` property
- Generate type-safe factory methods returning `ResultEntry<ResultType>`
- Support both internal and external result navigation

## Test Data Classes

- `TestArgs`: Sample navigation arguments class
- `TestResult`: Sample result data class
- Various destination classes for external entry testing

## Key Features Verified

1. **Code Generation**: All annotation combinations generate valid Kotlin code
2. **Type Safety**: Generated factories return appropriate types (no unsafe casts needed)
3. **Parameter Injection**: Navigation controller, implementation type, and custom parameters
4. **Cross-Module Support**: External entries work across module boundaries
5. **Result Navigation**: Type-safe result handling with proper return types

## Running the Tests

```bash
# Compile all tests (this runs the annotation processor)
./gradlew :tests:compileDebugKotlin

# Run actual unit tests (functional verification)
./gradlew :tests:test
```

## Adding New Tests

When adding new compilation tests:

1. Create test functions with appropriate `@UiEntry` or `@UiExternalEntry` annotations
2. Use descriptive function names that indicate the scenario being tested
3. Add comprehensive documentation explaining what should be generated
4. Create necessary destination classes for external entry tests
5. Ensure tests cover edge cases and new features

## Troubleshooting

If compilation tests fail:

1. Check the KSP annotation processor logs
2. Verify required dependencies are available
3. Ensure destination classes are properly defined for external entries
4. Check that test data classes (TestArgs, TestResult) are accessible
5. Verify the nibel-compiler module builds successfully

These tests serve as both verification of the annotation processor and documentation of supported navigation patterns.
