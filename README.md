# Nibel 💫

Nibel  —  is a type-safe navigation library for seamless integration of Jetpack Compose in fragment-based Android apps.

When we built it at Turo, our goal was to ensure a proper Jetpack Compose experience for the team when creating new features while keeping them compatible with the rest of the codebase automatically.

By leveraging the power of annotation processing Nibel provides a unified and type-safe way of navigating between screens in the following navigation scenarios:

- **fragment → compose**
- **compose → compose**
- **compose → fragment**

Nibel supports both **single-module** and **multi-module** navigation out-of-the-box. The latter is especially useful when navigating between feature modules that do not depend on each other directly.

- [Materials](#materials)
- [Installation](#installation)
- [Basic usage](#basic-usage)
- [Multi-module navigation](#multi-module-navigation)
- [Customization](#customization)
- [Sample app](#sample-app)

## Materials

- Blog post [Designing Jetpack Compose architecture for a gradual transition from fragments on Android](https://medium.com/turo-engineering/designing-jetpack-compose-architecture-for-a-gradual-transition-from-fragments-on-android-b11ee5f19ba8).
- Blog post [Introducing Nibel - a navigation library for seamless adoption of Jetpack Compose in fragment-based Android apps]().
- Conference talk [Designing Jetpack Compose architecture for gradual migration from fragments on Android](https://android-worldwide.com/register/).

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

The latest version of Nibel is [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.turo.nibel/nibel/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.turo.android/nibel)

## Basic usage

### Configuration

To start using Nibel, just call the `Nibel.configure()` function to initialize it in the `Application.OnCreate`.

### Defining a screen

When working with Nibel, all you need to do is to annotate a composable function that represents a screen with a `@UiEntry` annotation.

```kotlin
@UiEntry(
  type = ImplementationType.Fragment // one of <Fragment|Composable>
)
@Composable
fun FirstScreen() { ... }
```

When you build the code, Nibel will generate a `{ComposableName}Entry` class for the annotated composable that serves as an entry point to the screen.

The type of the generated entry differs depending on the `ImplementationType` used in the annotation. Each type serves a specific scenario and can be one of:

- `Fragment` - generates a fragment that uses the annotated composable as its content. It makes the compose screen look like a fragment to other fragments and is crucial in **fragment → compose** navigation scenarios.
- `Composable` - generates a lightweight entry over a composable and reduces the performance overhead by avoiding instantiation of fragment-related classes for this screen. It is normally used in **compose → compose** and **compose → fragment** navigation scenarios.

### Navigating fragment to compose

When you need to navigate from an existing fragment to a `FirstScreen` composable, you should treat a generated `FirstScreenEntry` as a fragment and use a transaction for navigation.

```kotlin
class ZeroScreenFragment : Fragment() {
  ...
  requireActivity().supportFragmentManager.commit {
    replace(android.R.id.content, FirstScreenEntry.newInstance().fragment)
  }
}
```

### Defining a screen with args

Nibel makes it easy to define screens with arguments in a type-safe manner. Just use the class of your `Parcelable` args in the `@UiEntry` annotation.

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

> Parameters of the composable function are optional. You can learn more about using composable functions with params with Nibel [here](#composable-function-params).

### Navigating compose to compose

If you want to navigate from one compose screen to another, use `NavigationController` provided by Nibel. Just create an instance of a screen entry and pass it to the `navigateTo` function.
Here is the example of navigating from `FirstScreen` to `SecondScreen` both of which we declared earlier.

```kotlin
@UiEntry(Fragment)
@Composable
fun FirstScreen(
  navigator: NavigationController // optional param
) {
  ...
  val args = SecondScreenArgs(...)
  navigator.navigateTo(SecondScreenEntry.newInstance(args))
}
```

> You can navigate between screens of any implementation type. It is recommended to use `ImplementationType.Composable` for every compose screen for better performance. The only exception are those screens that are navigated from actual fragments, as they should use `ImplementationType.Fragment`.

### Navigating compose to fragment

You might find yourself in a situation where you need to navigate from a compose screen to an existing fragment.

```kotlin
class ThirdScreenFragment : Fragment() { ... }
```

To navigate from `SecondScreen` composable to `ThirdScreenFragment` just wrap the latter with `FragmentEntry` and use the navigation controller.

```kotlin
@UiEntry(Composable, SecondScreenArgs::class)
@Composable
fun SecondScreen(navigator: NavigationController) {
  ...
  val fragment = ThirdScreenFragment()
  navigator.navigateTo(FragmentEntry(fragment))
}
```

## Multi-module navigation

In multi-module apps it is crucial to be able to navigate between feature modules that do not depend on each other. In this case, it is impossible to have a direct reference to a generated entry class from another module.

```
featureA          featureB
   │                  │
   └──► navigation ◄──┘
```

Nibel provides an easy way of navigating between feature modules in a type-safe manner using a concept of destinations.

### Defining a destination

Destination is a simple class that defines a navigation intent between feature modules. It is a simple object that should implement `DestinationWithNoArgs`. It should be declared in a separate module, that is available to other feature modules.

```kotlin
object FirstScreenDestination : DestinationWithNoArgs
```

Instead of `@UiEntry` we should use `@UiExternalEntry`. External is an entry that makes it possible to navigate to this screen from other feature modules.

```kotlin
@UiExternalEntry(
  type = ImplementationType.Fragment,
  destination = FirstScreenDestination::class
)
@Composable
fun FirstScreen() { ... }
```

Every external entry should be associated with a corresponding destination type. A destination can be related only to a single external entry. Using it with multiple entries will result in a compile time error.

### Navigating fragment to compose

When you need to navigate from an existing fragment to `FirstScreen` composable, use `Nibel.newFragmentEntry` to instantiate a new entry associated with its destination. In this case an instance of `FirstScreenEntry` is created that should be treated as a fragment and used in a transaction for navigation.

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

For a screen with arguments, make sure its associated destination implements `DestinationWithArgs` where the args should be `Parcelable`.

```kotlin
data class SecondScreenDestination(
  override val args: SecondScreenArgs // Parcelable args
) : DestinationWithArgs<SecondScreenArgs>
```

Now, all that's left to do is to connect the destination type with a `@UiExternalEntry`, so that the arguments are automatically available as params of a composable function.

```kotlin
@UiExternalEntry(
  type = ImplementationType.Composable,
  args = SecondScreenDestination::class
)
@Composable
fun SecondScreen(
  args: SecondScreenArgs, // optional param.
) { ... }
```

### Navigating compose to compose

If you want to navigate from one compose screen to another, use `NavigationController` provided by Nibel. Use an instance of a destination that is associated with the screen you want to havigate to.
Here is the example of navigating from `FirstScreen` to `SecondScreen` both of which we declared earlier.

```kotlin
@UiExternalEntry(...)
@Composable
fun FirstScreen(navigator: NavigationController) {
  ...
  val args = SecondScreenArgs(...)
  navigator.navigateTo(SecondScreenDestination(args))
}
```

### Navigating compose to fragment

```kotlin
@LegacyExternalEntry(
  destination = ThirdScreenDestination::class
)
class ThirdScreenFragment : Fragment() {
  ...
  arguments?.getNibelArgs<ThirdScreenArgs>()
}
```

```kotlin
@UiExternalEntry(...)
@Composable
fun SecondScreen(navigator: NavigationController) {
  ...
  val args = ThirdScreenArgs(...)
  navigator.navigateTo(ThirdScreenFragmentEntry.newInstance(args))
}
```

## Customization

### Applying a theme

```kotlin
object CustomRootContent : RootDelegate {

    @Composable
    override fun Content(content: @Composable () -> Unit) {
        AppTheme {
            content()
        }
    }
}
```

```kotlin
Nibel.configure(rootDelegate = AppRootContent)
```

### Parametrized navigation

```kotlin
navigator.navigateTo(
  destination = NextScreenDestination,
  fragmentSpec = FragmentTransactionSpec(...),
  composeSpec = ComposeNavigationSpec(...)
)
```

`replace` , `addToBackstack`, `containerId`

- `fragmentSpec` - when you navigate to:
  - a composable annotated with `Fragment` implementation type.
  - a regular fragment.
- `composeSpec` - when you navigate to:
  - a composable annotated with `Composable` implementation type.

Nibel provides `FragmentTransactionSpec` as a default fragment spec and `ComposeNavigationSpec` as default compose spec for navigation.

#### Custom params

```kotlin
navigator.navigateTo(
    destination = SecondScreenDestination,
    fragmentSpec = FragmentTransactionSpec(
        replace = false,
        addToBackStack = false,
        containerId = R.id.custom_container_id
    )
)
```

#### Custom navigation logic

Additionally, you can modify the entire behavior of a `FragmentTransactionSpec` by extending it with a custom class.

```kotlin
class CustomFragmentSpec(
    val customParam: ...,
) : FragmentTransactionSpec(...) {

    override FragmentTransactionContext.navigateTo(entry: FragmentEntry) {
    fragmentManager.commit {
        // custom transaction logic
    }
}
}
```

#### Default navigation specs

If you want to specify default project-wide specs for the navigation, you can do it with `Nibel.configure` function.

```kotlin
Nibel.configure(
    fragmentSpec = CustomFragmentSpec(),
    composeSpec = CustomComposeSpec()
)
```

### Custom args key

```kotlin
Nibel.configure(
    argsKey = "custom_args_key"
)
```

## Compatibility with architecture libraries

### [Android architecture components](https://developer.android.com/topic/architecture)

Nibel is compatible with [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) and [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) architecture components out-of-the-box. Just initialize a view model in your composable entry with `hiltViewModel`.

```Kotlin
@UiEntry(type = Composable, args = DemoArgs::class)
@Composable
fun DemoScreen(viewModel: DemoViewModel = hiltViewModel()) { ... }
```

Retrieve the arguments from the `SavedStateHandle` as you would normally do in your view model.

```kotlin
@HiltViewModel
class DemoViewModel(handle: SavedStateHandle): ViewModel() {
    val args = handle.getNibelArgs<DemoArgs>()
}
```

Nibel uses [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) library under-the-hood which provides a compatibility with Android architecture components as shown above.

### [Mavericks](https://github.com/airbnb/mavericks)

TBD

## Sample app

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
