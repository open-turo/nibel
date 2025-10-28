# Task Completion Checklist

## Mandatory Steps (MUST BE DONE)

### Code Quality Verification ✅

1. **Run verification**: `./gradlew check` - Must pass completely
2. **Fix lint issues**: `./gradlew lintFix` - Apply automatic fixes
3. **Verify tests**: `./gradlew test` - All tests must pass
4. **Pre-commit hooks**: `pre-commit run --all-files` - Must pass

### Build Verification ✅

5. **Full build**: `./gradlew build` - Complete build success required
6. **Sample app**: `./gradlew :sample:app:build` - Test integration in context
7. **Clean build**: `./gradlew clean build` - Ensure no stale artifacts

## For Library Changes ✅

8. **Local publishing**: `./gradlew publishToMavenLocal` - Test library changes locally

## Documentation & Testing (When Applicable) ✅

9. **Update KDoc** for new public APIs
10. **Add unit tests** for new functionality
11. **Add integration tests** for new navigation patterns
12. **Update README.md** if public interface changes

## Git Best Practices ✅

13. **Conventional commits**: Use `feat:`, `fix:`, `docs:`, `refactor:` prefixes
14. **Branch hygiene**: Keep feature branches focused and small
15. **Rebase against main** before creating PR

## Performance & Integration ✅

16. **Run detekt**: `./gradlew detekt` - Static analysis must pass
17. **Verify sample works**: Test actual navigation flows in sample app
18. **Check multi-module**: Ensure cross-module navigation still works

## Critical Rule

**NEVER consider a task complete without running `./gradlew check` successfully!**

This ensures:

- Code compiles across all modules
- All tests pass
- Linting rules are satisfied
- Generated code is valid
- Integration tests pass
