# Nibel Essential Commands

## Development Workflow Commands

```bash
# After making changes - run all verification (MOST IMPORTANT)
./gradlew check

# Full build verification (used in CI)
./gradlew build

# Clean build to avoid stale artifacts
./gradlew clean build
```

## Testing Commands

```bash
# Run all unit tests
./gradlew test

# Run specific test variants
./gradlew testDebugUnitTest
./gradlew testReleaseUnitTest

# Run instrumentation tests (requires device)
./gradlew connectedAndroidTest
```

## Linting & Formatting Commands

```bash
# Fix lint issues automatically (IMPORTANT)
./gradlew lintFix

# Run Kotlin static analysis
./gradlew detekt

# Run all Android lint checks
./gradlew lint
```

## Module-Specific Commands

```bash
# Build specific modules
./gradlew :nibel-runtime:build
./gradlew :nibel-compiler:build
./gradlew :sample:app:build

# Test changes locally (for library development)
./gradlew publishToMavenLocal --no-configuration-cache
```

## Pre-commit & Git Commands

```bash
# Run pre-commit hooks manually
pre-commit run --all-files

# Standard git commands (macOS/Darwin system)
git status
git add .
git commit -m "feat: description"
git push
```

## Documentation Commands

```bash
# Generate documentation
./gradlew dokkaHtml
./gradlew dokkaGfm
```

## System Utilities (macOS/Darwin)

```bash
# File operations
ls -la          # List files with details
find . -name "*.kt"  # Find Kotlin files
grep -r "pattern" .  # Search in files
cd directory    # Change directory

# Development utilities
./gradlew tasks  # List all available Gradle tasks
./gradlew help   # Gradle help
```

## Critical Workflow

**ALWAYS run `./gradlew check` before considering any task complete!**
