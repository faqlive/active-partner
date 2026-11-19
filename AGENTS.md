# AGENTS.md

Guidance for coding agents working in this repository.

## Project Snapshot

- Build tool using Maven Wrapper (`./mvnw`, `mvnw.cmd`) with Maven 3.8.5.
- Use JDK 17 for local development. The Maven compiler is configured with Java 11 source and target levels.
- UI stack uses JavaFX 17.0.6 and FXML.
- Module system: enabled via `src/main/java/module-info.java`.
- Current package root: `org.example.activepartner`.
- Test framework: JUnit 5 (`junit-jupiter-api`, `junit-jupiter-engine`).
- Lint/format plugins: not explicitly configured in `pom.xml`.

## Rules Files Discovered

- `.cursorrules`: not found.
- `.cursor/rules/`: not found.
- `.github/copilot-instructions.md`: not found.
- Because no Cursor/Copilot rule files exist, follow this `AGENTS.md` plus existing repository patterns.

## Directory Reference

- The Maven JavaFX plugin launches `src/main/java/org/example/activepartner/HelloApplication.java`.
- The packaged JAR uses `src/main/java/org/example/activepartner/Main.java` as its launcher.
- The main FXML controller is `src/main/java/org/example/activepartner/ui/MainLayoutController.java`.
- The main FXML layout is `src/main/resources/org/example/activepartner/main-layout.fxml`.
- Java module declaration: `src/main/java/module-info.java`.
- Build config: `pom.xml`.

## Build / Run / Test Commands

- Compile only: `./mvnw clean compile`
- Package artifact: `./mvnw clean package`
- Run unit tests: `./mvnw test`
- Clean + full verify lifecycle: `./mvnw clean verify`
- Run JavaFX app (plugin): `./mvnw clean javafx:run`
- Windows command equivalent: replace `./mvnw` with `mvnw.cmd`

## Running a Single Test (Important)

- Single test class:
  - `./mvnw -Dtest=MyServiceTest test`
- Single test method:
  - `./mvnw -Dtest=MyServiceTest#handlesInvalidInput test`
- Multiple tests (comma-separated):
  - `./mvnw -Dtest=FooTest,BarTest test`
- Run by wildcard pattern:
  - `./mvnw -Dtest='*ControllerTest' test`

Notes:
- This uses Maven Surefire's `-Dtest=...` selector.
- Quote wildcard expressions in shells that expand globs.

## Linting and Formatting Status

- No dedicated lint command is currently configured.
- No Checkstyle/SpotBugs/PMD/formatter plugin is configured in `pom.xml`.
- Agent behavior in this repo should be:
  - Keep formatting consistent with surrounding files.
  - Use IDE formatter defaults only when a file is already consistently formatted.
  - Avoid broad reformat-only diffs unless explicitly requested.

## Coding Style Guidelines

These are the expected conventions for new/updated code.

### Imports

- Group imports as:
  1) `java.*` / `javax.*`
  2) third-party imports
  3) project-local imports
- Keep imports explicit; do not use wildcard imports.
- Remove unused imports before finalizing changes.
- In JavaFX controllers, import `javafx.fxml.FXML` for injected fields/handlers.

### Formatting

- Use 4-space indentation; no tabs.
- Keep one top-level public type per file.
- Keep line length readable (target around 100-120 chars max).
- Preserve existing brace style (K&R / same-line opening brace).
- Keep methods small and intention-revealing.
- Separate logical blocks with a single blank line.

### Types and APIs

- Prefer concrete, descriptive types over `var` in non-obvious contexts.
- Use interfaces for fields/parameters where practical; concrete types for construction.
- Avoid raw types; always use generics.
- Prefer immutable fields (`final`) where mutation is not required.
- Keep JavaFX UI references (`@FXML` fields) `private` unless framework constraints require otherwise.

### Naming Conventions

- Classes: `PascalCase` (`PartnerDashboardController`).
- Methods/fields/local variables: `camelCase`.
- Constants: `UPPER_SNAKE_CASE` (`DEFAULT_TIMEOUT_MS`).
- Test classes: `<ClassUnderTest>Test`.
- Test methods: behavior-focused names (`returnsEmptyStateWhenNoData`).
- Name FXML files in kebab-case, following `main-layout.fxml`.

### Error Handling

- Do not swallow exceptions.
- Catch the most specific exception type possible.
- Add contextual messages when rethrowing.
- For recoverable UI errors, surface a user-facing message and log technical details.
- For initialization failures (e.g., FXML load issues), fail fast with a clear exception path.

### JavaFX / FXML Conventions

- Keep business logic out of `Application` bootstrap class.
- Keep controllers focused on view wiring and user interaction handling.
- Use `@FXML` only for elements/handlers referenced by FXML.
- Match `fx:id` names with controller field names exactly.
- Keep FXML hierarchy readable; avoid deeply nested containers when unnecessary.

### Module System (`module-info.java`)

- Add `requires ...` entries for new module dependencies.
- Add `opens <pkg> to javafx.fxml;` for FXML-injected packages.
- Add `exports <pkg>;` only for packages that must be public API.
- Keep module declarations minimal; remove unused `requires` where safe.

### Testing Expectations

- Prefer deterministic unit tests (no network, time, or filesystem flakiness unless mocked).
- One behavioral concern per test method.
- Arrange-Act-Assert structure is preferred.
- Use descriptive assertions with failure messages when intent is not obvious.
- Add/adjust tests for any non-trivial behavior changes.

## Dependency and Build File Changes

- When editing `pom.xml`:
  - Keep dependency versions explicit unless using a BOM.
  - Preserve existing plugin structure unless migration is intentional.
  - Avoid mixing formatting styles in one change.
- If adding a new library that is JavaFX-related, check whether `module-info.java` also needs updates.

## Agent Workflow Recommendations

- Before edits, inspect neighboring files for local style patterns.
- Make the smallest safe change that satisfies the request.
- After edits, run the narrowest validating command first (single test), then broader checks.
- Recommended validation sequence:
  1) `./mvnw -Dtest=<ChangedClassTest> test` (if tests exist)
  2) `./mvnw test`
  3) `./mvnw clean verify`
- If no tests exist for changed behavior, add targeted tests where practical.

## When Unsure

- Prefer consistency with existing code over introducing new architectural patterns.
- Document assumptions in PR/commit messages when behavior is inferred.
- Ask for clarification only when a decision changes behavior materially.
