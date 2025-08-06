# Release Process

This document describes the automated release process for Nibel, implemented as part of [#11](https://github.com/open-turo/nibel/issues/11).

## Overview

The release process is fully automated via GitHub Actions and uses semantic versioning. Releases are triggered automatically when code is pushed to the `main` branch.

## Release Workflow

The release workflow (`.github/workflows/release.yaml`) consists of three jobs:

1. **Lint**: Code quality checks using `open-turo/actions-jvm/lint@v1`
2. **Test**: Unit tests with proper Android and JDK setup
3. **Release**: Semantic release and Maven Central publishing (only runs if lint and test pass)

## Configuration Files

### Semantic Release

- `.releaserc.json`: Extends `@open-turo/semantic-release-config/lib/gradle`
- Handles automatic version bumping and changelog generation

### Gradle Configuration

- `gradle.properties`: Contains `mavenCentralAutomaticPublishing=true` for streamlined publishing
- Maven publishing is configured via `NibelMavenPublishPlugin.kt`

## Version Management

- Versions are managed automatically by semantic-release
- The current version in `gradle.properties` serves as a fallback
- Release versions are determined by conventional commit messages:
  - `feat:` → minor version bump
  - `fix:` → patch version bump
  - `BREAKING CHANGE:` → major version bump

## Manual Release (if needed)

To trigger a manual release:

1. Push to the `main` branch with appropriate conventional commit messages
2. The GitHub Action will automatically handle the rest

## Troubleshooting

### Release Fails

- Check that all required secrets are configured
- Verify that the GPG key is valid and properly formatted
- Ensure Maven Central credentials are user tokens, not login credentials

### Version Not Updated

- Verify semantic-release configuration in `.releaserc.json`
- Check that commit messages follow conventional commit format
- Review the GitHub Actions logs for semantic-release output

## Dependencies

This release process depends on:

- `@open-turo/semantic-release-config` for semantic release configuration
- `com.vanniktech.maven.publish` plugin for Maven Central publishing
- `open-turo/actions-jvm` for CI/CD actions
