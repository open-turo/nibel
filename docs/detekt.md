# Detekt - Static Code Analysis

[Detekt](https://github.com/detekt/detekt) is a static code analysis tool for Kotlin that helps maintain code quality, enforce consistent coding standards, and catch common mistakes before code is committed.

## Overview

This project uses Detekt with the following configuration:

- **Configuration file**: `config/detekt/detekt.yaml`
- **Baseline file**: `config/detekt/baseline.xml` (tracks existing issues)
- **Integration**: Pre-commit hooks, CI pipeline, and manual execution

## Quick Commands

### Run Detekt Analysis

```bash
./gradlew detekt
```

### Auto-fix Code Issues (where possible)

```bash
./gradlew detektFormat
```

### Update Baseline (suppress existing issues)

```bash
./gradlew detektBaseline
```

## Pre-commit Hooks Setup

### Install Pre-commit Hooks

If you haven't already, install the pre-commit hooks:

```bash
pre-commit install
```

### What Runs on Commit

When you commit code, the following quality checks run automatically:

- Detekt static analysis on all Kotlin files
- Prettier formatting
- JSON/YAML validation
- Trailing whitespace removal
- End-of-file fixing
- GitHub Actions workflow validation

### Skip Pre-commit Hooks (Not Recommended)

In rare cases, you can skip pre-commit hooks:

```bash
git commit --no-verify -m "your message"
```

## Configuration Details

### Code Style Rules

The project enforces these key rules via `config/detekt/detekt.yml`:

**Style Rules:**

- Maximum line length: 120 characters
- Consistent function naming (camelCase)
- Kotlin code conventions compliance

**Complexity Rules:**

- Long parameter lists flagged (>10 parameters)
- Complex functions identified
- Reasonable function counts per class

**Quality Rules:**

- Empty code blocks detected
- Unused private properties flagged
- Exception handling patterns enforced

### Baseline System

The baseline file (`config/detekt/baseline.xml`) contains a list of existing code quality issues that are temporarily suppressed. This allows:

1. **Gradual Improvement**: Fix quality issues incrementally without blocking development
2. **New Issues Only**: Only new code quality violations will fail builds
3. **Baseline Updates**: When you fix existing issues, update the baseline to include those improvements

## CI Integration

Detekt runs automatically in the CI pipeline:

1. **Detekt Job**: Runs as part of the pre-commit checks in CI
2. **Dependency**: The lint job depends on Detekt completion
3. **Failure Handling**: Any Detekt violations will fail the build

## Troubleshooting

### Common Issues

**"Provided path does not exist" Error:**

- Ensure `config/detekt/detekt.yml` exists
- Check the path configuration in `build-tools/conventions/src/main/kotlin/NibelDetektPlugin.kt`

**Too Many Violations:**

- Run `./gradlew detektBaseline` to create/update the baseline
- Focus on fixing new issues rather than all existing ones

**Pre-commit Hook Failures:**

- Run `./gradlew detektFormat` to auto-fix formatting issues
- Check individual violations with `./gradlew detekt`
- Consider updating the baseline if dealing with inherited code

### Customizing Rules

To modify Detekt rules:

1. Edit `config/detekt/detekt.yml`
2. See [Detekt documentation](https://detekt.dev/) for available rules
3. Test changes locally with `./gradlew detekt`
4. Update baseline if needed with `./gradlew detektBaseline`

## IDE Integration

### IntelliJ IDEA / Android Studio

Install the Detekt plugin:

1. Go to **Settings → Plugins**
2. Search for "Detekt"
3. Install the official Detekt plugin
4. Configure it to use the project's config file: `config/detekt/detekt.yml`

This provides real-time feedback on code quality issues as you write code.

## Reports and Output

Detekt generates several types of reports in `build/reports/detekt/`:

- **HTML Report**: Human-readable report with issue details
- **XML Report**: Machine-readable format for CI integration
- **Markdown Report**: For documentation or PR comments

All reports are generated automatically when running `./gradlew detekt`.
