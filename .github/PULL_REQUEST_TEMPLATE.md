## Related Issue
<!-- Link the issue this PR resolves, e.g. "Closes #123". If there is no issue, briefly explain why. -->
Closes #

## Description
<!-- Describe what this PR changes and why. Keep it concise. -->

## Type of Change
<!-- Check all that apply. -->
- [ ] Bug fix (non-breaking)
- [ ] New feature (non-breaking)
- [ ] Breaking change (`⚠️` entry required in CHANGELOG)
- [ ] Internal refactor (no public API impact)
- [ ] Documentation / KDoc
- [ ] CI / Build

## Checklist
<!-- Items marked [N/A] can be left unchecked with a note. -->

### Code
- [ ] Changes do not use Spigot/Bukkit-only APIs — PaperMC 1.21.4+ APIs only
- [ ] Async operations are Folia-compatible (`FoliaLib` scheduler, no bare `Bukkit.getScheduler()`)
- [ ] New public API members (`api.interfaces.*`, `api.objects.*`, `api.event.*`) have KDoc comments
- [ ] Internal implementation details are placed under `internal.*` or `impl.*`, not in `api.*`

### Tests
- [ ] Tests added or updated in `api/src/test/`
- [ ] All tests pass locally (`mvn -pl api test`)

### Documentation
- [ ] CHANGELOG.md updated under the `[Unreleased]` section using the correct emoji prefix:
  - `✨` for new features
  - `🛠` for bug fixes
  - `⚠️` for breaking changes
- [ ] Starlight document site (`api/document/`) updated if user-facing behavior changed
- [ ] KDoc updated if public API signatures changed

## Notes for Reviewer
<!-- Anything that needs special attention, known limitations, or follow-up items. -->