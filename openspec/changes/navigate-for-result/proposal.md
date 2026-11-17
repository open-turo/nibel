# Proposal

## Why

Screens built with Nibel can't return data to the screen that opened them. Apps work around it with shared ViewModels, event buses or reverse navigation args, none of which are type-safe or survive process death. A first attempt (open-turo/nibel#38) stored the pending request on the shared navigation controller and the callbacks in a global registry: chained requests crashed, and results were lost on process death. A spike on `spike/navigate-for-result` showed that delivering results through the activity's `FragmentManager` result API, with the request key stored on the opened screen, works in all three navigation directions, including chained requests, configuration changes and process death.

## What Changes

- Callers register a result launcher: `rememberResultLauncher<R> { result -> }` in compose, `registerForNibelResult<R> { result -> }` in fragments. A launcher survives recomposition, configuration changes and process death.
- `NavigationController.navigateForResult(...)` opens a screen whose result goes to a launcher. It works for fragment → compose, compose → compose and compose → fragment, for internal entries and external destinations.
- Opened screens return with `setResultAndNavigateBack(result)` or `cancelResultAndNavigateBack()`; plain fragments use `Fragment.setNibelResult(result)`. Leaving with system or predictive back delivers a `null` (cancelled) result.
- `@UiEntry`, `@UiExternalEntry`, `@LegacyEntry` and `@LegacyExternalEntry` get an optional `result` parameter, defaulting to `NoResult::class`. Screens that declare one get a typed `newInstanceForResult(...)` factory.
- New `DestinationWithResult<R>` interface for multi-module destinations. The compiler fails the build when the annotation's result type and the destination's `R` disagree.
- The new `NavigationController` methods are `open` with default implementations, so existing subclasses keep compiling. No existing signature changes, and screens without a result generate the same code as today. Not breaking.
- The sample app gets a results demo covering every direction; the journey skill and journeys guard it.
- `tests` compiles against the local runtime instead of the published artifact, and gains result codegen tests. The sample gets instrumentation tests. A binary compatibility check runs in CI.

## Capabilities

### New Capabilities

- `navigation-results`: runtime behavior of navigating for a result: launching, delivering, cancelling, surviving recreation and process death, and the supported navigation directions and their limits.
- `result-entry-generation`: declaring result types on entry annotations and destinations, the typed entries the compiler generates for them, and the compile-time checks.

### Modified Capabilities

None. No specs exist yet in this project.

## Impact

- **Public API, additive only:** `nibel-annotations` (annotation parameters, `DestinationWithResult`), `nibel-runtime` (`ResultLauncher`, `ResultEntry`, `NoResult`, fragment extensions, new `NavigationController` methods), `nibel-stub` (`NoResult` stub).
- **Code generation:** `nibel-compiler` visitors, metadata and templates for entries that declare a result. Output for entries without a result is unchanged.
- **Runtime internals:** `NibelNavigationController`, `ComposeNavigationDelegate`, `ComposableFragment`, composition locals.
- **Known limits:** results across activities (for example a custom fragment spec that starts a new activity) aren't delivered, and the caller gets a cancel. A custom `ComposeSpec` must navigate the internal `NavController` synchronously.
- **Build and CI:** binary-compatibility-validator plugin and API dumps; an emulator job for instrumentation tests; `tests` module dependency change.
- **Docs and release:** README section; ships as a minor release (`feat:`, 3.1.0). No breaking-changes document.
- **Consumers:** validated in a consuming app with a pre-release artifact once the PR is open. Consumer code changes are outside this change.
- **Supersedes** open-turo/nibel#38.
