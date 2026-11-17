# Spec Delta

## Purpose

Lets a screen open another screen and receive a typed result when that screen closes, across fragment and compose screens, in a way that survives configuration changes and process death.

## ADDED Requirements

### Requirement: Caller registers a result launcher

A compose caller and a fragment caller SHALL each be able to register a result launcher with a result callback. A launcher has one result type and identifies its caller's requests. It SHALL keep its identity across recomposition, configuration changes and process death.

#### Scenario: Compose caller registers a launcher

- **WHEN** a composable screen remembers a result launcher for result type `R`
- **THEN** it receives a launcher whose callback is invoked with results of type `R`

#### Scenario: Fragment caller registers a launcher

- **WHEN** a fragment registers a result launcher for result type `R` while it is being created
- **THEN** it receives a launcher whose callback is invoked with results of type `R`

#### Scenario: Launcher identity survives process death

- **GIVEN** a caller registered a launcher and opened a screen with it
- **WHEN** the process is killed and the app is restored
- **THEN** the launcher registered again by the restored caller receives the result of that screen

#### Scenario: Two instances of the same fragment caller

- **GIVEN** two instances of the same fragment class are on the back stack and each registered a launcher with the same name
- **WHEN** a screen opened by the first instance returns a result
- **THEN** only the first instance's callback is invoked

### Requirement: Navigate to a screen for a result

The navigation controller SHALL open a screen for a result given a launcher and either a screen entry or an external destination. The opened screen is shown exactly as with regular navigation, using the same navigation specs.

#### Scenario: Open an internal entry for a result

- **WHEN** a caller navigates for a result to an entry with a launcher
- **THEN** the entry's screen is shown and its result is delivered to that launcher

#### Scenario: Open an external destination for a result

- **WHEN** a caller navigates for a result to an external destination with a launcher
- **THEN** the screen associated with the destination is shown and its result is delivered to that launcher

#### Scenario: Unknown external destination

- **WHEN** a caller navigates for a result to an external destination that has no associated screen
- **THEN** navigation fails with an error naming the destination, as with regular navigation

### Requirement: Opened screen returns a result

A screen opened for a result SHALL be able to send a result to its caller and navigate back in one call. A plain fragment SHALL be able to send a result to its caller and navigate back itself. The caller's callback SHALL be invoked exactly once per opened screen.

#### Scenario: Compose screen returns a result

- **WHEN** a compose screen opened for a result sets result `X` and navigates back
- **THEN** the caller is shown again and its callback is invoked once with `X`

#### Scenario: Plain fragment returns a result

- **WHEN** a plain fragment opened for a result sets result `X` and navigates back
- **THEN** the caller is shown again and its callback is invoked once with `X`

#### Scenario: Result has the wrong type

- **WHEN** an opened screen sets a result whose type isn't the type it was opened for
- **THEN** the call fails with an error naming the expected and actual types, and nothing is delivered

### Requirement: Cancellation delivers a null result

The caller's callback SHALL be invoked with `null` when the opened screen is closed without a result: by an explicit cancel, by system or predictive back, or by any other navigation that removes it from the back stack.

#### Scenario: Explicit cancel

- **WHEN** an opened screen cancels its result and navigates back
- **THEN** the caller's callback is invoked once with `null`

#### Scenario: System back

- **WHEN** the user leaves an opened screen with the system back button or the predictive back gesture
- **THEN** the caller's callback is invoked once with `null`

#### Scenario: Result followed by back

- **WHEN** an opened screen sets a result and the user then leaves it with system back
- **THEN** the caller's callback is invoked once, with the result, and not again with `null`

### Requirement: Results reach the caller only when it is visible

A result or cancellation SHALL be delivered once the caller is at least started again, never while the opened screen or another screen covers the caller.

#### Scenario: Result while the caller is covered

- **WHEN** an opened screen sets a result before it is removed from the screen
- **THEN** the caller's callback runs after the caller is shown again, not before

### Requirement: Results survive configuration changes and process death

A pending request and an already-sent result SHALL survive configuration changes and process death of the caller, the opened screen, or both, in every supported navigation direction.

#### Scenario: Configuration change while the opened screen is shown

- **GIVEN** a caller opened a screen for a result
- **WHEN** the activity is recreated by a configuration change and the opened screen then returns `X`
- **THEN** the caller's callback is invoked once with `X`

#### Scenario: Process death while the opened screen is shown

- **GIVEN** a caller opened a screen for a result
- **WHEN** the process is killed, the app is restored on the opened screen, and the screen then returns `X`
- **THEN** the caller's callback is invoked once with `X`

### Requirement: Supported navigation directions

Navigating for a result SHALL work in every direction Nibel supports: fragment caller to compose screen, compose caller to compose screen, and compose caller to fragment screen, for screens in the same module and through external destinations in other modules.

#### Scenario: Fragment to compose

- **WHEN** a fragment opens a compose screen for a result and the screen returns `X`
- **THEN** the fragment's callback is invoked once with `X`

#### Scenario: Compose to compose

- **WHEN** a compose screen opens another compose screen for a result and it returns `X`
- **THEN** the caller's callback is invoked once with `X`

#### Scenario: Compose to fragment

- **WHEN** a compose screen opens a fragment screen for a result and it returns `X`
- **THEN** the caller's callback is invoked once with `X`

### Requirement: Chained requests are independent

A screen opened for a result SHALL be able to open further screens for results. Each result SHALL reach the caller that requested it, at any depth.

#### Scenario: Nested picker

- **GIVEN** screen A opened screen B for a result, and B opened screen C for a result
- **WHEN** C returns `X` and B then returns `Y`
- **THEN** B's callback is invoked with `X`, then A's callback is invoked with `Y`

#### Scenario: Regular navigation in between

- **GIVEN** screen A opened screen B for a result
- **WHEN** B navigates to screen C without a result, the user goes back to B, and B returns `Y`
- **THEN** A's callback is invoked once with `Y`

### Requirement: Relaunching a launcher replaces its request

A launcher SHALL have at most one request in flight. Opening a screen with a launcher that already has a request in flight SHALL replace that request.

#### Scenario: Launch twice

- **GIVEN** a caller opened a screen with a launcher and the screen is still open
- **WHEN** the caller's launcher is used again to open a second screen and that screen returns `X`
- **THEN** the callback is invoked once with `X`

### Requirement: Screens opened without a result

A screen that can return a result SHALL also work when opened with regular navigation. Setting or cancelling a result there SHALL navigate back without delivering anything.

#### Scenario: Result screen opened with regular navigation

- **WHEN** a screen opened with regular navigation sets a result and navigates back
- **THEN** it navigates back, nothing is delivered, and nothing fails

### Requirement: Backwards compatibility

Navigating for a result SHALL be additive. Existing code SHALL compile and behave as before without changes, including custom navigation controllers, delegates and navigation specs.

#### Scenario: Existing controller subclass

- **WHEN** an app that subclasses the navigation controller, including anonymous subclasses, upgrades to this version
- **THEN** it compiles without changes

#### Scenario: Existing navigation

- **WHEN** an app that doesn't use results upgrades to this version
- **THEN** its navigation behaves exactly as before

#### Scenario: Controller without result support

- **WHEN** a custom navigation controller that doesn't implement results is asked to navigate for a result
- **THEN** the call fails with an error saying the controller doesn't support results

### Requirement: Unsupported setups fail safely

Where a result can't be delivered, the caller SHALL receive a cancellation instead of waiting forever, or the call SHALL fail with a clear error.

#### Scenario: Screen opened in another activity

- **WHEN** a caller opens a screen for a result with a navigation spec that starts a new activity, and the user returns
- **THEN** the caller's callback is invoked once with `null`

#### Scenario: Compose navigation spec that doesn't navigate immediately

- **WHEN** a caller navigates for a result to a compose entry with a custom compose navigation spec that doesn't show the entry immediately
- **THEN** the call fails with an error explaining that the spec doesn't support results
