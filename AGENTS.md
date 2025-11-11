# Repository Guidelines

## Project Structure & Module Organization
- Root: Gradle multi-module Spring Boot (Java 17, WebFlux).
- App code: `src/main/java/...`; tests: `src/test/java/...`.
- Modules: `openai/` and `claude/` (each with their own `src/main|test/java`).
- Utilities/examples: `frontend-demo/`, `frontend-sse-example.js`, `http/` (HTTP request samples).

## Build, Test, and Development Commands
- `./gradlew bootRun`: start the application locally.
- `./gradlew build`: compile, run tests, and assemble artifacts.
- `./gradlew test`: run JUnit tests (all modules).
- `./gradlew :openai:build :claude:build`: build specific modules.
- `rg "pattern"`: fast search across the repo.

## Coding Style & Naming Conventions
- Java: standard conventions (4-space indent, class `PascalCase`, methods/fields `camelCase`, packages lowercase).
- Imports: keep ordered and remove unused (IDE Optimize Imports).
- Lombok is used; prefer Lombok annotations over boilerplate where present.
- Markdown/JSON/TOML: 2-space indent; ~100-char lines; small, focused diffs.
- Filenames: Java match public class; scripts/docs use kebab-case.

## Testing Guidelines
- Frameworks: JUnit 5, Reactor Test.
- Location/naming: `src/test/java`, classes end with `*Test`.
- Run: `./gradlew test -q` or module-scoped (e.g., `./gradlew :claude:test`).
- Coverage: aim for >80% on new/changed code; mirror package layout.

## Commit & Pull Request Guidelines
- Commits: Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:`, `refactor:`, `test:`). Keep atomic; reference issues (e.g., `#123`).
- PRs: clear description, linked issues, validation steps (`./gradlew build`), and notes on config/data changes. Add screenshots for API docs/UI if relevant.

## Security & Configuration Tips
- Never commit secrets or real API keys. Use environment variables or profile-specific `application-*.yml` kept out of VCS.
- Scrub sample requests in `http/` before sharing.
- Add bulky/ephemeral artifacts to `.gitignore` when appropriate.

## Agent-Specific Instructions
- Prefer read-only operations; request approval for writes/deletions.
- Avoid persisting sensitive outputs; use sanitized examples in docs.
- Document any automation scripts and how to run them.

