---
name: journey
description: Run the nibel sample app's journey tests (XML UI scenarios in sample/journeys/) on an Android emulator with the android CLI to catch navigation regressions. Use after changing nibel-runtime, nibel-compiler or the sample app, before opening a PR, or when asked to run journeys or check for regressions. Also covers writing new journeys.
---

# Journey tests for the nibel sample app

A journey is an XML test of the sample app's behavior: an ordered list of `<action>` steps, each
either a UI interaction or a check. Journeys live in `sample/journeys/*.xml` and are the source of
truth: if the app disagrees with a journey, the app has failed.

Arguments: journey file names or a substring of them (for example `results`). No arguments means
run every journey.

## 1. Prepare the device

1. `adb devices`. If no device is listed, run `android emulator list`, start one with
   `android emulator start <name>` and wait until
   `adb shell getprop sys.boot_completed` prints `1`.
2. With more than one device, pick one and pass `--device=<serial>` to every `android` command
   and `-s <serial>` to every `adb` command.
3. Build and install the sample from the repo root: `./gradlew :sample:app:installDebug`.
   If the build fails, stop and report the build error. Don't run journeys against a stale APK.

## 2. Run each journey

Create one report folder for the whole run before the first journey:
`build/journeys/<yyyyMMdd-HHmmss>/`.

Run journeys one at a time on a device. Before each journey, reset to a fresh launch:

```bash
adb shell cmd uimode night no
adb shell pm clear com.turo.nibel.sample
adb logcat -c
adb shell am start -W -n com.turo.nibel.sample/.MainActivity
```

Every journey starts on "Feature A | Zero Screen". Then evaluate the `<action>` elements in order:

- Execute each action exactly as written and independently of the others. Don't skip or merge
  actions because you think you know the intent.
- An action that lists several steps ("tap X and then Y") is evaluated as separate sub-actions.
- An action starting with "Verify" or "Check" only inspects the current screen. Don't scroll or
  tap to make it pass. Every expectation in it must hold.
- If an action can't be performed (element missing, wrong screen) or an expectation fails, the
  journey fails and evaluation stops. Right away, before any other action or retry, capture the
  screen with
  `android screen capture -o <report folder>/<journey>-failure.png`.
- If the app crashes, exits or freezes, the journey fails. After every action that isn't a check,
  run `adb logcat -d -b crash | grep -A40 com.turo.nibel.sample`. Any output is a crash: include
  it in the report. No output (and `grep` exiting with 1) means no crash.
- An action that is neither a UI interaction, a check, nor one of the device actions below makes
  the journey malformed. Report it and stop that journey.
- Keep debugging to a minimum. The goal is to report whether the current app passes the current
  journey; put fix suggestions in the report summary.

### Interacting

- Read the app's visible text with:
  ```bash
  android layout --flat | jq -r '.[] | select(.type=="APPLICATION") | .content[] | .text // empty'
  ```
  `android layout` waits for the UI to be idle, so no sleep is needed after an action. If it
  returns no app text at all, a transition was still running: wait a second and read again. If a
  text is missing, retry with `android layout --flat --full`: a toolbar title drawn under the status
  bar (Feature C's Fourth Screen) only shows up there. Use `android screen capture -o <file>` and look at the PNG only after that.
- Tap with `android input tap "text=<label>" --match=first --wait=5000`. The command prints JSON:
  the tap happened only if `.results[0].status` is `success`. `no_matches` means the element
  wasn't found, which fails the action.
- Selectors are comma-separated `key=value` pairs that match case-insensitive substrings.
  Spaces, parentheses and arrows are fine in a plain label (`text=Pick color (compose → compose)`).
  A label containing a comma must be written as an anchored regex with `(`, `)` and `.` escaped:
  `"text=/^First Screen \(type\.fragment, internal\)$/"`. A short substring can match a title or
  another button first, so prefer the full label.
- Type with `android input text "<text>" "clz=android.widget.EditText"`.
- Never use `adb shell uiautomator dump`: it conflicts with the android CLI's automation session
  and returns stale screens.

### Device actions

Journeys may use these actions besides UI interactions:

| Action                       | Commands                                              |
| ---------------------------- | ----------------------------------------------------- |
| Press the system back button | `android input key back`                              |
| Turn dark mode on            | `adb shell cmd uimode night yes`, then wait 2 seconds |
| Turn dark mode off           | `adb shell cmd uimode night no`, then wait 2 seconds  |
| Restart the app process      | See below                                             |

Toggling dark mode is a configuration change: the activity is destroyed and recreated in the same
process, like on rotation. Don't use rotation in journeys: every `android layout` and
`android input` call resets the device to portrait.

"Restart the app process" simulates process death. `am kill` only kills a process in the
background, and the app can take a few seconds to get there after HOME, so retry until it's gone:

```bash
adb shell input keyevent HOME
for i in $(seq 1 15); do
  sleep 1
  adb shell am kill com.turo.nibel.sample
  [ -z "$(adb shell pidof com.turo.nibel.sample)" ] && break
done
adb shell pidof com.turo.nibel.sample  # must print nothing, otherwise the action fails
adb shell am start -W -n com.turo.nibel.sample/.MainActivity
```

Android restores the back stack and saved state, so the app must come back on the screen it was
on. `am start` then prints `Warning: Activity not started, its current task has been brought to
the front` followed by `LaunchState: COLD`. That is expected; `LaunchState: HOT` means the process
wasn't killed and the action fails.

## 3. Report

Write one report per journey to the run's report folder, named after the journey file without
`.xml` (`navigation-chain-with-args.md`). Prefix screenshot names with the same name so journeys
don't overwrite each other's files:

```markdown
# Journey: <journey name>

## Results

### Action: <action text> ✅

- **Commands**: `android input tap "text=First Screen" --match=first --wait=5000`
- **Comment**: <only if useful>

### Action: <action text> ❌

- **Screenshot**: [navigation-chain-with-args-failure.png](navigation-chain-with-args-failure.png)
- **Comment**: <what was on screen instead, crash trace>

### Action: <action that was not evaluated>
```

Mark an action ✅ if it passed and ❌ if it failed. Leave actions that were never evaluated
unmarked. End with a short summary and any suggestions.

Then give the user a table with one row per journey (name, ✅/❌, the failing action and the
reason), plus the report folder path.

## Writing journeys

- One behavior per journey, starting from a fresh launch on the Zero screen.
- Use labels exactly as they appear on screen, and verify the screen after every navigation,
  device action and process restart.
- Make every screen identifiable by visible text. Two instances of the same screen need
  something that tells them apart, like the color picker's "Picker level: N".
- Use dark mode, not rotation, to force a configuration change.
- When a sample flow is added or changed, add or update its journey in the same change.
- File names are kebab-case and grouped by prefix: `navigation-*` for existing navigation,
  `results-*` for navigate for result.
