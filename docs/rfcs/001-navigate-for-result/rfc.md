# RFC 001: Navigate-For-Result Feature

|                  |                                                                    |
| ---------------- | ------------------------------------------------------------------ |
| **RFC Number**   | 001                                                                |
| **Title**        | Navigate-For-Result: Type-Safe Result Handling in Nibel Navigation |
| **Status**       | Draft                                                              |
| **Authors**      | Nibel Team                                                         |
| **Created**      | 2025-10-28                                                         |
| **Last Updated** | 2025-10-28                                                         |
| **Reviewers**    | TBD                                                                |

## Table of Contents

- [Background](#background)
- [Motivation](#motivation)
- [Proposed Solution](#proposed-solution)
- [API Design](#api-design)
- [Implementation Details](#implementation-details)
- [Generated Code Examples](#generated-code-examples)
- [Backwards Compatibility](#backwards-compatibility)
- [Testing Strategy](#testing-strategy)
- [Implementation Plan](#implementation-plan)
- [Alternatives Considered](#alternatives-considered)
- [Open Questions](#open-questions)
- [Security Implications](#security-implications)
- [References](#references)

---

## Background

Nibel is a type-safe navigation library that enables seamless integration of Jetpack Compose in fragment-based Android apps. It currently supports three navigation scenarios:

- **fragment → compose**: Navigate from existing fragments to Compose screens
- **compose → compose**: Navigate between Compose screens
- **compose → fragment**: Navigate from Compose screens back to fragments

The library uses annotation-driven code generation (via KSP) to create type-safe navigation entry points with compile-time validation.

### Current State

Currently, Nibel supports:

- Type-safe navigation with `NavigationController`
- Optional arguments via `args: KClass<out Parcelable>` parameter
- Multi-module navigation via destinations
- Both Fragment and Composable implementation types

**What's Missing**: There is no built-in mechanism for screens to return results to their callers. This is a common pattern in Android (e.g., Activity Result API, Fragment Result API) and is essential for many user flows.

---

## Motivation

### Problem Statement

Modern Android apps frequently need screens to return data to their callers:

1. **User Selection Flows**: Photo picker, contact selector, location picker
2. **Form Confirmation**: User completes a form and returns the submitted data
3. **Decision Dialogs**: User makes a choice (approve/reject, select option)
4. **Data Creation**: Create a new item and return its ID
5. **Multi-step Workflows**: Gather information across multiple screens

Without native result support, developers must resort to workarounds:

- Shared ViewModels (couples modules, breaks encapsulation)
- Event buses (lacks type safety, hard to track)
- Navigation arguments in reverse direction (complex, error-prone)
- Direct callback passing (doesn't survive process death)

### Goals

1. **Type Safety**: Compile-time validation that result types match between caller and callee
2. **Backwards Compatible**: Existing code works without modifications
3. **Multi-Module Support**: Works across feature modules via destinations
4. **Android Integration**: Leverages Activity Result API pattern
5. **Consistent API**: Follows Nibel's existing design patterns
6. **Opt-in**: Feature is optional, not required for simple navigation

### Success Criteria

- Zero breaking changes to existing code
- Works for both `@UiEntry` and `@UiExternalEntry`
- Supports all implementation types (Fragment/Composable)
- Type-safe result delivery with compile-time checks
- Comprehensive test coverage
- Clear migration path and documentation

---

## Proposed Solution

### High-Level Design

Extend the existing annotation system with an optional `result` parameter, similar to how `args` works:

```kotlin
@UiEntry(
    type = ImplementationType.Composable,
    args = PhotoPickerArgs::class,
    result = PhotoPickerResult::class  // NEW
)
@Composable
fun PhotoPickerScreen(
    args: PhotoPickerArgs,
    navigator: NavigationController
) {
    // Implementation
    navigator.setResultAndNavigateBack(PhotoPickerResult(selectedPhoto))
}
```

### Key Components

1. **Annotation Extension**: Add optional `result` parameter to both `@UiEntry` and `@UiExternalEntry`
2. **NavigationController Methods**: Add result-aware navigation and result-setting methods
3. **Result Interfaces**: Generate result-aware entry interfaces for type safety
4. **Result Delivery Mechanism**: Handle result delivery across Fragment/Composable boundaries
5. **Multi-Module Support**: Extend destinations to carry result type information

### Design Principles

- **Opt-in**: Default is `NoResult::class`, no result handling
- **Type Safety**: Result type in annotation must match callback type
- **Nullable Results**: Callback receives `T?` where `null` indicates cancellation
- **Symmetric API**: Clear methods for setting and canceling results
- **Consistent**: Follows existing Nibel patterns (similar to args handling)

---

## API Design

### 1. Annotation Changes

#### @UiEntry (Internal Navigation)

```kotlin
@Target(FUNCTION)
@MustBeDocumented
annotation class UiEntry(
    val type: ImplementationType,
    val args: KClass<out Parcelable> = NoArgs::class,
    val result: KClass<out Parcelable> = NoResult::class  // NEW
)
```

#### @UiExternalEntry (Multi-Module Navigation)

```kotlin
@Target(FUNCTION)
@MustBeDocumented
annotation class UiExternalEntry(
    val type: ImplementationType,
    val destination: KClass<out ExternalDestination>,
    val result: KClass<out Parcelable> = NoResult::class  // NEW
)
```

#### NoResult Marker Class

```kotlin
package nibel.runtime

/**
 * Marker class indicating that a screen does not return a result.
 * This is the default value for the `result` parameter in navigation annotations.
 */
@Parcelize
object NoResult : Parcelable
```

### 2. NavigationController Extensions

```kotlin
abstract class NavigationController(
    val fragmentSpec: FragmentSpec<*> = Nibel.fragmentSpec,
    val composeSpec: ComposeSpec<*> = Nibel.composeSpec,
) {

    // Existing methods...
    abstract fun navigateBack()
    abstract fun navigateTo(entry: Entry, ...)
    abstract fun navigateTo(externalDestination: ExternalDestination, ...)

    // NEW: Navigate for result
    /**
     * Navigate to a screen and receive a result when it completes.
     *
     * @param entry The entry to navigate to (must be a ResultEntry)
     * @param callback Callback invoked with result (null if cancelled)
     */
    abstract fun <TResult : Parcelable> navigateForResult(
        entry: Entry,
        callback: (TResult?) -> Unit
    )

    // NEW: Set result and navigate back
    /**
     * Set a result and navigate back to the caller.
     * The result will be delivered to the callback provided in navigateForResult.
     *
     * @param result The result to return to the caller
     */
    abstract fun <TResult : Parcelable> setResultAndNavigateBack(result: TResult)

    // NEW: Cancel and navigate back
    /**
     * Navigate back without setting a result (equivalent to user pressing back).
     * The callback will receive null.
     */
    abstract fun cancelResultAndNavigateBack()
}
```

### 3. Usage Examples

#### Example 1: Internal Entry with Result (Composable)

```kotlin
// Define result type
@Parcelize
data class PhotoPickerResult(
    val photoUri: String
) : Parcelable

// Define args type
@Parcelize
data class PhotoPickerArgs(
    val allowMultiple: Boolean = false
) : Parcelable

// Define screen with result
@UiEntry(
    type = ImplementationType.Composable,
    args = PhotoPickerArgs::class,
    result = PhotoPickerResult::class
)
@Composable
fun PhotoPickerScreen(
    args: PhotoPickerArgs,
    navigator: NavigationController
) {
    // UI implementation
    Button(onClick = {
        val result = PhotoPickerResult("content://photo/123")
        navigator.setResultAndNavigateBack(result)
    }) {
        Text("Select Photo")
    }

    Button(onClick = {
        navigator.cancelResultAndNavigateBack()
    }) {
        Text("Cancel")
    }
}

// Caller
@UiEntry(type = ImplementationType.Composable)
@Composable
fun ProfileScreen(navigator: NavigationController) {
    Button(onClick = {
        val args = PhotoPickerArgs(allowMultiple = false)
        navigator.navigateForResult(
            entry = PhotoPickerScreenEntry.newInstance(args)
        ) { result: PhotoPickerResult? ->
            if (result != null) {
                // User selected a photo
                updateProfilePhoto(result.photoUri)
            } else {
                // User cancelled
            }
        }
    }) {
        Text("Change Photo")
    }
}
```

#### Example 2: External Entry with Result (Multi-Module)

```kotlin
// navigation module: Define destination with result
data class PhotoPickerDestination(
    override val args: PhotoPickerArgs
) : DestinationWithArgs<PhotoPickerArgs>,
    DestinationWithResult<PhotoPickerResult>  // NEW interface

// feature-photo module: Implement screen
@UiExternalEntry(
    type = ImplementationType.Fragment,
    destination = PhotoPickerDestination::class,
    result = PhotoPickerResult::class
)
@Composable
fun PhotoPickerScreen(
    args: PhotoPickerArgs,
    navigator: NavigationController
) {
    // Implementation...
    navigator.setResultAndNavigateBack(PhotoPickerResult(selectedUri))
}

// feature-profile module: Call with result
@UiExternalEntry(
    type = ImplementationType.Composable,
    destination = ProfileScreenDestination::class
)
@Composable
fun ProfileScreen(navigator: NavigationController) {
    Button(onClick = {
        navigator.navigateForResult(
            externalDestination = PhotoPickerDestination(
                args = PhotoPickerArgs(allowMultiple = false)
            )
        ) { result: PhotoPickerResult? ->
            result?.let { updateProfilePhoto(it.photoUri) }
        }
    }) {
        Text("Change Photo")
    }
}
```

#### Example 3: Fragment Implementation Type with Result

```kotlin
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

#### Example 4: No Args, Only Result

```kotlin
@Parcelize
data class UserSelectionResult(
    val userId: String,
    val userName: String
) : Parcelable

@UiEntry(
    type = ImplementationType.Composable,
    result = UserSelectionResult::class
)
@Composable
fun UserSelectionScreen(navigator: NavigationController) {
    LazyColumn {
        items(users) { user ->
            ListItem(
                text = { Text(user.name) },
                modifier = Modifier.clickable {
                    navigator.setResultAndNavigateBack(
                        UserSelectionResult(user.id, user.name)
                    )
                }
            )
        }
    }
}

// Caller
navigator.navigateForResult(
    entry = UserSelectionScreenEntry.newInstance()
) { result: UserSelectionResult? ->
    result?.let { selectedUser ->
        updateSelectedUser(selectedUser.userId)
    }
}
```

---

## Implementation Details

### 1. Annotations Module (nibel-annotations)

#### Changes to Existing Files

**UiEntry.kt** - Add result parameter:

```kotlin
annotation class UiEntry(
    val type: ImplementationType,
    val args: KClass<out Parcelable> = NoArgs::class,
    val result: KClass<out Parcelable> = NoResult::class  // ADD THIS
)
```

**UiExternalEntry.kt** - Add result parameter:

```kotlin
annotation class UiExternalEntry(
    val type: ImplementationType,
    val destination: KClass<out ExternalDestination>,
    val result: KClass<out Parcelable> = NoResult::class  // ADD THIS
)
```

#### New Files

**NoResult.kt**:

```kotlin
package nibel.annotations

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Marker indicating a screen does not return a result.
 * This is the default value for the `result` parameter.
 *
 * See [UiEntry], [UiExternalEntry].
 */
@Parcelize
object NoResult : Parcelable
```

### 2. Runtime Module (nibel-runtime)

#### New Interfaces

**ResultEntry.kt**:

```kotlin
package nibel.runtime

/**
 * Base interface for entries that return a result.
 * Generated entry classes will implement this when result parameter is specified.
 */
interface ResultEntry<TArgs : Parcelable, TResult : Parcelable> : Entry {
    /**
     * Result type class for runtime type checking
     */
    val resultType: Class<TResult>
}
```

**DestinationWithResult.kt**:

```kotlin
package nibel.runtime

/**
 * Interface for destinations that return a result.
 * Used in multi-module navigation with results.
 */
interface DestinationWithResult<TResult : Parcelable> {
    /**
     * Result type class for runtime resolution
     */
    val resultType: Class<TResult>
        get() = TODO("Compiler will implement this in destination classes")
}
```

#### NavigationController Extensions

**NavigationController.kt** - Add new methods:

````kotlin
abstract class NavigationController(
    val fragmentSpec: FragmentSpec<*> = Nibel.fragmentSpec,
    val composeSpec: ComposeSpec<*> = Nibel.composeSpec,
) {

    // Existing methods remain unchanged...

    /**
     * Navigate to a screen and receive a typed result when it completes.
     *
     * The callback will be invoked with:
     * - Non-null result if callee calls setResultAndNavigateBack
     * - Null if callee calls cancelResultAndNavigateBack or user presses back
     *
     * Example:
     * ```
     * navigator.navigateForResult(PhotoPickerEntry.newInstance(args)) { result: PhotoResult? ->
     *     result?.let { handlePhoto(it.uri) }
     * }
     * ```
     */
    abstract fun <TResult : Parcelable> navigateForResult(
        entry: Entry,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
        callback: (TResult?) -> Unit
    )

    /**
     * Navigate to an external destination and receive a typed result.
     * Used for multi-module navigation with results.
     */
    abstract fun <TResult : Parcelable> navigateForResult(
        externalDestination: ExternalDestination,
        fragmentSpec: FragmentSpec<*> = this.fragmentSpec,
        composeSpec: ComposeSpec<*> = this.composeSpec,
        callback: (TResult?) -> Unit
    )

    /**
     * Set a result and navigate back to the previous screen.
     * The result will be delivered to the callback in navigateForResult.
     *
     * Can only be called from screens annotated with a result parameter.
     */
    abstract fun <TResult : Parcelable> setResultAndNavigateBack(result: TResult)

    /**
     * Navigate back without setting a result (equivalent to back press).
     * The navigateForResult callback will receive null.
     *
     * Can be called from any screen.
     */
    abstract fun cancelResultAndNavigateBack()
}
````

#### Implementation in NibelNavigationController

**NibelNavigationController.kt** - Implement result delivery:

```kotlin
class NibelNavigationController(
    private val navController: NavController,
    fragmentSpec: FragmentSpec<*>,
    composeSpec: ComposeSpec<*>,
) : NavigationController(fragmentSpec, composeSpec) {

    // Result callback storage
    private val resultCallbacks = mutableMapOf<String, (Any?) -> Unit>()

    override fun <TResult : Parcelable> navigateForResult(
        entry: Entry,
        fragmentSpec: FragmentSpec<*>,
        composeSpec: ComposeSpec<*>,
        callback: (TResult?) -> Unit
    ) {
        // Generate unique key for this navigation
        val requestKey = UUID.randomUUID().toString()

        // Store callback
        resultCallbacks[requestKey] = callback as (Any?) -> Unit

        // Navigate with request key
        when (entry) {
            is FragmentEntry -> {
                // Use Fragment Result API
                navController.currentBackStackEntry?.savedStateHandle?.let { handle ->
                    handle.getLiveData<TResult>(requestKey).observeForever { result ->
                        callback(result)
                        handle.remove<TResult>(requestKey)
                        resultCallbacks.remove(requestKey)
                    }
                }
                navigateTo(entry, fragmentSpec, composeSpec)
            }
            is ComposableEntry<*> -> {
                // Store request key in entry metadata
                (entry as? ComposableEntryWithResult)?.requestKey = requestKey
                navigateTo(entry, fragmentSpec, composeSpec)
            }
        }
    }

    override fun <TResult : Parcelable> setResultAndNavigateBack(result: TResult) {
        // Get current entry's request key
        val requestKey = getCurrentRequestKey()

        // Deliver result via appropriate mechanism
        when (getCurrentImplementationType()) {
            ImplementationType.Fragment -> {
                // Use Fragment Result API
                navController.previousBackStackEntry?.savedStateHandle?.set(requestKey, result)
            }
            ImplementationType.Composable -> {
                // Invoke stored callback
                resultCallbacks[requestKey]?.invoke(result)
                resultCallbacks.remove(requestKey)
            }
        }

        navigateBack()
    }

    override fun cancelResultAndNavigateBack() {
        val requestKey = getCurrentRequestKey()

        // Deliver null result
        when (getCurrentImplementationType()) {
            ImplementationType.Fragment -> {
                navController.previousBackStackEntry?.savedStateHandle?.set(requestKey, null)
            }
            ImplementationType.Composable -> {
                resultCallbacks[requestKey]?.invoke(null)
                resultCallbacks.remove(requestKey)
            }
        }

        navigateBack()
    }

    private fun getCurrentRequestKey(): String {
        // Extract from current entry metadata
        return navController.currentBackStackEntry
            ?.arguments?.getString(REQUEST_KEY_ARG)
            ?: error("No request key found for current entry")
    }

    companion object {
        private const val REQUEST_KEY_ARG = "nibel_request_key"
    }
}
```

### 3. Compiler Module (nibel-compiler)

#### Metadata Changes

**EntryMetadata.kt**:

```kotlin
sealed interface EntryMetadata {
    val argsQualifiedName: String?
    val resultQualifiedName: String?  // NEW
    val parameters: Map<ParameterType, ParameterMetadata>
}

data class InternalEntryMetadata(
    override val argsQualifiedName: String?,
    override val resultQualifiedName: String?,  // NEW
    override val parameters: Map<ParameterType, ParameterMetadata>,
) : EntryMetadata

data class ExternalEntryMetadata(
    val destinationName: String,
    val destinationPackageName: String,
    val destinationQualifiedName: String,
    override val argsQualifiedName: String?,
    override val resultQualifiedName: String?,  // NEW
    override val parameters: Map<ParameterType, ParameterMetadata>,
) : EntryMetadata
```

#### Code Generation Changes

**EntryGeneratingVisitor.kt** - Extract result type from annotation:

```kotlin
private fun extractMetadata(function: KSFunctionDeclaration): EntryMetadata {
    val annotation = function.annotations.first { /* UiEntry or UiExternalEntry */ }

    // Existing args extraction...
    val argsType = annotation.arguments
        .find { it.name?.asString() == "args" }
        ?.value as? KSType

    // NEW: Extract result type
    val resultType = annotation.arguments
        .find { it.name?.asString() == "result" }
        ?.value as? KSType

    val resultQualifiedName = resultType?.declaration?.qualifiedName?.asString()
        ?.takeIf { it != "nibel.annotations.NoResult" }

    // Validate: if result is specified, it must be Parcelable
    if (resultQualifiedName != null) {
        validateParcelable(resultType, "result")
    }

    return when (type) {
        ExternalEntry -> ExternalEntryMetadata(
            // ... existing fields
            resultQualifiedName = resultQualifiedName,
        )
        InternalEntry -> InternalEntryMetadata(
            argsQualifiedName = argsQualifiedName,
            resultQualifiedName = resultQualifiedName,
        )
    }
}
```

**ComposableGenerator.kt** - Generate result-aware entry:

```kotlin
fun generate(metadata: InternalEntryMetadata) {
    val hasResult = metadata.resultQualifiedName != null
    val resultTypeName = metadata.resultQualifiedName?.let { ClassName.bestGuess(it) }

    val entryClass = TypeSpec.classBuilder(entryClassName)
        .addModifiers(KModifier.DATA)
        .addAnnotation(Parcelize::class)
        .apply {
            if (hasResult) {
                // Implement ResultEntry<TArgs, TResult>
                addSuperinterface(
                    ClassName("nibel.runtime", "ResultEntry")
                        .parameterizedBy(argsTypeName, resultTypeName!!)
                )

                // Add resultType property
                addProperty(
                    PropertySpec.builder("resultType", Class::class.asClassName().parameterizedBy(resultTypeName))
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("%T::class.java", resultTypeName)
                        .build()
                )
            } else {
                // Normal ComposableEntry
                addSuperinterface(
                    ClassName("nibel.runtime", "ComposableEntry")
                        .parameterizedBy(argsTypeName)
                )
            }
        }
        // ... rest of generation
        .build()
}
```

**FragmentGenerator.kt** - Similar changes for Fragment entries with results.

---

## Generated Code Examples

### Example 1: Internal Entry Without Result (Current Behavior - Unchanged)

**Input**:

```kotlin
@UiEntry(type = ImplementationType.Composable, args = MyArgs::class)
@Composable
fun MyScreen(args: MyArgs) { }
```

**Generated** (same as before):

```kotlin
@Parcelize
class MyScreenEntry(
    override val args: MyArgs,
    override val name: String,
) : ComposableEntry<MyArgs>(args, name) {

    @Composable
    override fun ComposableContent() {
        MyScreen(args = LocalArgs.current as MyArgs)
    }

    companion object {
        fun newInstance(args: MyArgs): ComposableEntry<MyArgs> {
            return MyScreenEntry(
                args = args,
                name = buildRouteName(MyScreenEntry::class.qualifiedName!!, args),
            )
        }
    }
}
```

### Example 2: Internal Entry With Result (New Feature)

**Input**:

```kotlin
@UiEntry(
    type = ImplementationType.Composable,
    args = PhotoPickerArgs::class,
    result = PhotoPickerResult::class
)
@Composable
fun PhotoPickerScreen(args: PhotoPickerArgs, navigator: NavigationController) { }
```

**Generated**:

```kotlin
@Parcelize
class PhotoPickerScreenEntry(
    override val args: PhotoPickerArgs,
    override val name: String,
    internal var requestKey: String? = null,  // For result delivery
) : ComposableEntry<PhotoPickerArgs>(args, name),
    ResultEntry<PhotoPickerArgs, PhotoPickerResult> {  // NEW interface

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

### Example 3: External Entry With Result

**Input**:

```kotlin
// Destination
data class PhotoPickerDestination(
    override val args: PhotoPickerArgs
) : DestinationWithArgs<PhotoPickerArgs>

// Screen
@UiExternalEntry(
    type = ImplementationType.Fragment,
    destination = PhotoPickerDestination::class,
    result = PhotoPickerResult::class
)
@Composable
fun PhotoPickerScreen(args: PhotoPickerArgs, navigator: NavigationController) { }
```

**Generated**:

```kotlin
class PhotoPickerScreenEntry : ComposableFragment(),
    ResultEntry<PhotoPickerArgs, PhotoPickerResult> {  // NEW

    override val resultType: Class<PhotoPickerResult>
        get() = PhotoPickerResult::class.java

    @Composable
    override fun ComposableContent() {
        PhotoPickerScreen(
            args = LocalArgs.current as PhotoPickerArgs,
            navigator = LocalNavigationController.current
        )
    }

    companion object : FragmentEntryFactory<PhotoPickerDestination> {

        override fun newInstance(destination: PhotoPickerDestination): FragmentEntry {
            val fragment = PhotoPickerScreenEntry()
            fragment.arguments = Bundle().apply {
                putParcelable(ARGS_KEY, destination.args)
            }
            return FragmentEntry(fragment)
        }

        fun newInstance(args: PhotoPickerArgs): FragmentEntry {
            return newInstance(PhotoPickerDestination(args))
        }
    }
}

// Destination extension (generated in EntryFactoryProvider)
// This allows PhotoPickerDestination to declare it has a result
fun PhotoPickerDestination.resultType(): Class<PhotoPickerResult> =
    PhotoPickerResult::class.java
```

---

## Backwards Compatibility

### Zero Breaking Changes Guarantee

This feature is designed to be **100% backwards compatible**:

1. **Optional Parameter**: The `result` parameter defaults to `NoResult::class`
2. **Existing Code Unchanged**: All current `@UiEntry` and `@UiExternalEntry` usages compile without modifications
3. **Generated Code**: Entries without result parameter generate the same code as before
4. **Runtime**: New methods are additions, not modifications
5. **API Surface**: No existing public APIs are changed or deprecated

### Migration Path

**Phase 1: Adoption (No Action Required)**

- Library update with new feature
- Existing code continues to work
- No recompilation needed for apps not using the feature

**Phase 2: Opt-In Usage**

- Developers can add `result` parameter to new or existing screens
- Incrementally adopt the feature where needed
- No coordination required across modules

**Phase 3: Full Adoption (Optional)**

- Convert SharedViewModel patterns to navigate-for-result
- Remove workarounds for result passing
- Cleaner, more maintainable code

### Compatibility Matrix

| Scenario                  | Before   | After    | Compatible? |
| ------------------------- | -------- | -------- | ----------- |
| @UiEntry without args     | ✅ Works | ✅ Works | ✅ Yes      |
| @UiEntry with args        | ✅ Works | ✅ Works | ✅ Yes      |
| @UiExternalEntry          | ✅ Works | ✅ Works | ✅ Yes      |
| Multi-module navigation   | ✅ Works | ✅ Works | ✅ Yes      |
| Fragment implementation   | ✅ Works | ✅ Works | ✅ Yes      |
| Composable implementation | ✅ Works | ✅ Works | ✅ Yes      |
| Existing generated code   | ✅ Works | ✅ Works | ✅ Yes      |

### Testing Backwards Compatibility

1. **Regression Test Suite**: Run all existing tests without modifications
2. **Sample App**: Existing sample app screens work without changes
3. **Binary Compatibility**: Check with binary compatibility validator
4. **Multi-Version Test**: Test with old and new versions coexisting

---

## Testing Strategy

### 1. Compilation Tests (tests/src/test/kotlin/nibel/tests/codegen/)

**New File: `ResultEntryCompileTest.kt`**

```kotlin
package nibel.tests.codegen

import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.annotations.UiExternalEntry

// Test 1: Internal entry with result (Composable)
@UiEntry(
    type = ImplementationType.Composable,
    args = TestArgs::class,
    result = TestResult::class
)
@Composable
fun ComposableEntryWithResult(args: TestArgs, navigator: NavigationController) = Unit

// Test 2: Internal entry with result, no args
@UiEntry(
    type = ImplementationType.Composable,
    result = TestResult::class
)
@Composable
fun ComposableEntryWithResultNoArgs(navigator: NavigationController) = Unit

// Test 3: Fragment entry with result
@UiEntry(
    type = ImplementationType.Fragment,
    args = TestArgs::class,
    result = TestResult::class
)
@Composable
fun FragmentEntryWithResult(args: TestArgs) = Unit

// Test 4: External entry with result
@UiExternalEntry(
    type = ImplementationType.Composable,
    destination = TestDestinationWithResult::class,
    result = TestResult::class
)
@Composable
fun ExternalEntryWithResult(args: TestArgs, navigator: NavigationController) = Unit

// Test result type
@Parcelize
data class TestResult(val value: String) : Parcelable

// Verify generated code implements ResultEntry interface
// Verify resultType property is present
// Verify companion object newInstance returns correct type
```

**Validation Tests**:

- Generated entry implements `ResultEntry<TArgs, TResult>`
- `resultType` property returns correct class
- Companion object `newInstance()` method signature is correct
- Type safety: result parameter type matches composable function usage

### 2. Integration Tests

**New File: `ResultNavigationTest.kt`**

```kotlin
@Test
fun `navigateForResult delivers result correctly`() {
    // Arrange
    val testResult = TestResult("success")
    var receivedResult: TestResult? = null

    // Act
    navigator.navigateForResult(TestScreenEntry.newInstance(args)) { result ->
        receivedResult = result
    }

    // In test screen, call:
    navigator.setResultAndNavigateBack(testResult)

    // Assert
    assertEquals(testResult, receivedResult)
}

@Test
fun `navigateForResult handles cancellation`() {
    // Arrange
    var receivedResult: TestResult? = TestResult("initial")

    // Act
    navigator.navigateForResult(TestScreenEntry.newInstance(args)) { result ->
        receivedResult = result
    }

    navigator.cancelResultAndNavigateBack()

    // Assert
    assertNull(receivedResult)
}

@Test
fun `external entry with result works across modules`() {
    // Test multi-module result delivery
}

@Test
fun `fragment implementation type delivers results`() {
    // Test Fragment Result API integration
}

@Test
fun `composable implementation type delivers results`() {
    // Test Compose result delivery mechanism
}
```

### 3. Sample App Integration

**New Screens in Sample App**:

**feature-A/PhotoPickerScreen.kt**:

```kotlin
@UiExternalEntry(
    type = ImplementationType.Composable,
    destination = PhotoPickerDestination::class,
    result = PhotoPickerResult::class
)
@Composable
fun PhotoPickerScreen(navigator: NavigationController, viewModel: PhotoPickerViewModel = hiltViewModel()) {
    // Mock photo picker UI
    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
        items(mockPhotos) { photo ->
            Image(
                painter = painterResource(photo.resId),
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable {
                        navigator.setResultAndNavigateBack(
                            PhotoPickerResult(photoUri = photo.uri)
                        )
                    }
            )
        }
    }

    Button(onClick = { navigator.cancelResultAndNavigateBack() }) {
        Text("Cancel")
    }
}

@Parcelize
data class PhotoPickerResult(val photoUri: String) : Parcelable
```

**feature-B/ProfileScreen.kt** (caller):

```kotlin
@UiExternalEntry(
    type = ImplementationType.Fragment,
    destination = ProfileScreenDestination::class
)
@Composable
fun ProfileScreen(navigator: NavigationController) {
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }

    Column {
        selectedPhotoUri?.let { uri ->
            AsyncImage(model = uri, contentDescription = "Profile photo")
        }

        Button(onClick = {
            navigator.navigateForResult(
                PhotoPickerDestination
            ) { result: PhotoPickerResult? ->
                result?.let { selectedPhotoUri = it.photoUri }
            }
        }) {
            Text("Select Photo")
        }
    }
}
```

### 4. Test Coverage Goals

- **Unit Tests**: 90%+ coverage for new code
- **Compilation Tests**: All annotation combinations
- **Integration Tests**: All navigation flows with results
- **Sample App**: Real-world usage demonstration
- **Regression Tests**: Existing tests pass without modification

### 5. Test Matrix

| Implementation Type | Args | Result | Internal | External | Status   |
| ------------------- | ---- | ------ | -------- | -------- | -------- |
| Composable          | No   | No     | ✅       | ✅       | Existing |
| Composable          | Yes  | No     | ✅       | ✅       | Existing |
| Composable          | No   | Yes    | ✅       | ✅       | New      |
| Composable          | Yes  | Yes    | ✅       | ✅       | New      |
| Fragment            | No   | No     | ✅       | ✅       | Existing |
| Fragment            | Yes  | No     | ✅       | ✅       | Existing |
| Fragment            | No   | Yes    | ✅       | ✅       | New      |
| Fragment            | Yes  | Yes    | ✅       | ✅       | New      |

---

## Implementation Plan

### Phase 1: Annotations & Foundation (Week 1)

**Tasks**:

1. Add `result` parameter to `@UiEntry` and `@UiExternalEntry` annotations
2. Create `NoResult` marker class
3. Update annotation KDoc with result examples
4. Create `ResultEntry<TArgs, TResult>` interface
5. Create `DestinationWithResult<TResult>` interface

**Deliverables**:

- `nibel-annotations` module updated
- Basic `nibel-runtime` interfaces added
- Documentation updated

**Testing**:

- Annotations compile successfully
- Default parameter works (backwards compatible)

### Phase 2: NavigationController API (Week 1-2)

**Tasks**:

1. Add `navigateForResult()` methods to NavigationController
2. Add `setResultAndNavigateBack()` method
3. Add `cancelResultAndNavigateBack()` method
4. Implement result delivery mechanism in `NibelNavigationController`
5. Handle Fragment Result API integration
6. Handle Composable result delivery

**Deliverables**:

- `NavigationController` API complete
- `NibelNavigationController` implementation
- Result callback storage and delivery

**Testing**:

- Unit tests for result delivery mechanism
- Mock navigation scenarios

### Phase 3: Compiler Changes (Week 2-3)

**Tasks**:

1. Update `EntryMetadata` to include `resultQualifiedName`
2. Modify `EntryGeneratingVisitor` to extract result type
3. Update `ComposableGenerator` to generate result-aware entries
4. Update `FragmentGenerator` for Fragment result entries
5. Modify `EntryFactoryProviderGenerator` for external results
6. Add validation for result type (must be Parcelable)

**Deliverables**:

- `nibel-compiler` generates correct code for entries with results
- Generated code implements `ResultEntry` interface
- Proper type safety in generated code

**Testing**:

- Compilation tests for all scenarios
- Verify generated code structure
- Type checking validation

### Phase 4: Testing & Sample App (Week 3-4)

**Tasks**:

1. Create `ResultEntryCompileTest.kt`
2. Create `ResultNavigationTest.kt`
3. Add PhotoPickerScreen to sample app (feature-A)
4. Add ProfileScreen caller to sample app (feature-B)
5. Add ConfirmationDialog example
6. Create comprehensive test matrix
7. Run full regression test suite

**Deliverables**:

- Complete test coverage
- Sample app demonstrations
- All tests passing

**Testing**:

- Run `./gradlew test` - all pass
- Run `./gradlew check` - all pass
- Sample app builds and runs

### Phase 5: Documentation & Polish (Week 4)

**Tasks**:

1. Update main README with result feature
2. Write migration guide
3. Add KDoc to all public APIs
4. Create usage examples document
5. Update CHANGELOG
6. Review and polish all code

**Deliverables**:

- Complete documentation
- Migration guide
- Usage examples
- Release notes

**Testing**:

- Documentation review
- Code review
- Final QA pass

### Timeline Summary

| Phase                         | Duration     | Start  | End    |
| ----------------------------- | ------------ | ------ | ------ |
| Phase 1: Annotations          | 3 days       | Day 1  | Day 3  |
| Phase 2: NavigationController | 5 days       | Day 3  | Day 8  |
| Phase 3: Compiler             | 7 days       | Day 8  | Day 15 |
| Phase 4: Testing              | 7 days       | Day 15 | Day 22 |
| Phase 5: Documentation        | 3 days       | Day 22 | Day 25 |
| **Total**                     | **~5 weeks** |        |        |

### Milestones

- **M1** (End Week 1): Annotations and API design complete
- **M2** (End Week 2): NavigationController implementation complete
- **M3** (End Week 3): Code generation complete and tested
- **M4** (End Week 4): Sample app and integration tests complete
- **M5** (End Week 5): Documentation and release ready

### Dependencies

- No external dependencies required
- No breaking changes to existing code
- Can be developed incrementally
- Each phase can be reviewed independently

---

## Alternatives Considered

### 1. API Design: Annotation Approach

| Option                          | Pros                                               | Cons                                         | Decision        |
| ------------------------------- | -------------------------------------------------- | -------------------------------------------- | --------------- |
| **Optional parameter** (chosen) | Backwards compatible, consistent with args, simple | None significant                             | ✅ **Selected** |
| Separate @UiEntryWithResult     | Cleaner separation, no risk of confusion           | Not backwards compatible, duplication        | ❌ Rejected     |
| Builder pattern                 | Flexible                                           | Too different from existing pattern, verbose | ❌ Rejected     |

### 2. Result Callback API

| Option                                    | Pros                                      | Cons                               | Decision        |
| ----------------------------------------- | ----------------------------------------- | ---------------------------------- | --------------- |
| **NavigationController methods** (chosen) | Centralized, consistent, easy to discover | None significant                   | ✅ **Selected** |
| ResultCallback parameter                  | Explicit in function signature            | Verbose, not idiomatic for Compose | ❌ Rejected     |
| Composition local                         | Consistent with LocalArgs                 | Less discoverable, awkward API     | ❌ Rejected     |

### 3. Caller API

| Option                           | Pros                       | Cons                                             | Decision        |
| -------------------------------- | -------------------------- | ------------------------------------------------ | --------------- |
| **navigateForResult()** (chosen) | Explicit intent, type-safe | New method to learn                              | ✅ **Selected** |
| Callback on navigateTo()         | Single method              | Overloading complexity, optional param confusion | ❌ Rejected     |
| Entry provides callback          | Self-contained             | Entry creation becomes complex, hard to test     | ❌ Rejected     |

### 4. Cancellation Handling

| Option                       | Pros                           | Cons                             | Decision        |
| ---------------------------- | ------------------------------ | -------------------------------- | --------------- |
| **Nullable result** (chosen) | Simple, common Android pattern | null can be ambiguous            | ✅ **Selected** |
| Sealed class Result<T>       | Explicit success/failure       | Verbose, boilerplate             | ❌ Rejected     |
| Separate callbacks           | Clear separation               | Two callbacks to manage, verbose | ❌ Rejected     |

### 5. Multi-Module Result Support

| Option                              | Pros                             | Cons                                 | Decision        |
| ----------------------------------- | -------------------------------- | ------------------------------------ | --------------- |
| **DestinationWithResult interface** | Type-safe, compile-time checking | Destination must implement interface | ✅ **Selected** |
| Runtime result type registration    | No interface needed              | Runtime errors, not type-safe        | ❌ Rejected     |
| Result in navigation module         | Centralized                      | Coupling, harder to maintain         | ❌ Rejected     |

### 6. Implementation for Fragments

| Option                  | Pros                                           | Cons                          | Decision        |
| ----------------------- | ---------------------------------------------- | ----------------------------- | --------------- |
| **Fragment Result API** | Native Android support, survives process death | Fragment-specific             | ✅ **Selected** |
| Custom callback storage | Unified with Composable                        | Doesn't survive process death | ❌ Rejected     |
| Activity Result API     | Standard pattern                               | Requires Activity, complex    | ❌ Rejected     |

### 7. Implementation for Composables

| Option                                       | Pros                            | Cons                            | Decision              |
| -------------------------------------------- | ------------------------------- | ------------------------------- | --------------------- |
| **Callback storage in NavigationController** | Simple, works well with Compose | Doesn't survive process death   | ✅ **Selected**       |
| SavedStateHandle                             | Survives process death          | Complex, requires serialization | ⚠️ Future enhancement |
| Navigation component SavedState              | Native support                  | Limited to Navigation component | ❌ Rejected           |

---

## Open Questions

### 1. Process Death Handling

**Question**: Should we support result delivery after process death for Composable entries?

**Current State**:

- Fragment entries use Fragment Result API (survives process death)
- Composable entries use callback storage (does NOT survive process death)

**Options**:

- **Option A** (current): Document limitation, acceptable for most use cases
- **Option B**: Use SavedStateHandle for Composable results (complex, future enhancement)
- **Option C**: Only support results for Fragment implementation type

**Recommendation**: Start with Option A, evaluate Option B in future versions based on user feedback.

### 2. Activity Result Contracts Integration

**Question**: Should we provide integration with Activity Result Contracts for system features?

**Use Case**: Camera, gallery, permissions, etc.

**Options**:

- **Option A**: Not in initial release (users can call Activity Result API directly)
- **Option B**: Provide wrapper utilities for common contracts
- **Option C**: Full Activity Result Contract support via custom destination type

**Recommendation**: Option A initially, Option B as follow-up feature.

### 3. Multiple Results

**Question**: Should we support streaming multiple results (e.g., progress updates)?

**Use Case**: File upload with progress, multi-select picker

**Options**:

- **Option A**: Not supported, use SharedViewModel or event bus for complex scenarios
- **Option B**: Add `resultFlow: Flow<TResult>` variant
- **Option C**: Support both single and streaming results

**Recommendation**: Option A initially, collect real-world use cases before adding complexity.

### 4. Result Validation

**Question**: Should we validate result types at runtime?

**Current State**: Compile-time checking via KSP

**Options**:

- **Option A**: Compile-time only (current)
- **Option B**: Add runtime type checking and throw if mismatch
- **Option C**: Add runtime checking with logging but don't throw

**Recommendation**: Option A (compile-time is sufficient), Option B if we see type mismatches in production.

### 5. Destination Result Type Declaration

**Question**: How should destinations declare their result type?

**Current Proposal**: `DestinationWithResult<TResult>` interface

**Options**:

- **Option A** (current): Interface with resultType property
- **Option B**: Annotation on destination class
- **Option C**: Compile-time sealed hierarchy

**Recommendation**: Option A (most flexible and type-safe).

### 6. Nested Navigate-For-Result

**Question**: What happens when screen A → screen B (for result) → screen C (for result)?

**Scenarios**:

- Screen B navigates for result before returning its own result
- Multiple levels of result-returning screens

**Options**:

- **Option A**: Support naturally (each screen tracks its own result callback)
- **Option B**: Disallow nested results (runtime error)
- **Option C**: Support with explicit parent result forwarding

**Recommendation**: Option A - should work naturally with callback stacking.

### 7. Configuration Changes

**Question**: How do results survive configuration changes (rotation)?

**Current State**:

- Fragment entries: Handled by Fragment Result API
- Composable entries: Callbacks stored in ViewModel-scoped NavigationController

**Recommendation**: Ensure NavigationController is ViewModel-scoped in sample app documentation.

---

## Security Implications

### Data Exposure Risks

**Risk**: Results contain sensitive data (PII, credentials, etc.)

**Mitigation**:

- Results are Parcelable and stay within app process
- No network transmission
- Follow same security model as navigation arguments
- Document best practices for sensitive data

**Recommendations**:

1. Don't pass sensitive data in results when possible
2. Use encrypted storage for sensitive data, pass only references
3. Clear result data when screen is destroyed
4. Follow Android security best practices for Parcelables

### Data Validation

**Risk**: Malformed or unexpected result data

**Mitigation**:

- Compile-time type checking ensures type safety
- Result types must be Parcelable (validated at compile-time)
- Null handling for cancellation is explicit

**Recommendations**:

1. Validate result data in callback before use
2. Handle null results defensively
3. Use sealed classes for result types with multiple states

### Memory Leaks

**Risk**: Stored callbacks could cause memory leaks

**Mitigation**:

- Callbacks cleared after delivery
- Callbacks cleared on cancellation
- NavigationController lifecycle-aware

**Recommendations**:

1. Ensure NavigationController is ViewModel-scoped
2. Document lifecycle considerations
3. Add leak detection in debug builds

### Denial of Service

**Risk**: Result callback queue grows unbounded

**Mitigation**:

- Each navigation creates single callback entry
- Callbacks removed after use
- Callback storage is bounded by navigation stack depth

**Recommendations**:

1. Add maximum callback storage limit
2. Add monitoring for callback queue size
3. Clear callbacks on app background

---

## References

### Android APIs

1. **Activity Result API**

   - [Activity Result API Guide](https://developer.android.com/training/basics/intents/result)
   - Pattern: Register contract, launch for result, handle callback
   - Inspiration for our `navigateForResult` API

2. **Fragment Result API**

   - [Fragment Result API Guide](https://developer.android.com/guide/fragments/communicate#fragment-result)
   - Used for Fragment implementation type result delivery
   - Survives process death

3. **Navigation Component**
   - [Navigation Result Handling](https://developer.android.com/guide/navigation/navigation-programmatic#returning_a_result)
   - SavedStateHandle-based result passing
   - Alternative approach we considered

### Kotlin / Compose

4. **Compose Navigation**

   - [Compose Navigation with Results](https://developer.android.com/jetpack/compose/navigation#returning_a_result)
   - Use SavedStateHandle for results
   - Our approach is more type-safe

5. **Kotlin Parcelize**
   - [Parcelable implementation generator](https://kotlinlang.org/docs/parcelize.html)
   - All args and results must be Parcelable

### Internal Documentation

6. **Nibel Architecture Patterns** (memory)
7. **Nibel Project Structure** (memory)
8. **Current @UiEntry documentation** (nibel-annotations)
9. **Current @UiExternalEntry documentation** (nibel-annotations)

---

## Glossary

| Term                      | Definition                                                      |
| ------------------------- | --------------------------------------------------------------- |
| **Entry**                 | Generated navigation class for a screen (e.g., `MyScreenEntry`) |
| **ResultEntry**           | Entry interface for screens that return results                 |
| **NavigationController**  | Core navigation component injected into composables             |
| **Result**                | Data returned from a called screen to its caller                |
| **Callback**              | Function invoked when result is delivered                       |
| **Destination**           | Lightweight navigation intent for multi-module navigation       |
| **DestinationWithResult** | Destination interface for screens returning results             |
| **NoResult**              | Marker class for screens without results (default)              |
| **Internal Entry**        | Entry defined with @UiEntry (single module)                     |
| **External Entry**        | Entry defined with @UiExternalEntry (multi-module)              |
| **Implementation Type**   | Fragment or Composable (how entry is rendered)                  |
| **Args**                  | Input parameters for a screen (Parcelable)                      |
| **KSP**                   | Kotlin Symbol Processing (annotation processing)                |
| **Parcelable**            | Android serialization interface for inter-component data        |
| **Navigate-for-result**   | Navigation pattern where caller receives data back              |
| **Cancellation**          | User exits without providing result (callback receives null)    |

---

## Appendix: Complete Usage Example

### Scenario: Photo Selection Flow

**Use Case**: User needs to select a photo from gallery for their profile.

#### Step 1: Define Result Type

```kotlin
// shared/models/PhotoResult.kt
@Parcelize
data class PhotoResult(
    val photoUri: String,
    val fileName: String,
    val mimeType: String
) : Parcelable
```

#### Step 2: Define Destination (Multi-Module)

```kotlin
// navigation module
object PhotoPickerDestination : DestinationWithNoArgs, DestinationWithResult<PhotoResult>
```

#### Step 3: Implement Photo Picker Screen

```kotlin
// feature-photo/PhotoPickerScreen.kt
@UiExternalEntry(
    type = ImplementationType.Composable,
    destination = PhotoPickerDestination::class,
    result = PhotoResult::class
)
@Composable
fun PhotoPickerScreen(
    navigator: NavigationController,
    viewModel: PhotoPickerViewModel = hiltViewModel()
) {
    val photos by viewModel.photos.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Photo") },
                navigationIcon = {
                    IconButton(onClick = { navigator.cancelResultAndNavigateBack() }) {
                        Icon(Icons.Default.Close, "Cancel")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(padding)
        ) {
            items(photos) { photo ->
                AsyncImage(
                    model = photo.uri,
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable {
                            val result = PhotoResult(
                                photoUri = photo.uri,
                                fileName = photo.fileName,
                                mimeType = photo.mimeType
                            )
                            navigator.setResultAndNavigateBack(result)
                        }
                )
            }
        }
    }
}
```

#### Step 4: Call from Profile Screen

```kotlin
// feature-profile/ProfileScreen.kt
@UiExternalEntry(
    type = ImplementationType.Fragment,
    destination = ProfileScreenDestination::class
)
@Composable
fun ProfileScreen(
    navigator: NavigationController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    Column {
        AsyncImage(
            model = profile.photoUri ?: R.drawable.default_avatar,
            contentDescription = "Profile photo",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Button(
            onClick = {
                navigator.navigateForResult(PhotoPickerDestination) { result: PhotoResult? ->
                    result?.let { photo ->
                        viewModel.updateProfilePhoto(photo.photoUri)
                        // Show success message
                        viewModel.showMessage("Photo updated successfully")
                    }
                }
            }
        ) {
            Text("Change Photo")
        }
    }
}
```

#### Step 5: ViewModel (Optional - for state management)

```kotlin
// feature-profile/ProfileViewModel.kt
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profile = MutableStateFlow(Profile())
    val profile: StateFlow<Profile> = _profile.asStateFlow()

    fun updateProfilePhoto(photoUri: String) {
        viewModelScope.launch {
            _profile.update { it.copy(photoUri = photoUri) }
            profileRepository.updatePhoto(photoUri)
        }
    }

    fun showMessage(message: String) {
        // Show snackbar or toast
    }
}
```

### Generated Code (Reference)

The KSP processor will generate:

```kotlin
// Generated: PhotoPickerScreenEntry.kt
@Parcelize
class PhotoPickerScreenEntry(
    override val args: NoArgs,
    override val name: String,
    internal var requestKey: String? = null,
) : ComposableEntry<NoArgs>(args, name),
    ResultEntry<NoArgs, PhotoResult> {

    override val resultType: Class<PhotoResult>
        get() = PhotoResult::class.java

    @Composable
    override fun ComposableContent() {
        PhotoPickerScreen(
            navigator = LocalNavigationController.current
        )
    }

    companion object : ComposableEntryFactory<PhotoPickerDestination> {
        override fun newInstance(destination: PhotoPickerDestination): ComposableEntry<NoArgs> {
            return PhotoPickerScreenEntry(
                args = NoArgs,
                name = buildRouteName(PhotoPickerScreenEntry::class.qualifiedName!!, NoArgs),
            )
        }
    }
}
```

### Complete Flow

1. User taps "Change Photo" button in ProfileScreen
2. `navigator.navigateForResult(PhotoPickerDestination)` is called
3. NavigationController stores callback and navigates to PhotoPickerScreen
4. User selects a photo in PhotoPickerScreen
5. `navigator.setResultAndNavigateBack(PhotoResult(...))` is called
6. NavigationController delivers result to callback
7. ProfileViewModel updates profile with new photo
8. UI updates to show new photo

---

**End of RFC**
