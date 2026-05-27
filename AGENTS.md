# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java` contains the Java source for the Spring Boot + JavaFX application (package root `com.teamgannon.trips`).
- `src/main/resources` holds FXML views, images, configuration (`application.yml`), and datasets (for example `planetsim/*.csv`).
- `files/` contains external data assets (for example `files/chview/*.CHV`).
- Build metadata lives in `pom.xml` with the Maven wrapper scripts `mvnw`/`mvnw.cmd`.

## Build, Test, and Development Commands
- `./mvnw spring-boot:run` launches the application locally (uses the JavaFX entry point).
- `./mvnw test` runs the test suite via JUnit 5 and Spring Boot test support.
- `./mvnw package` builds the runnable artifact and executes the full Maven lifecycle.

## Coding Style & Naming Conventions
- Java source uses 4-space indentation and standard Java formatting; keep diffs aligned with existing files.
- Use lower-case package names (for example `com.teamgannon.trips.routing`), UpperCamelCase for classes, and `lowerCamelCase` for methods/fields.
- Lombok is available; follow existing patterns (for example `@Slf4j`) rather than introducing new logging styles.

## Logging

Always use SLF4J parameter substitution. Never use `+` for string concatenation inside `log.*` calls.

```java
// GOOD — message is built only if the level is enabled
log.info("loaded {} stars from {}", count, datasetName);
log.error("failed to load {}", datasetName, ex);   // last arg can be a Throwable

// BAD — string is built every call, defeats lazy evaluation, allocates garbage
log.info("loaded " + count + " stars from " + datasetName);
```

Other rules:
- Use `@Slf4j` (Lombok) to get the `log` field. Do not declare your own logger.
- Replace `e.printStackTrace()` with `log.error("context message", e)`.
- Do not use `System.out` / `System.err` from production code — route everything through SLF4J. (See `tripsapplication/src/main/java/com/teamgannon/trips` for examples; bootstrap paths are an exception only because the logger isn't yet initialized.)
- Pick the right level: `trace` (fine-grained dev tracing), `debug` (occasional diagnostics, off by default), `info` (lifecycle events, dataset switches), `warn` (recoverable issue), `error` (action failed; user-visible).

The rule is enforced by `maven-checkstyle-plugin` (config at `tripsapplication/config/checkstyle/checkstyle.xml`), bound to the `validate` phase — any string-concat in a `log.*` call fails the build before compilation. `scripts/check-logging.sh` runs the same pattern out-of-band when you want a faster read without invoking Maven.

## Testing Guidelines
- Testing uses JUnit 5 (`spring-boot-starter-test` and `junit-jupiter-api`).
- Place new tests under `src/test/java` and mirror the production package structure.
- No explicit coverage thresholds are configured; add focused tests for new behavior and edge cases.

## Commit & Pull Request Guidelines
- Recent commits use short, plain-language messages (for example “moved statusbar to a separate file”). Follow that style unless the team specifies otherwise.
- PRs should include a concise summary, testing notes (`./mvnw test` or “not run”), and screenshots for UI changes in JavaFX/FXML.

## Configuration & Data Notes
- Application settings live in `src/main/resources/application.yml`.
- Data imports/exports and sample datasets live under `src/main/resources` and `files/`; keep formats consistent when adding new files.
