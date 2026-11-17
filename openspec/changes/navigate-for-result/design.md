# Design

## Context

See proposal.md for motivation and specs for the required behavior.

- Navigation runs through two mechanisms: fragment transactions on the activity's `supportFragmentManager` (`FragmentTransactionSpec`), and a `NavHost` per `ComposeNavigationDelegate.Content`, whose `NibelNavigationController` is shared by every compose screen in that host. `ComposableFragment` hosts a `NavHost` whose root route is the annotated composable.
- `NavigationController` is an abstract class. Consumers subclass it, including anonymously (`object : NavigationController()`), and extend `NibelNavigationController` and `ComposeNavigationDelegate`. `Entry`, `EntryFactory` and `ExternalDestination` are sealed, so new subtypes would break consumers' exhaustive `when`s.
- Annotations live in a JVM module that sees Android types only through `nibel-stub` (`NoArgs`, `Parcelable`). Code generation uses string templates.
- `tests` depends on the published `nibel-runtime`, so it doesn't exercise local runtime changes. There are no instrumentation tests and no API compatibility check.
- The spike on `spike/navigate-for-result` implements the untyped runtime path and passes the journeys in `sample/journeys/`.

## Goals / Non-Goals

**Goals:**

- One delivery mechanism for all three navigation directions, with no special cases per direction.
- No state on the shared controller and no process-wide registries: everything needed to deliver a result is saved state.
- Typed at compile time on the caller side, from the generated entry or destination.

**Non-Goals:**

- Results across activities. They degrade to a cancellation.
- Compile-time typing on the opened screen's side (`setResultAndNavigateBack` takes any `Parcelable`). It is checked at runtime against the declared type.
- Dialog or bottom-sheet destinations.
- Results from screens embedded with `Nibel.ExternalContent`. The embedded screen isn't opened for a result; screens it navigates to inside its own host work normally.

## Decisions

### Transport: the activity FragmentManager's result API

`setFragmentResult` / `setFragmentResultListener(key, lifecycleOwner)` on the activity's `supportFragmentManager` carries every result. The FragmentManager keeps unconsumed results in its saved state and delivers them when the listener's lifecycle owner is started. That gives survival across recreation and process death, and delivery only once the caller is visible. Compose callers register with their `NavBackStackEntry` as lifecycle owner; fragment callers with the fragment.

Alternatives: `NavBackStackEntry.savedStateHandle` of the previous entry is the Navigation-recommended pattern, but it only works inside one `NavHost`, while Nibel crosses hosts and fragment transactions. A process-wide callback registry (PR #38) loses callbacks on process death and leaks.

### The request key belongs to the opened screen

Each request has a key. For fragment entries it is written into the fragment's arguments before the transaction. For compose entries it is written into the new `NavBackStackEntry`'s `SavedStateHandle` right after navigating. `ComposableFragment` hands its key to its root route's entry. When a screen sets a result, the controller reads the key of `internalNavController.currentBackStackEntry`. Chained requests can't overwrite each other because each screen carries its own key. That bug crashed PR #38.

If the current entry after navigating isn't the requested one (a custom `ComposeSpec` that navigates differently), `navigateForResult` throws instead of attaching the key to the wrong screen.

### Launcher keys

- Compose: a UUID in `rememberSaveable`, so it is scoped to the caller's back stack entry and saved with it.
- Fragment: a UUID per launcher name, stored with the fragment's `SavedStateRegistry`. Each fragment instance gets its own key that survives recreation. The spike's `className:name` key collides for two instances of the same fragment.

### Cancellation detected on the caller side

Each launcher saves its pending request: the key, plus whether the caller has been paused since launching. When the caller resumes after having been paused and the request is still pending, the opened screen is gone without a result, so the launcher delivers `null` and clears the request. A real result clears the request first, because the FragmentManager delivers it at `ON_START`, before `ON_RESUME`. The launcher registers its FragmentManager listener before its own lifecycle observer so this order holds when observers replay. Explicit cancel sends a `null` result through the same transport. Compose navigation pauses the caller inside `navigate()`, before the request starts, so a request opened on a compose entry starts as already paused.

This covers system back, predictive back, `popBackStack` from anywhere and new-activity specs (the caller resumes with nothing delivered), for every type of opened screen, including plain fragments that know nothing about Nibel.

Alternative: detect removal on the opened screen's side with a ViewModel whose `onCleared` sends the cancel. It's precise for Nibel-generated screens, but plain fragments need a `FragmentLifecycleCallbacks` registration that is lost on process death, and it needs a FragmentManager reference outliving the screen.

### Typing without new sealed subtypes

- `ResultEntry<R>` is a final wrapper holding an `Entry`, typed only at compile time. Generated entries for screens with a result add `newInstanceForResult(args?)` returning it. `newInstance` is unchanged.
- `DestinationWithResult<R>` is a standalone interface, not part of the sealed `ExternalDestination` hierarchy. Destinations combine it with `DestinationWithArgs` or `DestinationWithNoArgs`.
- Overloads: `navigateForResult(entry: ResultEntry<R>, launcher: ResultLauncher<R>, …)` and `navigateForResult(destination: D, launcher: ResultLauncher<R>, …) where D : ExternalDestination, D : DestinationWithResult<R>`.
- `NoResult` follows `NoArgs`: a runtime object plus a stub in `nibel-stub`, used as the annotation default.
- The launcher holds the result `Class<R>`, captured by reified `rememberResultLauncher<R>` / `registerForNibelResult<R>`, so destinations get it too. Its name travels with the request key, and the runtime checks `isInstance` before sending.

Alternative: make generated entry classes implement a result interface (PR #38). This types composable entries only, because fragment-type factories return the `FragmentEntry` value class, and a generic `FragmentEntry` or a new `Entry` subtype would break existing code.

### Compiler validation

PR #38's parsing of the `result` argument and its data-class/object and no-generics checks are reused. For external entries, the visitor resolves the destination's `DestinationWithResult` supertype argument, compares it with the annotation's `result`, and reports the three mismatch cases from the spec with `logger.error` on the annotated symbol.

### Compatibility guard

The binary-compatibility-validator plugin is applied at the root for the modules consumers compile against: `nibel-annotations`, `nibel-runtime` and `nibel-stub`. `nibel-compiler` is excluded: KSP loads its processors through a service loader, and every class in it is public, so its dump would change with every internal refactor. API dumps are generated from `main` (the `v3.0.0` tag) before any change and committed, so the diff in this change shows only additions. CI runs `apiCheck`.

Version 0.18.2 only configures Android modules that apply `kotlin-android`, which AGP 9's built-in Kotlin doesn't, so it skips `nibel-runtime` silently. A convention plugin registers the validator's own build, check and dump tasks for `nibel-runtime` against its release Kotlin classes, marked as a workaround to remove once the validator supports AGP 9. The tasks only read compiled classes, so published AARs and JARs are unchanged.

The dumps compare JVM signatures, which misses source-only breaks. A fixture in `tests` mirrors the consumer patterns that must keep compiling: an anonymous `NavigationController`, a `NibelNavigationController` subclass that overrides navigation, a `ComposeNavigationDelegate` subclass, and custom fragment and compose specs.

Alternatives: KGP's built-in `abiValidation` isn't available on AGP 9's Kotlin extension. japicmp against the released artifacts doesn't understand Kotlin metadata, reports synthetic methods, and gives reviewers no committed API diff.

### Testing layers

- `tests` switches to `projects.nibelRuntime`.
- Compile tests in `tests` cover every annotation × implementation type × args combination with a result.
- Negative compiler tests use kotlin-compile-testing (kctfork, KSP2) to assert the error messages.
- Runtime behavior is covered by instrumentation tests in the sample app (Compose UI test with Espresso), one per direction plus chained, cancel, back and `ActivityScenario.recreate`. Process death stays with the journeys, since instrumentation can't kill the process mid-test. CI gets an emulator job.

## Risks / Trade-offs

- [The caller-side cancel misfires if the caller resumes while the opened screen is still shown] → A caller can't be resumed while another screen covers it in Nibel's navigation. The pending request also requires the caller to have been paused since the launch. Covered by instrumentation tests.
- [`FragmentTransactionSpec(replace = false)` adds the opened fragment over a caller that stays resumed, so back can't be detected] → The result and explicit cancel still work. Documented as a limit of add transactions.
- [A result set on an opened screen whose caller was removed (for example a cleared back stack) stays in the FragmentManager] → It is overwritten by the next request with that key, and keys are random. Negligible memory.
- [Writing the key into `SavedStateHandle` right after `navigate()` depends on `NavController` updating its back stack synchronously] → That is the documented behavior. The check that throws on a mismatch makes a regression loud.
- [More public API to keep stable] → API dumps and the compatibility check.
- [Emulator CI is slow and flaky] → One API level and retries on the instrumentation job. The journeys remain the manual end-to-end check before release.

## Migration Plan

Additive minor release (3.1.0 via `feat:`). Consumers opt in per screen and need no migration. Rollback means reverting the release, since nothing persists across versions except the transient FragmentManager results. Before release, a consuming app validates a pre-release artifact with a date-picker example. Once merged, PR #38 is closed with a link.
