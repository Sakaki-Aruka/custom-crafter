# Contributing to Custom Crafter

Thank you for your interest in contributing.
Custom Crafter is a PaperMC plugin and API library that provides a flexible custom crafting system.

---

## Table of Contents

1. [Before You Start](#before-you-start)
2. [Development Setup](#development-setup)
3. [Project Structure](#project-structure)
4. [Package Guidelines](#package-guidelines)
5. [Writing Code](#writing-code)
6. [Testing](#testing)
7. [Changelog](#changelog)
8. [Pull Request Process](#pull-request-process)
9. [Reporting Bugs](#reporting-bugs)

---

## Before You Start

- Check [open issues](https://github.com/Sakaki-Aruka/custom-crafter/issues) and
  [open PRs](https://github.com/Sakaki-Aruka/custom-crafter/pulls) to avoid duplicate work.
- For significant changes (new public API, breaking changes, large refactors), open a Feature Request
  issue first and wait for feedback before writing code.
- The public API is in **beta** (`IS_BETA = true`). Breaking changes are acceptable but must be clearly
  marked and justified.

---

## Development Setup

**Requirements**

| Tool | Version |
|------|---------|
| JDK  | 21      |
| Maven| 3.9+    |
| Kotlin (via Maven plugin) | 2.3.0 |

**Clone and build**

```bash
git clone https://github.com/Sakaki-Aruka/custom-crafter.git
cd custom-crafter
mvn install -DskipTests
```

**Run tests**

```bash
# API module only (most development happens here)
mvn -pl api test

# Full build with tests
mvn test
```

Tests use [MockBukkit](https://github.com/MockBukkit/MockBukkit) (`mockbukkit-v1.21`) and JUnit 5.
A running server is not required.

---

## Project Structure

```
custom-crafter/
├── api/                    # Plugin JAR + public API library (primary module)
│   ├── src/main/kotlin/io/github/sakaki_aruka/customcrafter/
│   │   ├── event/          # Custom Bukkit events — public API surface
│   │   ├── matter/         # CMatter interfaces and implementations
│   │   ├── objects/        # Public data objects (AsyncContext, CraftView, etc.)
│   │   ├── recipe/         # CRecipe interfaces and implementations
│   │   ├── result/         # ResultSupplier types
│   │   ├── search/         # Search interfaces (PartialSearch, VanillaSearch)
│   │   ├── ui/             # UI design API (CraftUIDesigner)
│   │   ├── util/           # Utility helpers
│   │   ├── internal/       # Internal plugin logic — excluded from KDoc, not for external use
│   │   ├── CustomCrafter.kt         # JavaPlugin entry point
│   │   └── CustomCrafterAPI.kt      # API singleton
│   ├── document/           # Astro Starlight documentation site
│   └── docs/               # Generated KDoc output (do not edit manually)
├── demo/                   # Reference implementation showing API usage
└── .github/
    ├── workflows/          # CI/CD pipelines
    └── ISSUE_TEMPLATE/
```

---

## Package Guidelines

| Package prefix | Purpose | Rules |
|---|---|---|
| `event.*` | Custom Bukkit events | Add KDoc. Extend `Event` or `Cancellable`. |
| `matter.*` | Material condition types (`CMatter` + `Impl` classes) | Add KDoc on interfaces. Keep `Impl` constructors stable. |
| `objects.*` | Public data objects | Add KDoc. Keep immutable where possible. |
| `recipe.*` | Recipe types (`CRecipe` + `Impl` classes) | Add KDoc on interfaces. |
| `result.*` | Result supplier types | Add KDoc. |
| `search.*` | Search interfaces | Add KDoc. Must not reference `internal.*`. |
| `ui.*` | UI design API | Add KDoc. Must not reference `internal.*`. |
| `util.*` | Utility helpers | No KDoc required. |
| `internal.*` | Plugin internals (GUI, commands, scheduling) | Do not expose to any public package. Not documented. |

**Key rules:**
- Never reference `internal.*` from any public package.
- GUI classes (`internal.gui.*`) may not be instantiated directly by API consumers.
- New public API additions must include KDoc with `@param`, `@return`, and `@since` tags.

---

## Writing Code

### Language

- Use **Kotlin** for all new files. The entire codebase (including `CustomCrafter.kt`) is written in Kotlin.

### PaperMC / Folia compatibility

- Use **PaperMC 1.21.4+ APIs only**. Spigot/Bukkit-only APIs are not supported.
- All task scheduling must go through `FoliaLib` (`InternalAPI.foliaLib.scheduler.*`).
  Do **not** use `Bukkit.getScheduler()` or `BukkitRunnable` — these are not Folia-safe.
- Avoid blocking the main thread. Long-running work belongs in `CompletableFuture.runAsync`
  dispatched to `InternalAPI.executor` (virtual threads).

### API stability

- Adding new default methods to existing interfaces is non-breaking — preferred over adding new interfaces.
- Removing or changing method signatures is a **breaking change** and requires an `⚠️` entry in CHANGELOG.
- Deprecate before removing: annotate with `@Deprecated` and document the replacement for at least one release.

---

## Testing

- Tests live in `api/src/test/kotlin/online/aruka/customcrafter/api/`.
- Use `MockBukkit.mock()` / `MockBukkit.unmock()` in `@BeforeEach` / `@AfterEach`.
- Avoid mocking `InternalAPI.foliaLib.scheduler` or `CompletableFuture`-based async paths —
  test the synchronous logic instead, or test with synchronous stubs.
- `internal.*` classes are accessible from the test module (same Maven module, `internal` Kotlin visibility).
- Aim to add at least one test for each new public method or behavioral change.

---

## Changelog

Every user-visible change must be recorded in `CHANGELOG.md` under `[Unreleased]`.

| Prefix | When to use |
|--------|-------------|
| `✨`   | New feature or API addition |
| `🛠`   | Bug fix |
| `⚠️`   | Breaking change |

Example entry:

```markdown
## [Unreleased]
- ✨ add `CRecipe.getPriority()` to allow recipe ordering
- 🛠 fix `AllCandidateUI` pages not initializing when results exceed 45
- ⚠️ remove deprecated `ResultSupplier.getResult()` (use `asyncGetResults()`)
```

---

## Pull Request Process

1. **Fork** the repository and create a branch from `master`.
   Suggested naming: `fix/short-description`, `feature/short-description`.
2. Make your changes, add tests, and update CHANGELOG.
3. Open a PR against `master`. Fill in the PR template completely.
4. CI must pass (`plugin-build.yml` runs on PR close; run `mvn -pl api test` locally first).
5. A maintainer will review and merge. Merging to `master` triggers the automated release workflow.

**Do not open PRs against `github-pages`** — that branch is managed exclusively by CI.

---

## Reporting Bugs

Use the [Bug Report](https://github.com/Sakaki-Aruka/custom-crafter/issues/new?template=bug_report.yml)
issue template. Include:

- Server software and version (e.g., Paper 1.21.10)
- Plugin version (e.g., CustomCrafter 5.0.20)
- Reproduction steps
- Full stack trace with IP addresses masked

---

*This project is maintained by a single developer. Response times may vary.*