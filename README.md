# Nibel 💫

Nibel — is a type-safe navigation library for seamless integration of Jetpack Compose in fragment-based Android apps.

When we built it at Turo, our goal was to ensure a proper Jetpack Compose experience for the team when creating new features while keeping them compatible with the rest of the codebase automatically.

By leveraging the power of annotation processing Nibel provides a unified and type-safe way of navigating between screens in the following navigation scenarios:

- **fragment → compose**
- **compose → compose**
- **compose → fragment**

Nibel supports both **single-module** and **multi-module** navigation out-of-the-box. The latter is especially useful when navigating between feature modules that do not depend on each other directly.

- [Installation](#installation)
- [Basic usage](#basic-usage)
- [Multi-module navigation](#multi-module-navigation)
- [Sample app](#sample-app)

## Materials

- Blog post [Designing Jetpack Compose architecture for a gradual transition from fragments on Android](https://medium.com/turo-engineering/designing-jetpack-compose-architecture-for-a-gradual-transition-from-fragments-on-android-b11ee5f19ba8).
- Blog post [Introducing Nibel - a navigation library for seamless adoption of Jetpack Compose in fragment-based Android apps](https://medium.com/@morfly/introducing-nibel-a-navigation-library-for-adopting-jetpack-compose-in-fragment-based-apps-541c7b2f3f84).
- Talk [Migrating Android apps from Fragments to Jetpack Compose at Turo with Pavlo Stavytskyi
  ](https://youtu.be/SO0qjys_d08?si=xLtdbyp5ZRXXAp9j).

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

## Sample app

Check out a [sample app](sample) for demonstration of various scenarios of using Nibel in practice.

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
