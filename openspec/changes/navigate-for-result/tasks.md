# Tasks

## 1. Test and compatibility scaffolding

- [x] 1.1 Apply the binary-compatibility-validator plugin at the root for `nibel-annotations` and `nibel-stub` (`nibel-compiler` excluded), and register its tasks for `nibel-runtime` through a convention plugin (workaround for AGP 9's built-in Kotlin). Generate the API dumps from `main`'s sources (in a worktree of `main`), commit them, and verify `./gradlew apiCheck` passes on `main`, all four modules have a dump, and the release AAR's contents are identical with and without the plugin.
- [x] 1.2 Add `apiCheck` to the CI lint job in `.github/workflows/ci.yaml`, and verify actionlint passes in pre-commit.
- [x] 1.3 Switch `tests/build.gradle.kts` from `libs.nibel.runtime` to `projects.nibelRuntime`, and add a source-compatibility fixture mirroring consumer patterns (anonymous `NavigationController`, `NibelNavigationController` subclass overriding `navigateTo`, `ComposeNavigationDelegate` subclass, custom fragment and compose specs). Verify `./gradlew tests:test` passes and the fixture compiles against `main`'s runtime too.
- [x] 1.4 Add kctfork (KSP2) as a test dependency of `tests` with one negative test that asserts an existing compiler error (args type mismatch), and verify it passes with `./gradlew tests:test`.
- [x] 1.5 Add an `androidTest` source set to `sample/app` with Compose UI test and Espresso dependencies plus a smoke test that launches `MainActivity` and checks the Zero screen. Verify `./gradlew :sample:app:connectedDebugAndroidTest` passes on the emulator.
- [x] 1.6 Add a CI job that runs `:sample:app:connectedDebugAndroidTest` on an emulator (one API level, retries), and verify actionlint passes.

## 2. Annotations and result types

- [x] 2.1 Add `NoResult` to `nibel-runtime` and `nibel-stub`, mirroring `NoArgs`, and verify `./gradlew :nibel-runtime:assembleRelease :nibel-stub:build` succeeds.
- [x] 2.2 Add `result: KClass<out Parcelable> = NoResult::class` to `@UiEntry`, `@UiExternalEntry`, `@LegacyEntry` and `@LegacyExternalEntry`, with KDoc. Verify the sample builds unchanged.
- [x] 2.3 Add `DestinationWithResult<R : Parcelable>` to `nibel-annotations` as a standalone interface outside the sealed hierarchy, with KDoc. Verify a destination combining it with `DestinationWithArgs` compiles in `tests`.
- [x] 2.4 Run `./gradlew apiDump` and verify the dump diff contains only additions.

## 3. Runtime delivery

- [x] 3.1 Replace the spike's untyped API with `ResultEntry<R>` and typed `navigateForResult` overloads for entries and `DestinationWithResult` destinations. Keep the `open` defaults that throw for unsupported controllers, carry the result class with the request, and verify `apiCheck` after `apiDump` shows only additions against `main`.
- [x] 3.2 Fail `navigateForResult` when the current back stack entry after navigating isn't the requested compose entry, and verify with an instrumentation test using a custom `ComposeSpec` that doesn't navigate.
- [x] 3.3 Check the result type before sending (error naming expected and actual types), and make set/cancel on a screen opened without a result navigate back silently. Verify both with instrumentation tests.
- [x] 3.4 Store fragment launcher keys per instance in the fragment's `SavedStateRegistry`, and verify with an instrumentation test that two instances of one fragment class each get only their own result.
- [x] 3.5 Implement caller-side cancellation (pending request with a paused-since-launch flag, resolved on `ON_RESUME` after the FragmentManager listener), and verify with instrumentation tests: system back, `popBackStack` and result-then-back each deliver exactly once.
- [x] 3.6 Write instrumentation tests for fragment → compose, compose → compose, compose → fragment, chained requests, relaunching a launcher, and delivery after `ActivityScenario.recreate()`. Verify all pass on the emulator.
- [x] 3.7 Update the sample's results demo to the typed runtime API (`ResultEntry` wrappers until group 4 generates them), add the system back = cancel expectation to `results-compose-to-compose.xml`, and verify every `results-*` journey passes with `/journey results`.
- [x] 3.8 Add a README section on navigating for a result: launchers, opening screens, returning and cancelling, plain fragments, and the limits (other activities, add transactions, `ExternalContent`). Verify its code snippets match the sample.

## 4. Code generation

- [x] 4.1 Parse `result` in `AbstractEntryGeneratingVisitor` and `LegacyEntryGeneratingVisitor` into entry metadata, reusing PR #38's validation (data class or object, no generics). Verify kctfork negative tests for both errors.
- [x] 4.2 Generate `newInstanceForResult(...)` returning `ResultEntry<R>` for composable, fragment and legacy entries, with and without args. Leave output for entries without a result unchanged. Verify compile tests for every combination in `tests` and a byte-identical generated file for an existing entry.
- [x] 4.3 Validate destination and annotation result types for external entries (types differ, only destination, only annotation), and verify a kctfork negative test per case that asserts the error message.
- [x] 4.4 Add compile tests showing that a mismatched launcher type and an entry without a result don't compile (kctfork), and verify they pass.
- [x] 4.5 Switch the sample to generated typed entries. Add a cross-module result flow through a `DestinationWithResult` in `sample/navigation`, and make the legacy picker a `@LegacyEntry` with a result. Add a journey and an instrumentation test for the cross-module flow, and verify all journeys and instrumentation tests pass.
- [x] 4.6 Document `result =`, `newInstanceForResult` and `DestinationWithResult` in the README, and verify the snippets compile as written in the sample.

## 5. Integration and release readiness

- [x] 5.1 Run `./gradlew detekt lintDebug assembleDebug tests:test apiCheck :sample:app:connectedDebugAndroidTest` and `/journey` (all journeys), and verify everything passes.
- [x] 5.2 Reorganize the branch history into reviewable conventional commits (scaffolding, runtime, codegen, sample and docs), and verify each commit builds and passes pre-commit.

## Workflow follow-up

- Open the PR for review once the user approves.
- Add the pre-release tag to the PR to publish a pre-release artifact, then validate it in a consuming app: existing `NavigationController` subclasses compile unchanged, and a date picker returns a date to a compose caller and a fragment caller.
- After merge, close open-turo/nibel#38 with a link to the new PR.
- Archive the change after review is complete.
