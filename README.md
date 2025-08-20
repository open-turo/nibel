# Nibel 💫

Nibel — is a type-safe navigation library for seamless integration of Jetpack Compose in fragment-based Android apps.

When we built it at Turo, our goal was to ensure a proper Jetpack Compose experience for the team when creating new features while keeping them compatible with the rest of the codebase automatically.

By leveraging the power of annotation processing, Nibel provides a unified and type-safe way of navigating between screens in the following navigation scenarios:

- **fragment → compose**
- **compose → compose**
- **compose → fragment**

Nibel supports both **single-module** and **multi-module** navigation out-of-the-box. The latter is especially useful when navigating between feature modules that do not depend on each other directly.

- [Installation](#installation)
- [Basic usage](#basic-usage)
- [Multi-module navigation](#multi-module-navigation)
- [Result-based navigation](#result-based-navigation)
- [Advanced features](#advanced-features)
- [Sample app](#sample-app)
- [Migration guide](#migration-guide)

## Materials

- [Designing Jetpack Compose architecture for a gradual transition from fragments on Android](https://medium.com/turo-engineering/designing-jetpack-compose-architecture-for-a-gradual-transition-from-fragments-on-android-b11ee5f19ba8) - blog post at Turo Engineering.
- [Introducing Nibel - a navigation library for seamless adoption of Jetpack Compose in fragment-based Android apps](https://medium.com/@morfly/introducing-nibel-a-navigation-library-for-adopting-jetpack-compose-in-fragment-based-apps-541c7b2f3f84) - blog post at Turo Engineering.
- [Migrating Android apps from Fragments to Jetpack Compose at Turo](https://youtu.be/SO0qjys_d08?si=xLtdbyp5ZRXXAp9j) - talk at Android Worldwide.
- [Migrating Android apps from Fragments to Jetpack Compose with Nibel](https://www.droidcon.com/2023/10/06/migrating-android-apps-from-fragments-to-jetpack-compose-with-nibel/) - talk at droidcon New York 2023.

## Installation

Nibel consists of 2 components: **runtime** and **compiler**. The latter enables code generation to provide a type-safe way of navigating between screens.

In the `build.gradle.kts` of your feature module add the dependencies below.

```kotlin
dependencies {
  implementation("com.openturo.nibel:nibel-runtime:x.y.z")
  ksp("com.openturo.nibel:nibel-compiler:x.y.z")
}
```

Don't forget to apply a KSP plugin and enable Jetpack Compose for every module where Nibel is used.

```kotlin
plugins {
  id("com.google.devtools.ksp")
}
```

The latest version of Nibel is [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.openturo.nibel/nibel-runtime/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.openturo.nibel/nibel-runtime).

## Basic usage

### Configuration

To start using Nibel, just call the `Nibel.configure()` function to initialize it in the `Application.OnCreate`.

### Delaring a screen

When working with Nibel, all you need to do is to annotate a composable function that represents a screen with a `@UiEntry` annotation.

```kotlin
@UiEntry(
  type = ImplementationType.Fragment // one of <Fragment|Composable>
)
@Composable
fun FirstScreen() { ... }
```

When you build the code, there will be generated a `{ComposableName}Entry` class that serves as an entry point to the screen.

The type of the generated entry differs depending on the `ImplementationType` specified in the annotation. Each type serves a specific scenario and can be one of:

- `Fragment` - generates a fragment that uses the annotated composable as its content. It makes the compose screen look like a fragment to other fragments and is crucial in **fragment → compose** navigation scenarios.
- `Composable` - generates a small wrapper class over a composable. It is normally used in **compose → compose** and **compose → fragment** navigation scenarios.

> Use `ImplementationType.Composable` as much as you can to reduce the performance overhead by avoiding instantiation of fragment-related classes for each screen under-the-hood.

### Navigating fragment to compose

To navigate from an existing fragment to a `FirstScreen` composable you should treat a generated `FirstScreenEntry` as a fragment and use a transaction for navigation.

```kotlin
class ZeroScreenFragment : Fragment() {
  ...
  requireActivity().supportFragmentManager.commit {
    replace(android.R.id.content, FirstScreenEntry.newInstance().fragment)
  }
}
```

### Declaring a screen with args

For screens with arguments, just pass the class of your `Parcelable` args in the `@UiEntry` annotation.

```kotlin
@UiEntry(
  type = ImplementationType.Composable,
  args = SecondScreenArgs::class // optional Parcelable args
)
@Composable
fun SecondScreen(
  args: SecondScreenArgs, // optional param
) { ... }
```

Optionally, arguments could be declared in params of the composable function, so that the instance is automatically provided by Nibel.

> **Warning**: If argument types in the annotation and function params do not match, a compile time error will be thrown.

### Navigating compose to compose

The core navigation component within compose screens is `NavigationController`. It can be optionally declared in params of the composable function, so that the instance is automatically provided by Nibel.

To navigate from `FirstScreen` to `SecondScreen`, use `navigateTo` with an instance of a generated entry class.

```kotlin
@UiEntry(type = ImplementationType.Fragment)
@Composable
fun FirstScreen(
  navigator: NavigationController // optional param
) {
  ...
  val args = SecondScreenArgs(...)
  navigator.navigateTo(SecondScreenEntry.newInstance(args))
}
```

> You can navigate between screens of any `ImplementationType` with no limitations but it is recommended to use `Composable` for every screen unless it is reached from an existing fragment.

### Navigating compose to fragment

When adopting Jetpack Compose there if often the need to navigate from compose screens to old fragments.

```kotlin
class ThirdScreenFragment : Fragment() { ... }
```

To navigate from `SecondScreen` composable to `ThirdScreenFragment` just wrap the latter with `FragmentEntry`.

```kotlin
val fragment = ThirdScreenFragment()
navigator.navigateTo(FragmentEntry(fragment))
```

## Multi-module navigation

In multi-module apps it is common to have feature modules that do not depend on each other directly. This leads to inability of obtaining direct references to generated entry classes.

Nibel provides an easy way of multi-module navigation in a type-safe manner using a concept of destinations. Destination — is a simple data type that stands for a navigation intent. It is located in a separate module available to other feature modules.

```
 featureA          featureB
  module            module
    │                  │
    └──► navigation ◄──┘
           module
```

### Declaring a destination

The most basic destination is an `object` that implements `DestinationWithNoArgs` an is declared in a separate navigation module, available to other feature modules.

```kotlin
// navigation module available to other feature modules
object FirstScreenDestination : DestinationWithNoArgs
```

A destination then must be associated with a corresponding compose screen. This time there should be used `UiExternalEntry` annotation instead of `UiEntry`.

```kotlin
// feature module
@UiExternalEntry(
  type = ImplementationType.Fragment,
  destination = FirstScreenDestination::class
)
@Composable
fun FirstScreen() { ... }
```

A destination must be associated with **exactly one** compose screen. Otherwise, a compile time error will be thrown.

> `UiExternalEntry` includes all the functionallity of `UiEntry`. This includes generating entry classes for navigating within a single feature module.

### Navigating fragment to compose

To navigate from an existing fragment to a `FirstScreen` composable, use a destination to obtain a `FirstScreenEntry` instance by calling `Nibel.newFragmentEntry`. Treat it as a fragment and use a transaction for navigation.

```kotlin
class ZeroScreenFragment : Fragment() {
  ...
  requireActivity().supportFragmentManager.commit {
    val entry = Nibel.newFragmentEntry(FirstScreenDestination)!!
    replace(android.R.id.content, entry.fragment)
  }
}
```

### Defining a destination with args

For a screen with arguments, make sure its associated destination is a `data class` that implements `DestinationWithArgs` where the args should be `Parcelable`.

```kotlin
data class SecondScreenDestination(
  override val args: SecondScreenArgs // Parcelable args
) : DestinationWithArgs<SecondScreenArgs>
```

Now, all that's left to do is to connect the destination type with a `@UiExternalEntry`, so that the arguments are automatically available as params of a composable function.

```kotlin
@UiExternalEntry(
  type = ImplementationType.Composable,
  destination = SecondScreenDestination::class
)
@Composable
fun SecondScreen(args: SecondScreenArgs) { ... }
```

### Navigating compose to compose

To navigate from `FirstScreen` to `SecondScreen` both of which are composables defined above, use `navigateTo` with an instance of a destination assiciated with the target screen.

```kotlin
val args = SecondScreenArgs(...)
navigator.navigateTo(SecondScreenDestination(args))
```

### Navigating compose to fragment

To navigate from `SecondScreen` composable to `ThirdScreenFragment` annotate the latter with `@LegacyExternalEntry` and associate it with a destination.

```kotlin
@LegacyExternalEntry(destination = ThirdScreenDestination::class)
class ThirdScreenFragment : Fragment() {
  ...
  // Accessing screen arguments
  arguments?.getNibelArgs<ThirdScreenArgs>()
}
```

Then, use the destination for navigation.

```kotlin
val args = ThirdScreenArgs(...)
navigator.navigateTo(ThirdScreenDestination(args))
```

## Result-based navigation

Nibel supports result-based navigation that allows screens to return typed data to the previous screen. This feature integrates seamlessly with Android's Activity Result API pattern while maintaining Nibel's type-safe approach and works across both single-module and multi-module architectures.

Result navigation is perfect for scenarios like:

- **Photo/file pickers** returning selected media
- **User selection screens** returning chosen users/items
- **Form/settings screens** returning configuration data
- **Authentication flows** returning user credentials
- **Any modal workflow** that needs to return data

### Declaring a result screen

To create a screen that can return a result, specify the `result` parameter in your annotation with a `Parcelable` result type:

```kotlin
@Parcelize
data class PhotoResult(
    val photoUrl: String,
    val photoName: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class PhotoArgs(
    val maxPhotos: Int,
    val allowMultiple: Boolean = false
) : Parcelable

@UiEntry(
    type = ImplementationType.Composable,
    args = PhotoArgs::class,
    result = PhotoResult::class  // This screen can return PhotoResult
)
@Composable
fun PhotoPickerScreen(
    args: PhotoArgs,
    navigator: NavigationController
) {
    var selectedPhotos by remember { mutableStateOf(emptyList<String>()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select up to ${args.maxPhotos} photos")

        // Photo selection UI
        LazyVerticalGrid(columns = GridCells.Fixed(3)) {
            items(availablePhotos) { photo ->
                PhotoItem(
                    photo = photo,
                    isSelected = selectedPhotos.contains(photo),
                    onToggle = {
                        selectedPhotos = if (selectedPhotos.contains(photo)) {
                            selectedPhotos - photo
                        } else {
                            (selectedPhotos + photo).take(args.maxPhotos)
                        }
                    }
                )
            }
        }

        Row {
            Button(onClick = {
                // Return result with selected photo
                if (selectedPhotos.isNotEmpty()) {
                    val result = PhotoResult(
                        photoUrl = selectedPhotos.first(),
                        photoName = "selected_photo.jpg"
                    )
                    navigator.setResultAndNavigateBack(result)
                }
            }) {
                Text("Select")
            }

            Button(onClick = {
                // Cancel without returning a result
                navigator.cancelResultAndNavigateBack()
            }) {
                Text("Cancel")
            }
        }
    }
}
```

When you build the code, the generated entry class will implement both `ComposableEntry` and `ResultEntry<PhotoResult>` interfaces.

### Navigating for a result

To navigate to a result-returning screen and receive the result:

```kotlin
@UiEntry(type = ImplementationType.Composable)
@Composable
fun HomeScreen(navigator: NavigationController) {
    var selectedPhoto by remember { mutableStateOf<PhotoResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = {
                isLoading = true
                navigator.navigateForResult(
                    entry = PhotoPickerScreenEntry.newInstance(
                        PhotoArgs(maxPhotos = 5, allowMultiple = true)
                    ),
                    callback = { result: PhotoResult? ->
                        isLoading = false
                        // Handle the result - null if cancelled
                        if (result != null) {
                            selectedPhoto = result
                        } else {
                            // User cancelled the photo selection
                            println("Photo selection was cancelled")
                        }
                    }
                )
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text("Pick Photo")
            }
        }

        // Display result
        selectedPhoto?.let { photo ->
            Card(modifier = Modifier.padding(top = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Selected Photo:", style = MaterialTheme.typography.titleMedium)
                    Text("Name: ${photo.photoName}")
                    Text("URL: ${photo.photoUrl}")
                    Text("Selected at: ${Date(photo.timestamp)}")
                }
            }
        }
    }
}
```

### Multi-module result navigation

For cross-module result navigation, use external destinations with the `result` parameter:

```kotlin
// In navigation module
@Parcelize
data class UserSelectionResult(
    val userId: String,
    val userName: String
) : Parcelable

data class UserPickerDestination(
    override val args: UserPickerArgs
) : DestinationWithArgs<UserPickerArgs>
```

```kotlin
// In feature module
@UiExternalEntry(
    type = ImplementationType.Composable,
    destination = UserPickerDestination::class,
    result = UserSelectionResult::class
)
@Composable
fun UserPickerScreen(
    args: UserPickerArgs,
    navigator: NavigationController
) {
    // Implementation
    Button(onClick = {
        navigator.setResultAndNavigateBack(
            UserSelectionResult("123", "John Doe")
        )
    }) {
        Text("Select User")
    }
}
```

```kotlin
// Navigate from another module
navigator.navigateForResult(
    destination = UserPickerDestination(UserPickerArgs()),
    callback = { result: UserSelectionResult? ->
        result?.let { user ->
            // Use the selected user
            println("Selected user: ${user.userName}")
        }
    }
)
```

### Type safety and error handling

Nibel ensures compile-time type safety for result navigation:

- **Result types** must be `Parcelable` data classes or objects
- **Factory methods** return `ResultEntry<R>` instead of generic entries
- **Callbacks** are strongly typed to the expected result type
- **Runtime errors** occur if destinations don't implement `ResultEntry`

### Edge cases handled

The result navigation system handles several important scenarios:

- **Configuration changes**: Results survive device rotation and process death
- **Multiple requests**: Each navigation request gets a unique callback key
- **Lifecycle management**: Callbacks are automatically cleaned up
- **Cancellation**: Users can cancel without providing a result (`null` callback)

### Best practices

- **Keep results simple**: Use lightweight `Parcelable` data classes
- **Handle null results**: Always check if the result is null (cancelled)
- **Cleanup resources**: The framework handles callback lifecycle automatically
- **Test thoroughly**: Verify both success and cancellation paths

### Migration from existing patterns

If you're migrating from Fragment result APIs or other patterns:

```kotlin
// Before (Fragment)
setFragmentResultListener("photo_key") { _, bundle ->
    val result = bundle.getParcelable<PhotoResult>("photo_data")
    // handle result
}

// After (Nibel)
navigator.navigateForResult(
    entry = PhotoPickerScreenEntry.newInstance(args)
) { result: PhotoResult? ->
    // handle result - automatically typed and null-safe
}
```

## Advanced features

### Fragment result navigation

Nibel also supports result navigation with fragments using the same type-safe approach:

```kotlin
@LegacyExternalEntry(
    destination = LegacyPickerDestination::class,
    result = LegacyPickerResult::class
)
class LegacyPickerFragment : Fragment() {

    private fun returnResult() {
        val result = LegacyPickerResult("selected_value")
        // Use Nibel's result API in fragments
        requireActivity().supportFragmentManager.setNibelResult(result)
        findNavController().popBackStack()
    }

    private fun cancelResult() {
        requireActivity().supportFragmentManager.cancelNibelResult<LegacyPickerResult>()
        findNavController().popBackStack()
    }
}
```

### Generated code structure

Understanding what Nibel generates helps with debugging and advanced usage:

```kotlin
// For a result-enabled screen, Nibel generates:
class PhotoPickerScreenEntry private constructor() :
    ComposableEntry, ResultEntry<PhotoResult> {

    companion object : EntryFactory<PhotoPickerScreenDestination, PhotoPickerScreenEntry> {
        fun newInstance(args: PhotoArgs): PhotoPickerScreenEntry =
            PhotoPickerScreenEntry().apply {
                this.args = args
            }
    }

    // Type-safe result handling
    override fun unwrapResult(): PhotoResult? = /* implementation */
    override fun setResult(result: PhotoResult) { /* implementation */ }
}
```

### Performance considerations

- **Use `ImplementationType.Composable`** when possible to avoid Fragment overhead
- **Result entries** have minimal performance impact beyond regular navigation
- **Generated classes** are optimized for fast instantiation and memory efficiency
- **Multi-module resolution** caches destination factories after first lookup

### Debugging navigation issues

Common issues and solutions:

```kotlin
// Issue: "Nibel not configured"
// Solution: Ensure Nibel.configure() is called in Application.onCreate()

// Issue: "Destination not found"
// Solution: Verify @UiExternalEntry has correct destination class

// Issue: "Result callback never called"
// Solution: Check that result screen calls setResultAndNavigateBack()

// Issue: "ClassCastException in result"
// Solution: Ensure result types match between annotation and callback
```

### Integration with existing navigation

Nibel can coexist with other navigation solutions:

```kotlin
@Composable
fun HybridScreen(navigator: NavigationController) {
    // Mix Nibel navigation with other approaches
    Button(onClick = {
        // Use Nibel for type-safe navigation
        navigator.navigateTo(TypedScreenDestination(args))
    }) {
        Text("Navigate with Nibel")
    }

    Button(onClick = {
        // Use traditional Fragment transactions when needed
        requireActivity().supportFragmentManager.commit {
            replace(R.id.container, LegacyFragment())
        }
    }) {
        Text("Navigate with Fragments")
    }
}
```

## Sample app

Check out a [sample app](sample) for demonstration of various scenarios of using Nibel in practice.

## Migration guide

### From Fragment-based navigation

When migrating existing Fragment-based apps to use Nibel:

1. **Start with new screens**: Use `@UiEntry` for new Compose screens
2. **Gradual adoption**: Keep existing fragments, add Nibel navigation incrementally
3. **Legacy integration**: Use `@LegacyEntry` to make existing fragments accessible from Compose

```kotlin
// Step 1: Existing fragment
class ProfileFragment : Fragment() { /* existing code */ }

// Step 2: Make it accessible from Compose
@LegacyEntry
class ProfileFragment : Fragment() { /* existing code */ }

// Step 3: Create new Compose screens with Nibel
@UiEntry(type = ImplementationType.Composable)
@Composable
fun NewFeatureScreen(navigator: NavigationController) {
    // Can navigate to existing fragments
    navigator.navigateTo(FragmentEntry(ProfileFragment()))
}
```

### From Navigation Component

Nibel can complement or replace Navigation Component:

```kotlin
// Before (Navigation Component)
navController.navigate(
    ProfileFragmentDirections.actionToSettings(userId = "123")
)

// After (Nibel)
navigator.navigateTo(
    SettingsScreenDestination(SettingsArgs(userId = "123"))
)
```

### From manual result handling

Replace manual result handling with type-safe result navigation:

```kotlin
// Before (Fragment Result API)
childFragmentManager.setFragmentResultListener("picker_key", this) { _, bundle ->
    val photoUrl = bundle.getString("photo_url")
    val photoName = bundle.getString("photo_name")
    if (photoUrl != null && photoName != null) {
        handlePhotoResult(PhotoData(photoUrl, photoName))
    }
}

// After (Nibel)
navigator.navigateForResult(
    entry = PhotoPickerScreenEntry.newInstance(args)
) { result: PhotoResult? ->
    result?.let { photo ->
        handlePhotoResult(photo) // Automatically typed and null-safe
    }
}
```

### Migration checklist

- [ ] **Add Nibel dependencies** to feature modules
- [ ] **Configure Nibel** in `Application.onCreate()`
- [ ] **Identify navigation patterns** in existing code
- [ ] **Create destination classes** for multi-module navigation
- [ ] **Annotate new Compose screens** with `@UiEntry` or `@UiExternalEntry`
- [ ] **Add `@LegacyEntry`** to existing fragments as needed
- [ ] **Replace manual result handling** with `navigateForResult()`
- [ ] **Test navigation flows** thoroughly in both directions
- [ ] **Verify type safety** at compile time

## Contributing

Pull requests are welcome! See [here](https://github.com/open-turo/contributions) for guidelines on how to contribute to this project.

## License

```
MIT License

Copyright (c) 2023 Turo Open Source

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
