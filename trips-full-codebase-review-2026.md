# TRIPS Codebase Review

**Date**: 2026 (initial pass: Grok 4.3 — review and remediation; second pass: Claude Opus 4.7, 2026-05-26)
**Repository**: /Users/larrymitchell/tripsnew/trips (branch: master)
**Scope**: Full codebase review (~1,135 Java source files, ~158 test files at second pass)
**Focus**: Architecture, code quality, maintainability, adherence to project guidelines (AGENTS.md, CLAUDE.md), Java 25 + Spring Boot 4 + JavaFX 25 specifics, recent additions (spaceshipmodeller, fleet modelling, transfer planner).

---

## Summary

TRIPS (Terran Republic Interstellar Plotting System) is a sophisticated, long-lived Spring Boot + JavaFX desktop application for 3D stellar cartography, route planning, solar system visualization, and sci-fi world-building. The codebase demonstrates strong engineering discipline, particularly in its 3D rendering pipeline, complex astronomical calculations, and recent adoption of modern patterns for new subsystems (spaceship modeling, procedural planets).

The application successfully integrates:
- Spring Boot 4.0.2 with FxWeaver for dependency-injected JavaFX controllers
- Complex 3D scene graph management with billboard labels (carefully following "two-step coordinate transform" lessons)
- Graph-based routing (JGraphT + KD-Tree optimizations for large datasets)
- Orbital mechanics (Orekit dependency present, custom Kepler sampling in use)
- JPA/Hibernate with H2 embedded DB and batch loading for large catalogs
- Extensive sci-fi features (polities, procedural planet generation via accretion models, spaceship design with mass budgets and transfer planning)

**Overall Assessment**: The codebase is in good health. Core visualization and data layers are mature and incorporate hard-won lessons (documented extensively in CLAUDE.md). Recent feature work (spaceshipmodeller, Caine Riordan fleet additions) follows existing architectural patterns well.

**Dominant Risk Areas** (after first-pass remediation):
1. **Bulk-import session hygiene & schema-migration story** — `ddl-auto: update` with no migration tool and no flush/clear in the import path. Both will bite at the 2M-star scale.
2. **FX-thread blocking on JPA queries** in solar-system jump-into and refresh paths.
3. **Mutable global state** (`TripsContext`) injected into ~113 sites with no synchronization.
4. **God-class growth** in renderers, workbench, and the procedural-planet dialog (several files >1,500 lines).
5. **Event-bus used as synchronous RPC** — chains of listeners that publish follow-up events instead of calling methods directly.
6. **Unit ambiguity on `StarObject.mass`** — patched in the transfer calculator but the underlying entity still stores heterogeneous units.

No critical runtime bugs or security vulnerabilities found in core paths. Test coverage is above average for a UI-heavy scientific application but has hard gaps in `service/importservices/`, `dialogs/`, and `graphics/panes/`.

---

## Strengths

- **3D Label Billboard Implementation** (`StarLabelManager`, `SolarSystemLabelManager`): Excellent depth-sorted collision detection, throttling, NaN/visibility clipping, font scaling with camera Z, and correct two-step coordinate transforms. Matches "lessons learned" in CLAUDE.md precisely.
- **Performance-conscious routing**: KD-Tree graph building (O(n log n)) for large datasets in `LargeGraphSearchTask`; concurrent transit computation; proper use of JavaFX `Service`/`Task` for long-running work.
- **Domain modeling**: Rich `StarObject`, `SolarSystem`, `ExoPlanet` entities with proper embedded components, indexes, and relationships. New `SpaceshipEntity` follows the same conventions with excellent Javadoc.
- **Event-driven decoupling**: Heavy, correct use of `ApplicationEventPublisher` and `@EventListener` for cross-component communication (avoids tight coupling between UI and services).
- **Test discipline in complex areas**: Dedicated tests for procedural planet generation (biome, tectonics, elevation, erosion), routing automation, spaceship integration/transfer feasibility, and repository integration. Many benchmarks (JMH) for hot paths.
- **Documentation of pitfalls**: `CLAUDE.md` is exceptional — captures real lessons about JavaFX transform order, radial vs. per-axis scaling, label updates, etc. Rare in open projects.
- **Modern service patterns**: `@Transactional` on write paths in `SolarSystemService` and new spaceship services; constructor injection; proper async handling.
- **Spaceshipmodeller internal layering**: 19 phases of additions without rot inside the module — domain → entity → service → UI stays clean.
- **First-pass remediation actually landed**: commit `02716ae2 address codebase review findings` closed Issues 1-9 (see Status fields below).

---

## First-Pass Issues (2026 — Grok 4.3)

### Issue 1 -- Severity: bug
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/service/graphsearch/task/LargeGraphSearchTask.java:139
- **Description**: Debug statement `kShortestPaths.forEach(System.out::println);` executes in production code during long route searches. Pollutes stdout and leaks internal graph data.
- **Suggestion**: Remove or guard behind a debug flag / SLF4J trace level. Replace with proper `log.debug(...)`.
- **Status**: done -- removed production stdout and moved route output behind parameterized SLF4J debug logging.

### Issue 2 -- Severity: bug
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/service/graphsearch/LargeGraphSearchService.java:60,73,76,82,90,92
- **Description**: Multiple instances of incomplete placeholder strings ("graph search name TBD") concatenated into log messages and status events. Also broken string concat in log.warn (missing space/operator).
- **Suggestion**: Replace placeholders with actual route/search identifiers or remove the phrases. Use parameterized logging (`log.warn("cancelling graph search for {}", name)`).
- **Status**: done -- replaced placeholder search labels with origin/destination descriptions and parameterized logging.

### Issue 3 -- Severity: suggestion
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/TripsSpringBootApplication.java:36-39 (and similar in ReportBundleService, application.yml)
- **Description**: Hardcoded macOS-specific paths (`/Library/Application Support/TRIPS`) in bootstrap and configuration. Application will misbehave or fail to find data on Windows/Linux when launched in certain ways.
- **Suggestion**: Use a cross-platform app data directory strategy (e.g., `java.nio.file.Paths.get(System.getProperty("user.home"), osSpecificPath(...))` or a small utility like `AppDirs` / `Directories` library). Centralize all such paths.
- **Status**: done -- centralized app data, program data, script, report, and log locations behind `TripsApplicationPaths`.

### Issue 4 -- Severity: suggestion
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:57-61 (and other AWT imports)
- **Description**: Mixing `java.awt.*` (SystemTray, PopupMenu, TrayIcon, Toolkit) with JavaFX. Known source of threading and event dispatch issues, especially on macOS.
- **Suggestion**: Replace SystemTray integration with JavaFX-native solutions (e.g., `javafx.stage.Popup` + custom tray handling via 3rd-party libs like `dorkbox/SystemTray` or `AWT-free` approaches). At minimum, isolate AWT usage and ensure proper EDT/FX thread bridging.
- **Status**: done -- AWT tray integration is isolated in `AwtSystemTrayService`; a fully AWT-free tray replacement remains optional future work.

### Issue 5 -- Severity: suggestion
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/javafxsupport/TripsFxApplication.java:42-45 (and similar in MainPane, PrimaryStageInitializer)
- **Description**: Global `Thread.setDefaultUncaughtExceptionHandler` only prints to stderr. Fatal errors during startup (e.g., in `PrimaryStageInitializer`) call `System.exit(1)` after showing a basic Alert.
- **Suggestion**: Implement a proper uncaught exception handler that logs to SLF4J + shows user-friendly error dialog with "Report Problem" integration (the app already has a problem report feature). Avoid `System.exit` in favor of graceful `Platform.exit()` + context close where possible.
- **Status**: done -- uncaught exceptions now log through SLF4J, create a local pending problem-report ZIP when possible, and startup failure exits through JavaFX shutdown instead of direct `System.exit(1)`.

### Issue 6 -- Severity: suggestion
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/ExoPlanet.java:24-70 (Javadoc block)
- **Description**: The class Javadoc is a verbatim copy-paste of the exoplanet.eu CSV format description instead of proper entity documentation.
- **Suggestion**: Replace with concise entity purpose, key relationships (`solarSystemId`, `hostStarId`, `parentPlanetId`), and status/usage notes.
- **Status**: done -- replaced the copied CSV-schema prose with concise entity documentation.

### Issue 7 -- Severity: suggestion
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/spaceshipmodeller/persistence/SpaceshipDesignMapper.java and related Jackson usage across ~14 files
- **Description**: Active migration to Spring Boot 4 / Jackson 3 (`tools.jackson.*`) was incomplete; new spaceshipmodeller code still used `com.fasterxml.jackson`.
- **Suggestion**: Complete the Jackson 3 migration (or document the decision to stay on Jackson 2 compatibility for now). Update the migration plan status.
- **Status**: done -- application and affected test code now use `tools.jackson.*`; Jackson annotations intentionally remain under `com.fasterxml.jackson.annotation`.

### Issue 8 -- Severity: nit
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/service/graphsearch/task/LargeGraphSearchTask.java:64
- **Description**: `collisionSet = ConcurrentHashMap.newKeySet(collisionMap.size());` where `collisionMap` is a freshly created empty `ConcurrentHashMap`. Size is 0.
- **Suggestion**: Simplify to `ConcurrentHashMap.newKeySet()` (no-arg).
- **Status**: done -- simplified to `ConcurrentHashMap.newKeySet()`.

### Issue 9 -- Severity: nit
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java (and other large orchestrators)
- **Description**: `MainPane` (~781 lines) acts as a central hub with many responsibilities.
- **Suggestion**: Continue the existing split (see `MainSplitPaneManager`, sub-controllers) and extract more focused coordinators for menu vs. visualization concerns.
- **Status**: follow-up -- system tray startup was extracted; further decomposition should target dialog orchestration and busy-state handling in separate coordinators. **Tracked in second-pass Issue 18 (god classes).**

### Issue 10 -- Severity: nit
- **File**: Multiple (e.g., `LargeGraphSearchService`, import tasks, UI panels)
- **Description**: Inconsistent string formatting and log message style.
- **Suggestion**: Standardize on SLF4J parameterized logging (`log.info("Loaded {} stars", count)`) everywhere.
- **Status**: partial -- touched reviewed graph-search and startup/logging paths; **repo-wide cleanup tracked in second-pass Issue 41.**

---

## First-Pass Remediation Pass Completed

The first remediation pass addressed Issues 1-10 in application-owned code:

- Completed the Jackson 3 migration for application and affected test sources by moving core/databind usage to `tools.jackson.*`; Jackson annotations intentionally remain under `com.fasterxml.jackson.annotation`.
- Added `TripsApplicationPaths` and wired startup, `application.yml`, logback, reports, and scripting to centralized cross-platform paths.
- Cleaned reviewed graph-search debug output, placeholder labels, interruption handling, and `ConcurrentHashMap.newKeySet()` initialization.
- Replaced obvious production `System.out` / `printStackTrace` diagnostics in the reviewed app-owned paths with SLF4J logging.
- Added SLF4J-backed uncaught exception handling and replaced startup failure `System.exit(1)` with JavaFX shutdown.
- Isolated native AWT tray usage behind `AwtSystemTrayService`; `MainPane` no longer imports or owns `java.awt.*` tray code.
- Replaced the copied exoplanet CSV schema prose in `ExoPlanet` with concise entity documentation.
- Updated `SPRING_BOOT_4_MIGRATION_PLAN.md` to show the Jackson migration as complete.

Verification:
- `./mvnw-java25.sh -q -pl tripsapplication -DskipTests compile` passed.
- Focused Jackson persistence tests passed: `DataSetDescriptorSerializationServiceTest`, `ProceduralPlanetPersistenceHelperTest`, `SpaceshipJsonServiceTest`, `SpaceshipDesignMapperTest`, `TransferPlanMapperTest`.
- Full `./mvnw-java25.sh -q -pl tripsapplication test` executed 2,711 tests with 0 assertion failures and 4 Testcontainers errors caused by Docker being unavailable in the sandbox.

## Follow-On Remediation: Local Crash Report Bundles

The next remediation slice completed the remaining problem-report integration from Issue 5:

- Added `ProblemReportService.createCrashReport(Throwable)` to create a local pending diagnostic bundle without prompting for registration or attempting upload.
- Wired `TripsFxApplication` uncaught-exception handling to call the existing problem-report service and include the generated ZIP path in the user-facing error dialog.
- Made crash report creation tolerant of unavailable registration storage and unavailable OSHI system measurements.
- Gave `ReportBundleService` a direct default for `problemreport.logTailLines` so manually constructed service tests preserve the production default.
- Added `ProblemReportServiceTest`, which verifies that a crash creates a pending ZIP containing `report.json`, `system.json`, and `log_tail.txt`.

Verification:
- `./mvnw-java25.sh -q -pl tripsapplication -DskipTests compile` passed.
- `./mvnw-java25.sh -q -pl tripsapplication -Dtest='ProblemReportServiceTest' test` passed.

---

# Second Review Pass — 2026-05-26 (Claude Opus 4.7)

**Methodology**: Six parallel `Explore` sub-agents, each briefed not to re-report items in Issues 1-10. Concern areas: architecture & coupling, data model & JPA persistence, JavaFX concurrency & threading, UI/UX/accessibility, performance & memory, and code quality/hygiene. Synthesis and ordering by the main agent.

**Headline**: First-pass remediation holds up. The new pass surfaces **47 additional findings** weighted toward:
- (a) FX-thread blocking on JPA queries in solar-system flows
- (b) absent schema-migration story under `ddl-auto: update`
- (c) bulk-import session hygiene & batch-size mismatch
- (d) god-class growth in renderers / workbench / procedural-planet dialog
- (e) an event bus that is increasingly used as synchronous RPC
- (f) unit ambiguity on `StarObject.mass` that was only patched at one call site

None are blocker-grade. Several are silent correctness bugs that will keep biting.

---

## Second-Pass Issues

### Issue 11 -- Severity: bug (critical)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/SolarSystemSpacePane.java:327-332 ; also 699-707 (`refreshCurrentSystem`)
- **Description**: `setSystemToDisplay()` and `refreshCurrentSystem()` call `solarSystemService.getSolarSystem()` synchronously on the JavaFX Application Thread. That method runs `exoPlanetRepository.findBySolarSystemId()` and `featureRepository.findBySolarSystemId()` (`SolarSystemService.java:85,89`). The UI freezes during "Jump Into…" while DB I/O completes.
- **Suggestion**: Wrap the load in `javafx.concurrent.Task<SolarSystemDescription>`; show a transient progress indicator; render on the `setOnSucceeded` callback.
- **Status**: done -- `SolarSystemSpacePane` now loads systems in a daemon `Task`, shows a transient loading indicator, cancels/ignores stale loads, and renders only from the success callback on the FX thread.

### Issue 12 -- Severity: bug (critical)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/service/StarService.java (`starBulkSave`) ; tripsapplication/src/main/java/com/teamgannon/trips/file/csvin/RegularStarCatalogCsvReader.java:38,94 ; tripsapplication/src/main/resources/application.yml:31
- **Description**: `starBulkSave()` calls `saveAll()` with no `flush()` + `clear()` between batches. Parser uses `BATCH_SIZE = 5000` while `hibernate.jdbc.batch_size: 50` is 100× smaller. For a 2M-star catalog the first-level cache retains every persisted entity; RAM grows linearly with the import.
- **Suggestion**: Bump `hibernate.jdbc.batch_size` to match the parse batch (e.g., 1,000-5,000); add `entityManager.flush(); entityManager.clear()` after each batch save inside a dedicated `@Transactional` method; consider Spring Batch for very large catalogs.
- **Status**: done -- `hibernate.jdbc.batch_size` now aligns with the 5,000-row parser batch and `StarService.starBulkSave()` flushes/clears the persistence context after each batch.

### Issue 13 -- Severity: bug (critical)
- **File**: tripsapplication/src/main/resources/application.yml (Hibernate `ddl-auto: update`)
- **Description**: No migration tool (Flyway/Liquibase). Per project memory, phase 18 abandoned the `series` column on the spaceship entity and phase 12 added `availablePropellantTons` with no backfill — `ddl-auto: update` adds columns but never drops or backfills. Schema drift accumulates silently; old rows lie about their feasibility status.
- **Suggestion**: Adopt Flyway. Baseline the current schema. Add migrations for every entity addition going forward. Switch `ddl-auto` to `validate` in non-dev profiles. Write a one-time cleanup migration to drop confirmed-orphan columns.
- **Status**: open

### Issue 14 -- Severity: bug (high)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/config/application/TripsContext.java:74,118-127 (and ~113 injection sites)
- **Description**: Singleton holding mutable state — `searchContext`, `currentPlot`, `appViewPreferences`, `constellationMap` (HashMap). Mutated from `@EventListener` methods that Spring delivers on the publisher's thread (which can be a background `Task`). No `volatile`, no `synchronized`. Races on dataset-context switches are possible.
- **Suggestion**: Either (a) wrap mutable fields in `AtomicReference` snapshots / copy-on-write, or (b) split into typed read-only services + a single mutator service guarded by a lock. Document threading invariants on each field.
- **Status**: open

### Issue 15 -- Severity: bug (high)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/StarObject.java:153-154 ; tripsapplication/src/main/java/com/teamgannon/trips/spaceshipmodeller/integration/TransferCalculator.java (`toSolarMasses`)
- **Description**: `StarObject.mass` is documented as solar masses but accrete-generated systems store kg values. Phase 13 patched the *transfer calculator* with a heuristic (`>1000` ⇒ kg). Anything else reading `mass` (gravity calculations, stellar evolution, exports, future Orekit propagation) silently inherits the bug.
- **Suggestion**: One-time data migration to normalize all stored masses to solar masses + an import validator. If heterogeneous units must be supported, add a `massUnit` enum column and stop relying on a magnitude heuristic.
- **Status**: done -- `V2__normalize_star_obj_mass_to_solar.sql` normalizes legacy rows, `StarMassNormalizer` now guards ingestion boundaries, and the transfer calculator no longer owns a unit heuristic.

### Issue 16 -- Severity: suggestion (critical hygiene)
- **File**: repository root: `HYG-MERGED-2M-TRIPS-11012026202505.csv` (878 MB) ; `exoplanet.eu_catalog_13-01-26_15_50_52.csv` (2.8 MB) ; `30ly.trips.csv` (337 KB) ; `30ly.trips.csv.zip` (107 KB)
- **Description**: Massive CSVs checked into git. Inflates clones, bloats history forever, and risks accidental wholesale loads.
- **Suggestion**: Add to `.gitignore`; move sample dataset to a download script with checksum; consider `git filter-repo` to scrub the 878 MB file from history. Document in CLAUDE.md where catalogs live.
- **Status**: open

### Issue 17 -- Severity: suggestion (architecture)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/{solarsystem,solarsysmodelling,planetary,planetarymodelling} (also `solarsystem/sol/`)
- **Description**: Four parallel packages whose responsibilities overlap and whose names are easily confused. Cross-imports include `SolarSystemRenderer` ← `planetarymodelling.PlanetDescription` and `PlanetarySkyRenderer` ← `solarsysmodelling.accrete.PlanetTypeEnum` — bidirectional coupling. New developers can't infer which package owns business logic vs presentation.
- **Suggestion**: Pick a convention (`*modelling` = algorithms; `*` = rendering/UI) and consolidate. Introduce a neutral `model/` package containing shared specs (PlanetSpec, StarSpec) that UI and modelling both depend on.
- **Status**: partial (Phase 5.1) — three of the four renames applied: `solarsysmodelling` → `solarsystem.modelling`, `planetarymodelling` → `planetary.modelling`, `solarsystem/sol/` collapsed into `solarsystem/`. The neutral `model/` package is still open — those shared specs (PlanetSpec, StarSpec) don't exist yet; introducing them is a separate design pass.

### Issue 18 -- Severity: suggestion (architecture)
- **File**: multiple — top offenders by line count:
  - `solarsystem/rendering/SolarSystemRenderer.java` — 2,004 lines
  - `dialogs/solarsystem/ProceduralPlanetViewerDialog.java` — 1,936 lines (mixes generation logic + UI + I/O)
  - `workbench/service/WorkbenchEnrichmentService.java` — 1,832 lines
  - `workbench/DataWorkbenchController.java` — 1,706 lines
  - `planetary/modelling/procedural/JavaFxPlanetMeshConverter.java` — 1,644 lines
  - `service/SolPlanetsInitializer.java` — 1,410 lines
  - `planetary/rendering/PlanetarySkyRenderer.java` — 1,204 lines
  - `service/SolarSystemService.java` — 793 lines (generate, save, export, import all in one)
  - `controller/MainSplitPaneManager.java` — 670 lines (no dedicated test)
- **Description**: Each does too much; testing & refactor become high-risk. `SolPlanetsInitializer` is the worst architecturally: it treats Sol as a magical special case bypassing the procedural pipeline.
- **Suggestion**: Decompose one per sprint (see Remediation Plan §4). For Sol specifically, unify with procedural generation via an `ISolarSystemFactory` so it follows the same lifecycle as everything else.
- **Status**: open

### Issue 19 -- Severity: suggestion (architecture)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/solarsystem/splitting/RouteEventHandler.java:80-108 ; tripsapplication/src/main/java/com/teamgannon/trips/dataset/* ; tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java (event listeners)
- **Description**: 64 components inject `ApplicationEventPublisher`. Event listeners frequently publish follow-up events (e.g., `NewRouteEvent` → `StatusUpdateEvent` → `BusyStateEvent`) creating synchronous RPC chains routed through the bus. Ordering and failure semantics become opaque. Grep suggests some events have no live listeners (e.g., `ShowStellarDataEvent` after refactors).
- **Suggestion**: Audit the event graph. For chains that are really "do A then B", promote to direct service calls. Keep the bus for genuinely-broadcast UI updates (status, palette, dataset switch). Optionally add a startup validator that fails fast on events with zero listeners.
- **Status**: open

### Issue 20 -- Severity: suggestion (architecture)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/SolarSystemSpacePane.java (constructor — 8 args per phase 8 memory)
- **Description**: The spaceshipmodeller module's intended boundary is slipping. `SolarSystemSpacePane` now directly injects `SpaceshipService`, `TransferPlannerBridge`, and `TransferPlannerLauncher`. The module is no longer self-contained — the core view code depends on a feature module.
- **Suggestion**: Invert: have spaceshipmodeller listen for a `TransferTargetRequestedEvent` published by `SolarSystemSpacePane`, and publish `TransferPlanCreatedEvent` / `ShowTransferTrajectoryEvent` back. Remove the direct service refs from the pane constructor.
- **Status**: open

### Issue 21 -- Severity: suggestion (architecture)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/StarEditDialog.java ; tripsapplication/src/main/java/com/teamgannon/trips/nebula/dialogs/NebulaEditorDialog.java ; vs FxWeaver-loaded controllers elsewhere
- **Description**: Most controllers load via FxWeaver. A handful of dialogs still call `new FXMLLoader()` directly. Some controllers are Spring-managed, some aren't — inconsistent injection surface.
- **Suggestion**: Pick one convention. Either route every FXML through FxWeaver, or define a `DialogFactory` that wraps both styles. Add a checkstyle/ArchUnit rule to enforce.
- **Status**: open

### Issue 22 -- Severity: suggestion (architecture)
- **File**: tripsapplication/src/main/java/org/fxyz3d/ (117 files)
- **Description**: A copy of the `org.fxyz3d` library lives inside the source tree. No README or fork-point notes — unclear whether it's modified or verbatim. License + maintenance risk.
- **Suggestion**: If unmodified, delete and add the upstream Maven coordinate. If forked, move to a separate module (`tripsapplication-fxyz3d-fork/`) with a CHANGELOG describing the diff from upstream.
- **Status**: open

### Issue 23 -- Severity: bug (medium)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/StarEditDialog.java:85,90-91 ; tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/StarObject.java:106-108 (`aliasList @ElementCollection(fetch = LAZY)`)
- **Description**: Dialog code calls `Hibernate.isInitialized(record.getAliasList())` and mutates the lazy collection. The dialog runs outside any transactional boundary, so detached entities will throw `LazyInitializationException`. The defensive `isInitialized` check hides — not solves — the problem.
- **Suggestion**: Load with `JOIN FETCH` in the service layer; pass a `StarEditViewModel` DTO to the UI. Stop letting JPA entities escape into JavaFX bindings.
- **Status**: open

### Issue 24 -- Severity: bug (medium)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/jpa/repository/StarObjectRepository.java:58,216,225,232,256,414,429 (and similar in other repos)
- **Description**: `findByDataSetNameOrderByDisplayName`, `findByDisplayNameContaining`, `findByConstellationName`, `findBySolarSystemId`, etc., return `List` without `Pageable`. On a 2M-star dataset, an accidental "list all by dataset" call OOMs the JVM.
- **Suggestion**: Add paginated overloads. Mark the unbounded variants `@Deprecated` with a migration note; remove once callers migrated.
- **Status**: open

### Issue 25 -- Severity: bug (medium)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/jpa/repository/ExoPlanetRepository.java (`findByHostStarId`) + callers
- **Description**: When code iterates exoplanets and dereferences the host star, each pair becomes a separate query. No `@EntityGraph` or `JOIN FETCH` variant.
- **Suggestion**: Add `findByHostStarIdGraph(...)` with `@EntityGraph(attributePaths = "hostStar")` (or move to a JPQL `JOIN FETCH`). Document expected eager-vs-lazy assumptions on each finder method.
- **Status**: investigated — no actual N+1 in the current code path. `ExoPlanet.hostStarId` is a raw `String` foreign-key column, not a JPA `@ManyToOne` association, so `@EntityGraph(attributePaths = "hostStar")` is not applicable (no `hostStar` field exists to fetch). Grepping for `getHostStarId()` callers turned up no "loop planets, look up host star by id" pattern either. Javadoc added to `findByHostStarId` documenting the raw-FK nature so a future reader doesn't go looking for the missing association. If the data model later adds a real `@ManyToOne hostStar` field, this issue should be re-evaluated.

### Issue 26 -- Severity: bug (medium)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/solarsystem/rendering/OrbitVisualizer.java:81-82,323
- **Description**: Each orbit creates two fresh `PhongMaterial` instances (`baseMaterial`, `highlightMaterial`). With 10+ planets and moons, hundreds of duplicate materials accumulate. Mesh caching exists (line 78); material caching does not. Adds GPU memory + GC pressure.
- **Suggestion**: Cache materials by color in a thread-confined map (FX thread) and reuse across orbits.
- **Status**: done (Phase 6.4) — `OrbitVisualizer` now keeps a `Map<MaterialKey, PhongMaterial> materialCache` keyed by a `(MaterialVariant, baseColor)` record. The three creation paths (`createOrbitMaterial(color, base)`, `createOrbitMaterial(color, highlight)`, `createPositionMarker`) all go through `computeIfAbsent`, so a system with N planets/moons sharing a small colour palette allocates O(unique colours) materials instead of O(2·N) fresh ones every render.

### Issue 27 -- Severity: bug (medium)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/service/graphsearch/task/LargeGraphSearchTask.java:319 ; tripsapplication/src/main/java/com/teamgannon/trips/service/graphsearch/task/SparseTransitComputor.java:33,144
- **Description**: `LargeGraphSearchTask` calls `shutdown()` in `finally` but never `awaitTermination()` — worker threads can outlive the Task. `SparseTransitComputor` creates a fresh `Executors.newFixedThreadPool(getNumCores())` per instance; multiple concurrent searches stack non-daemon pools that block JVM exit.
- **Suggestion**: Use one shared, daemon-threaded executor for graph search. Always `shutdown()` + `awaitTermination(timeout)` + `shutdownNow()` fallback. Clear `sparseTransitList` and `collisionSet` in `finally` to release ~5 M-edge graph memory.
- **Status**: open

### Issue 28 -- Severity: bug (medium)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/service/BulkLoadService.java:63 ; tripsapplication/src/main/java/com/teamgannon/trips/dataset/factories/DataSetDescriptorFactory.java:91
- **Description**: `loadCSVFile()` is not `@Transactional` and orchestrates a chain that includes `starObjectRepository.saveAll(...)` plus descriptor save. A mid-import crash leaves a partial dataset with no descriptor (or vice versa) — no rollback across the whole import.
- **Suggestion**: Wrap the entire import in a top-level `@Transactional(rollbackFor = Exception.class)`. Make sure parse-errors abort with a clean state, not "half a dataset".
- **Status**: done -- `BulkLoadService.loadCsvDataset(...)` now owns a single rollback boundary for CSV read, batched star saves, and descriptor creation; `CSVLoadTask` delegates to that entry point.

### Issue 29 -- Severity: suggestion (UX)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/dialogs/search/FindStarByCommonNameDialog.java:93 (and 27 other `new Alert(...)` sites)
- **Description**: Modal `Alert` is used for inline validation messages like "you must enter a partial id". Blocks the whole app for a non-critical UX nudge.
- **Suggestion**: Use inline validation (red border + small error label below the field). Reserve modal `Alert` for genuinely-blocking errors (DB failure, unreadable file).
- **Status**: open

### Issue 30 -- Severity: suggestion (correctness audit)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/solarsystem/modelling/utils/* (and any other 3D coordinate transforms)
- **Description**: CLAUDE.md documents that non-linear scaling MUST be radial, not per-axis. `OrbitVisualizer.toScreen()` is correct. Other 3D paths haven't been audited — if any do `auToScreen(x), auToScreen(y), auToScreen(z)` independently with log scaling, geometry gets squashed.
- **Suggestion**: Grep for `auToScreen(` and `Math.log` uses on coordinates; verify every site uses the shared radial helper. Add a unit test that fails if anyone reintroduces per-axis scaling.
- **Status**: done (Phase 6.5) — audited all 11 `auToScreen(` callsites: every one passes a single scalar (orbital distance, body size, min-orbit), never a coordinate triple. `ScaleManager.auVectorToScreen(x, y, z)` is the canonical radial helper for 3D coords. New regression test `ScaleManagerRadialScalingTest` (5 tests, all green) pins the contract: same-radius points must scale to same screen-radius, direction must be preserved, naive per-axis scaling is shown to distort. A future per-axis refactor will fail the suite.

### Issue 31 -- Severity: suggestion (data model)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/StarObject.java:273-284 (`miscText1..5`, `miscNum1..5`, `customData1..10`) ; tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/SolarSystem.java:149-170
- **Description**: 15+ unused extensibility columns per entity. Always fetched. No schema, no validation. Anti-pattern — looks like extensibility but is actually noise.
- **Suggestion**: Remove unused fields. If extensibility is genuinely needed, use one JSON column with a JSON Schema validator, or a side `EntityExtension` table keyed by entity id.
- **Status**: open

### Issue 32 -- Severity: suggestion (UX / i18n)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/spaceshipmodeller/ui/TransferPlannerPanel.java:179,299-302
- **Description**: Hardcoded English strings ("Ship:", "Route:", "Total Δv:", "No maneuvers") in a module that elsewhere routes UI text through `SpaceshipModellerLabels.get(...)`. Drifts from the user's stated `.properties` preference (see memory `trips-properties-file-preference`).
- **Suggestion**: Move every visible string into `spaceshipmodeller.properties`. Add a build-time scan to warn on hardcoded strings in `spaceshipmodeller/ui/*`.
- **Status**: partial (Phase 6.7) — the 6 detail-panel strings + 3 maneuver-table strings called out in this issue (`Ship:`, `Route:`, `Total Δv:`, `Total propellant:`, `Mission duration:`, `Status:`, `No maneuvers`, `Maneuver`, `Δv`) are now externalised under `planner.detail.*` / `planner.nodeTable.*` keys in `spaceshipmodeller.properties`, sourced via `SpaceshipModellerLabels.get(...).formatted(...)`. A full sweep across the rest of `spaceshipmodeller/ui/*` plus the CI scan is deferred — they're additive and don't block this issue's specific call-outs.

### Issue 33 -- Severity: suggestion (accessibility)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/spaceshipmodeller/ui/TransferPreviewDialog.java:231,240-245,289-293
- **Description**: Feasibility and transfer-type signaling use color only (green/orange/red text fill). Red-green colorblind users lose the channel.
- **Suggestion**: Pair every color cue with a glyph prefix (✓ ⚠ ✗) or text token (`[EFFICIENT]`, `[MARGINAL]`, `[INSUFFICIENT]`). Verify WCAG-AA contrast on foreground/background pairs.
- **Status**: done (Phase 6.8) — `TransferPreviewDialog` now prepends `costGlyph(cost)` (✓/⚠/✦) to the transfer-type combo entries and `feasibilityGlyph(f)` (✓ [FEASIBLE] / ⚠ [MARGINAL] / ✗ [INSUFFICIENT]) to the feasibility status label. The "Unavailable for this drive" branch also gained the ✗ [UNAVAILABLE] glyph + token. Colour-blind / greyscale users now have a non-colour channel for the same information. WCAG contrast audit on the specific hex pairs is deferred — a separate accessibility pass with the actual rendered colours under both light and dark palettes.

### Issue 34 -- Severity: suggestion (UX)
- **File**: dialog FXMLs under `src/main/resources/com/teamgannon/trips/` (multiple)
- **Description**: Fixed pixel sizes (e.g., `TransferPlannerPanel` nameCol 150px, nodeTable 160px; `SpaceshipDesignerPanel.validationMessages.prefHeight = 120`) prevent responsive resizing. Users frequently resize the main stage.
- **Suggestion**: Switch to `HGrow.ALWAYS` / `VGrow.ALWAYS` and percentage column constraints. Test at 1024×768 and 4K.
- **Status**: open

### Issue 35 -- Severity: suggestion (UX)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/dialogs/search/FindStarByCommonNameDialog.java:26 (and other text-input dialogs)
- **Description**: Search/filter `TextField`s lack `promptText` and tooltips. Users have to guess input format.
- **Suggestion**: Add `setPromptText("e.g., Sol, Alpha C")` and tooltips for non-obvious fields. Document accepted formats inline.
- **Status**: open

### Issue 36 -- Severity: suggestion (UX)
- **File**: `controller/menubar/*.fxml` ; main MainPane.fxml
- **Description**: Menus have no `mnemonicParsing="true"` Alt-key shortcuts. Buttons inconsistent ("Update", "Dismiss", "Confirm").
- **Suggestion**: Standardize button labels (OK / Cancel / Apply). Add mnemonics to top-level menus (`_File`, `_Tools`, `_Design`).
- **Status**: done (Phase 6.12) — all 11 top-level menus now have unique mnemonics: `_File _Edit _View _Search _Tools _Reports _Utilities _Admin _Design E_xperimental _Help` with `mnemonicParsing="true"`. Button-label normalisation: 3 dialogs renamed "Dismiss" → "Cancel" where the variable name + sibling primary-action button signalled intent (`ScriptDialog`, `AddStarRecordDialog`, `UpdateStarObjectWithRecordDialog`). The remaining 2 "Dismiss" sites (one-button warning/control dialogs with no input — `RotationDialog`, `ShowZoomWarning`) kept their labels since "Dismiss" is appropriate for a non-cancel-anything close action. Two duplicate `dismissButton.setOnAction(...)` lines fixed in passing.

### Issue 37 -- Severity: bug (medium)
- **File**: ~50 `@EventListener` Spring beans, especially dialog controllers
- **Description**: When a dialog closes, its `@EventListener` bean stays in the Spring context and may hold references to disposed JavaFX nodes. Over a long session this compounds into a real leak.
- **Suggestion**: Either (a) use `@Scope("prototype")` + an explicit unregister hook on dialog close, or (b) move event handling out of dialogs into a coordinator that outlives them. Audit the dialog → listener wiring.
- **Status**: open

### Issue 38 -- Severity: nit
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/file/csvin/RegularStarCatalogCsvReader.java:212
- **Description**: Broad `catch (Exception)` inside the parse loop swallows per-row failures, increments a reject counter, and continues with no context for the bad row.
- **Suggestion**: Catch `NumberFormatException` / `ArrayIndexOutOfBoundsException` / `DateTimeParseException` specifically. Capture the row number and offending field; emit a summary at end with first N bad rows for the user to inspect.
- **Status**: partial (Phase 7.1) — catch narrowed to `IllegalArgumentException | ArrayIndexOutOfBoundsException | NullPointerException` (covers `NumberFormatException` via its IAE superclass). The row number is already in the log message; the "summary of first N bad rows" UX deferred (would need a per-import collector hooked into the existing reject counter).

### Issue 39 -- Severity: nit
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/service/SolarSystemService.java:239-240 (and other `return null` on error sites)
- **Description**: `createSolarSystem()` logs an error and returns `null`. Callers either crash on the surprise NPE or have to add defensive null-checks that obscure intent.
- **Suggestion**: Return `Optional<SolarSystem>` (or throw a checked domain exception). Apply the same fix to the `OptionalValue` bridge methods and `Planet.findPrimaryJovian()`.
- **Status**: done (Phase 7.2) — `SolarSystemService.createSolarSystem(StarDisplayRecord)` now returns `Optional<SolarSystem>`; `SolarSystemService.findJupiterInSystem(String)` now returns `Optional<ExoPlanet>`. Both methods have zero external callers in the current codebase (public API placeholders), so the signature change is risk-free. The other forward references in the original issue (`OptionalValue`, `Planet.findPrimaryJovian`) don't exist anywhere in the tree.

### Issue 40 -- Severity: nit
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/solarsystem/modelling/accrete/Utils.java:84-103
- **Description**: `loadFile()` opens a `BufferedReader` and `close()`s manually in a `try/finally`; on exception during read, the close path is fragile. Worse, the catch path calls `System.exit(1)` from a library helper.
- **Suggestion**: Convert to try-with-resources. Replace `System.exit` with a thrown `IOException` so the caller decides how to handle a missing data file.
- **Status**: done (Phase 7.3) — `Utils.loadStarType` now uses try-with-resources on the BufferedReader; `System.exit(1)` replaced with an `UncheckedIOException` so the caller decides how to handle missing/unreadable data files.

### Issue 41 -- Severity: nit
- **File**: ~67 `log.*` call sites across the codebase
- **Description**: String-concatenation log messages (`log.info("Discarded " + count + " systems")`) instead of SLF4J parameter substitution. Defeats lazy evaluation; adds GC pressure in hot paths.
- **Suggestion**: Convert to `log.info("Discarded {} systems", count)`. Add a checkstyle rule to fail builds on `+` inside `log.*` arguments.
- **Status**: done (Phase 7.4) — `scripts/check-logging.sh` reports 0 violations across the whole source tree (was 107). 50 files updated. Throwables now passed directly to SLF4J as last arg where applicable, so full stack traces print. Latent bug fixed at `ChviewReader.java:899` where `index + i` was being string-concatenated instead of summed. The rule is now also enforced by `maven-checkstyle-plugin` (config at `tripsapplication/config/checkstyle/checkstyle.xml`, bound to the `validate` phase) so regressions fail the build before compilation; smoke-tested by introducing a deliberate violation and confirming the build fails with a pointer to the file:line.

### Issue 42 -- Severity: nit
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:264 ; tripsapplication/src/main/java/com/teamgannon/trips/scripting/ScriptDialog.java:225-239
- **Description**: Manual `FileInputStream` / `BufferedReader` without try-with-resources. On exception, streams may leak.
- **Suggestion**: Use try-with-resources everywhere. Add a pass over `file/`, `scripting/`, and `service/importservices/` to confirm.
- **Status**: partial (Phase 7.3) — both call-out sites fixed: `MainPane` app-icon load (try-with-resources on FileInputStream, narrowed catch, removed redundant null-check on a constructor that can't return null); `ScriptDialog.loadScriptFile` (nested try/finally collapsed to a single try-with-resources). A full pass over `service/importservices/` is deferred — those services may already use the right pattern, but a systematic grep wasn't done.

### Issue 43 -- Severity: nit (correctness debt)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/solarsystem/modelling/accrete/StarSystem.java (18+ TODOs) ; `Planet.java` (similar)
- **Description**: Constants like `PROTOPLANET_MASS`, `DUST_DENSITY_COEFF` carry open `TODO` comments questioning their values. The accrete algorithm correctness is therefore unverified.
- **Suggestion**: Audit against Dole 1970 (the canonical accretion paper) or document the values as "known approximations" with the reference paper. Convert unresolved TODOs into tracked issues.
- **Status**: done (Phase 7.5) — all 17 inline TODOs in `StarSystem.java` (10) + `Planet.java` (7) converted to proper docs. The 5 class-level constants (B, PROTOPLANET_MASS, DUST_DENSITY_COEFF, ALPHA, N, K) carry Javadoc citing Dole 1970 eqn/section + the ACCRETE reference-implementation lineage. The constants are documented as "retained as published — part of the algorithm's contract, not free parameters to tune", which matches how the ACCRETE community treats them. Inline TODOs likewise replaced with explanations covering: tidally-locked planet habitability scope, orbital-zone vs frost-line relationship, atmosphere-sort reference behaviour, moon-toString cosmetic limitation, etc.

### Issue 44 -- Severity: nit
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/astrogation/Coordinates.java:11-27
- **Description**: 3×3 transformation matrices hardcoded as numeric literals with no source citation. Future maintainers can't tell whether they're equatorial-to-galactic, what epoch (J2000?), or where they came from.
- **Suggestion**: Extract to named constants (`EQUATORIAL_TO_GALACTIC_J2000`); add Javadoc linking to the IAU definition; add a round-trip unit test.
- **Status**: done (Phase 7.6) — `Coordinates` now has named `EQUATORIAL_TO_GALACTIC_J2000` + `GALACTIC_TO_EQUATORIAL_J2000` constants (each with citations: IAU 1958 + Liu et al. 2011 A&A 526 A16 + Reid & Brunthaler 2004 ApJ 616 872), and `CoordinatesRoundTripTest` (6 tests) pins round-trip identity to 1e-9 relative, matrix orthonormality, and inverse-equals-transpose.

### Issue 45 -- Severity: nit
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/StarObject.java:301 ; SolarSystem.java:237-242 ; ExoPlanet.java:754-764
- **Description**: ID-only `equals/hashCode` is correct, but `id` itself is not `final`. If any code path mutates `id` after construction, hash-based collections corrupt silently.
- **Suggestion**: Make UUID `@Id` fields `final` (assign in constructor); add a static factory helper if needed. Add an `@PrePersist` assertion that `id` is non-null and untouched.
- **Status**: partial (Phase 7.7) — `@PrePersist`/`@PreUpdate` id-non-null assertions added on all three entities (StarObject reuses the existing `ensureCoordinates` hook; SolarSystem gains a new `assertIdAssigned`; ExoPlanet's existing auto-id hook promoted to handle update too, with the assertion baked in). `@Id` kept non-final: Hibernate hydrates entities via reflection, so making the field final would require unsafe-reflection workarounds that aren't worth it. The persist-time assertion catches the actual bug class — silent hash-collection corruption.

### Issue 46 -- Severity: nit
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/StarObject.java:255-260 (notes, source) ; ExoPlanet.java:711-729 (procedural snapshots) ; DataSetDescriptor.java:114-144 (JSON LOBs)
- **Description**: `@Lob` columns lack explicit `@Column(length = ...)` and aren't `@Basic(fetch = LAZY)`. Every find loads them — a non-trivial cost when the catalog has 2M rows.
- **Suggestion**: Add length caps and `@Basic(fetch = LAZY)`; or split into a side `*_Lob` table joined only when the field is requested.
- **Status**: partial (Phase 7.8) — `@Basic(fetch = LAZY)` annotations added to the 6 DataSetDescriptor LOBs, the 3 ExoPlanet procedural LOBs (including the heavy `byte[] proceduralPreview` image), and StarCatalogIds.catalogIdList. `StarObject.notes` + `StarObject.source` intentionally kept eager with a comment: WorkbenchEnrichmentService + edit/properties dialogs read them too broadly to mark LAZY without a caller-by-caller audit. Caveat: these annotations are *advisory* until `hibernate-enhance-maven-plugin` is wired into the build — without bytecode enhancement, Hibernate silently ignores `@Basic LAZY` on plain fields. Annotating now documents intent and auto-corrects when the enhancement is enabled.

### Issue 47 -- Severity: suggestion (test coverage)
- **File**: tripsapplication/src/test/java/...
- **Description**: Hard gaps: `service/importservices/` has 0 tests (mission-critical), `dialogs/` has 3 tests covering 94 source files, `graphics/panes/` has 6 tests covering 14 source files. `controller/MainSplitPaneManager.java` (670 lines) is untested.
- **Suggestion**: Prioritize importservices (refactor + characterization tests first). Use TestFX for the most-used dialogs. Add a smoke test for `SolarSystemSpacePane.setSystemToDisplay()` to catch FX-thread regressions.
- **Status**: partial (Phase 7.9) — focused payoff from the 7.14 extraction: `PlotStarsCoordinatorTest` (12 tests, 3 @Nested classes) pins the pure-function helpers extracted out of `MainSplitPaneManager.onPlotStarsEvent`. Demonstrates the "extract-then-test" pattern: orchestration moves to a `@Component` with pure helpers, tests cover the helpers without TestFX. The broader sweep (importservices/, TestFX on top dialogs, `SolarSystemSpacePane.setSystemToDisplay()` smoke test) is still a multi-sprint effort and remains open.

### Issue 48 -- Severity: nit
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:375-376,508-509,521-522,585
- **Description**: Four event-handler methods do `catch (Exception)` + log + show `Alert`, mixing recoverable failures (parse error) with unrecoverable ones (OOM). Hides bugs.
- **Suggestion**: Narrow each catch to the specific exception expected. Let unexpected throwables propagate to the global uncaught-exception handler so they're reported via the existing problem-report flow.
- **Status**: done (Phase 7.1) — all 4 event-handler catches in `MainSplitPaneManager` (recenter star, show stellar data, export query, plot stars) narrowed from `catch (Exception)` to `catch (RuntimeException)`. `Error` (OOM, StackOverflow) now reaches the global uncaught-exception handler. Narrower-still per-exception catches would require enumerating every checked exception the downstream `@Transactional` services can throw — RuntimeException is the right boundary in practice.

### Issue 49 -- Severity: suggestion (accessibility)
- **File**: ~60 dialogs across the codebase
- **Description**: No `accessibleText`, `accessibleHelp`, or `AccessibleRole` annotations anywhere. Screen readers see opaque widgets.
- **Suggestion**: Systematic pass on high-traffic dialogs first (search, route planning, transfer preview). Add `accessibleText` to every button and labelled input.
- **Status**: partial (Phase 7.11) — worked example added to `TransferPreviewDialog`: every interactive control (4 ComboBoxes, 1 TextField, the primary Create Plan button) carries `setAccessibleText` with a verbose role + intent label; the read-only feasibility verdict + explanation labels carry `setAccessibleHelp`. The pattern is now established for the broader systematic sweep across ~60 dialogs. That sweep remains a 1-sprint effort and is still open.

### Issue 50 -- Severity: suggestion (UI consistency)
- **File**: only 3 CSS files exist (SearchPane.css, viewer.css, tree-table-view.css). Inline `-fx-...` strings scattered across many dialogs (e.g., `TransferPreviewDialog.java:204`).
- **Description**: No central stylesheet; per-dialog inline styles make theme changes fragile. Color palette is hardcoded in Java string literals.
- **Suggestion**: Create `theme.css` with CSS variables (font, spacing, severity colors). Reference from FXML via `stylesheets` attribute. Remove inline `-fx-style` strings.
- **Status**: partial (Phase 7.10) — `tripsapplication/src/main/resources/com/teamgannon/trips/theme.css` seeded with CSS variables for the recurring patterns mined from the ~75 inline `-fx-…` callsites: severity palette (info/success/warn/danger), panel backgrounds (light/mid/dark), border tokens (radius-sm/md/lg), typography (xs/sm/md/lg), spacing tokens (pad-sm/xs). Includes ready-to-use classes for status badges + severity-typed labels. The per-dialog migration (replacing inline `setStyle("-fx-…")` with `getStyleClass().add(...)`) is documented as a 6-step migration plan in the file footer; it remains a sprint of work because each callsite needs visual diffing and intent confirmation.

### Issue 51 -- Severity: nit
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/experimental/AsteroidFieldWindow.java:287-314
- **Description**: `AnimationTimer.handle()` mutates `displayPoints` and `angles[]` without synchronization. Safe today because `AnimationTimer.handle` runs on the FX thread, but the thread invariant isn't documented; future "let's parallelize the ODE step" changes will silently break it.
- **Suggestion**: Add a comment on the field declaration that says "mutated on FX thread only via `AnimationTimer`". Optionally add a runtime assertion in the handle method.
- **Status**: done (Phase 7.12) — `angles[]` + `displayPoints` field-level javadoc spells out the FX-thread invariant. New `FxThread.assertFxThread()` static helper (general-purpose) called at the top of `AnimationTimer.handle` enforces the invariant — a future "parallelise the ODE step" change will fail fast at runtime instead of silently corrupting state.

### Issue 52 -- Severity: bug (medium)
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/scripting/engine/PythonScriptEngine.java:30
- **Description**: `publishEvent(new StatusUpdateEvent(...))` is called synchronously from `runAScript()`. If callers run scripts on a background thread, Spring delivers the event on that thread — listeners that mutate the scene graph without wrapping in `Platform.runLater` will corrupt the UI.
- **Suggestion**: Document the threading contract on every event class ("delivered on publisher's thread; listeners must wrap scene-graph mutations in `Platform.runLater`"). Or publish via a dedicated `FxEventBus` that always re-dispatches on the FX thread.
- **Status**: open

### Issue 53 -- Severity: suggestion (performance)
- **File**: tripsapplication/src/main/resources/application.yml (Hibernate caching)
- **Description**: Hibernate second-level cache disabled. Repeated query patterns (e.g., `getFromDatasetWithinRanges`, repeated `findBySolarSystemId` during a render) re-hit the DB every time.
- **Suggestion**: Enable second-level cache with JCache region factory for read-mostly entities (`DataSetDescriptor`, `StarObject` lookups by id). Benchmark before/after.
- **Status**: open

### Issue 54 -- Severity: nit (UX)
- **File**: tripsapplication/src/main/resources/com/teamgannon/trips/screenobjects/StarEditDialog.fxml:232-293
- **Description**: A "User Special Info" tab exposes raw `misc1`–`misc5` field names with no labels. Useless to end-users; coupled to Issue 31's customData smell.
- **Suggestion**: Remove the tab once Issue 31 lands; if extensibility is kept, present a key/value editor instead of raw `miscN` fields.
- **Status**: open

### Issue 55 -- Severity: nit (UX)
- **File**: search-pane FXMLs under `src/main/resources/com/teamgannon/trips/search/components/`
- **Description**: 13 search-pane FXMLs repeat the same Label + control + grid layout. Boilerplate; inconsistent spacing creeps in across copies.
- **Suggestion**: Build a `SearchPanelBase.fxml` (or a Java composite) and inherit. Saves maintenance and enforces visual consistency.
- **Status**: done (Phase 7.13) — `BasePane` now exposes a `loadFxml(String)` helper that wraps the FXMLLoader.setRoot(this).setController(this).load() boilerplate. All 14 panels (the 13 search-pane panels + DataSetPanel which follows the same pattern) collapsed their 11-line FXML-load constructor blocks + 2 unused imports into a single `loadFxml("PanelName.fxml")` call. Net 92 lines removed. The FXML files themselves stay separate — they're already minimal (13-39 lines each) and consolidating them via `fx:include` would add more boilerplate than it removes.

### Issue 56 -- Severity: suggestion (visibility)
- **File**: every `ApplicationEvent` class in `events/`
- **Description**: 64 publishers + ~50 listeners with no map of the event graph. Grep finds events with apparently zero listeners (e.g., `ShowStellarDataEvent` after some refactor).
- **Suggestion**: Add an `EventCatalog` doc (or a startup `@PostConstruct` validator) listing every event with its publishers and subscribers. Fail-fast if an event has zero subscribers.
- **Status**: open

### Issue 57 -- Severity: nit
- **File**: tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:499-514 (and several event-handler lambdas)
- **Description**: `@EventListener` methods contain inline business logic — query dispatch wrapped in `FxThread` lambdas wrapped in try/catch. Hard to test; failure modes opaque.
- **Suggestion**: Extract dispatch into a coordinator service; have the `@EventListener` delegate. Test the coordinator, not the controller.
- **Status**: partial (Phase 7.14) — `PlotStarsCoordinator` (`@Component`, ~160 lines) owns the business logic previously inlined in `MainSplitPaneManager.onPlotStarsEvent` (~50-line handler body + two private geometry helpers). `@EventListener` stays on `MainSplitPaneManager` and delegates inside the existing FxThread.runOnFxThread wrap. `PlotManager` is passed as an argument to `handle()` (not autowired) because it's constructed late in `MainSplitPaneManager.initialize()` and isn't a `@Component`. 12-test `PlotStarsCoordinatorTest` proves the extraction is unit-testable without TestFX. The other 3 `@EventListener` methods on `MainSplitPaneManager` (recenter, show stellar data, export query) stay inline — they call private methods that read controller `@FXML` state, so extracting them would require either moving that state too (big change) or creating a back-reference (circular).

---

# Comprehensive Remediation Plan

Phases are ordered by dependency and risk. Each phase is independently shippable; later phases assume earlier ones landed (especially Phase 0 — a migration tool unlocks everything that touches schema). Reference issue numbers tie back to the catalog above.

## Phase 0 — Foundation (do these first; everything else assumes them) — **COMPLETE**

| # | Action | Issue(s) | Status |
|---|---|---|---|
| 0.1 | `git rm --cached` the 6 large tracked CSVs (`30ly.trips.csv`, `30ly.trips.csv.zip`, `exoplanet.eu_catalog_*.csv`, `newDataset1-page-1.csv`, `files/30ly_dataset.trips.csv`, `files/exoplanet.eu_catalog.csv`, `files/exoplanets/exoplanets.csv`). `.gitignore` updated with explicit `*.csv` rules + positive exceptions for `files/programdata/*.csv` and `tripsapplication/src/main/resources/**/*.csv` (the small reference data the app ships with). `Readme.md` gained a "Sample Datasets" section with sources. History rewrite (`git filter-repo`) deferred. | 16 | done |
| 0.2 | Added `org.springframework.boot:spring-boot-flyway` (the 4.x-renamed module — `spring-boot-autoconfigure` no longer carries `FlywayAutoConfiguration` as of Boot 4). Generated `tripsapplication/src/main/resources/db/migration/V1__baseline.sql` (17 tables, 85 lines) via a new `SchemaBaselineExporterTest` (`@Disabled` regen tool — see `db/migration/README.md`). `application.yml` wired with `spring.flyway.{enabled, baseline-on-migrate, baseline-version, locations, table}`. Default profile keeps `ddl-auto=update` for now; **new `application-prod.yml`** sets `ddl-auto=validate`. Added `FlywayBaselineSmokeTest` (runs on every `mvn test`) that boots `@DataJpaTest` against H2, applies V1, then runs Hibernate `validate` — a permanent regression guard for entity↔schema drift. `BaseRepositoryIntegrationTest` patched to disable Flyway (its Postgres testcontainer can't ingest the H2-flavoured baseline). Full non-integration test suite (2,709 tests) still green. | 13 | done |
| 0.3 | Style guide added under `AGENTS.md > "Logging"` and `CLAUDE.md > Lombok Usage > "Logging"`. Shipped `scripts/check-logging.sh` (executable; supports `--count`). Current violation count: **113**. Wiring into `maven-checkstyle-plugin` as an enforcing step lands in Phase 7.4 once the existing violations are cleaned up — adding it now would pollute every build with 113 warnings. | 41 (rule) | done |

## Phase 1 — Data Correctness (silent bugs that compound) — Complete

| # | Action | Issue(s) | Status |
|---|---|---|---|
| 1.1 | Added `StarMassNormalizer`, applied it at import/creation boundaries, added `V2__normalize_star_obj_mass_to_solar.sql`, updated `StarObject.mass` documentation, removed the `TransferCalculator.toSolarMasses(...)` heuristic, and covered normalization with `StarMassNormalizerTest` plus updated transfer-calculator expectations. | 15 | done |
| 1.2 | Added `BulkLoadService.loadCsvDataset(...)` as the transactional CSV import entry point and moved `CSVLoadTask` to delegate to it, so parser failures and descriptor failures roll back the import as one unit. | 28 | done |
| 1.3 | Set `hibernate.jdbc.batch_size` to 5,000 to match the parser batch and updated `StarService.starBulkSave(...)` to `flush()` + `clear()` after each batch. | 12 | done |
| 1.4 | V3 migration `drop_spaceship_design_series` drops the phase-18 orphan column with `DROP COLUMN IF EXISTS` (idempotent — no-op on fresh DBs). V4 migration `backfill_transfer_plan_available_propellant` sets `available_propellant_tons = total_propellant_tons` for legacy rows where availableProp=0 and totalProp>0 (their plans must have been feasible at save time). | 13 (followup) | done |

Verification:
- `./mvnw-java25.sh -q -pl tripsapplication -DskipTests compile` passed.
- `./mvnw-java25.sh -pl tripsapplication test -Dtest='StarMassNormalizerTest,TransferCalculatorTest,FlywayBaselineSmokeTest' test` passed — the smoke test exercises V1→V2→V3→V4 against in-memory H2 followed by Hibernate `validate` against the entity model.
- Full non-integration suite: **2,714 tests pass, 0 failures** (+5 from new `StarMassNormalizerTest`).

## Phase 2 — Fix FX-thread Freezes (user-visible) — **COMPLETE**

| # | Action | Issue(s) | Status |
|---|---|---|---|
| 2.1 | Wrapped `SolarSystemSpacePane.setSystemToDisplay()` and `refreshCurrentSystem()` in `javafx.concurrent.Task` via a shared `loadAndRenderSystem(...)` helper; added a transient loading indicator; render now happens on `setOnSucceeded`, stale loads are cancelled/ignored, failures surface via `setOnFailed` + error alert. | 11 | done |
| 2.2 | Audited 22 `@EventListener`-bearing classes. Confirmed UIStateSynchronizer/PlotManager/SystemPreferencesService don't touch the scene graph. Defensively wrapped six listeners that DID mutate scene graph but lacked an FX-thread wrap: `SolarSystemSpacePane.onSolarSystemDisplayToggleEvent`, `onSolarSystemScaleEvent`, `onSolarSystemAnimationEvent`, `onSolarSystemCameraEvent`; `MainPane.onOpenWorkbenchEvent`, `onGraphEnablesPersistEvent`. Fixed `PythonScriptEngine` to publish via `FxThread.runOnFxThread` (plus fixed two `log.error` typo + string-concat sites it carried). Documented the threading contract in `CLAUDE.md` under "JavaFX Thread Safety > `@EventListener` threading contract" with the canonical defensive idiom. | 14, 52 | done |
| 2.3 | `LargeGraphSearchTask` + `SparseTransitComputor` now use a **daemon** `ThreadFactory` (named `graph-search-N` / `sparse-transit-N` for log clarity). After the work completes the executor is shut down orderly with bounded `awaitTermination(30s)` followed by `shutdownNow()` on timeout. `LargeGraphSearchTask` also overrides `Task.cancelled()` to call `shutdownNow()` eagerly + release the ~5M-edge `sparseTransitList` and `collisionSet` so the GC can reclaim graph state immediately. `SparseTransitComputor` is dead code in production today (only its own `main()` calls it) but got the same treatment for consistency. | 27 | done |
| 2.4 | Audited the `dialogs/` and `workbench/` packages — **zero** `@EventListener` annotations there, so Issue 37's premise (dialogs holding listener registrations) doesn't apply in this codebase. The real leak risk was `AnimationTimer` lifecycle in stand-alone windows: `AsteroidFieldWindow` and `RingFieldWindow` each had a `dispose()` method but no `setOnCloseRequest` hook, so the timer kept firing after the user closed the window. Both got `stage.setOnCloseRequest(event -> dispose())`. Verified `ProceduralPlanetViewerDialog` (the real "dialog" of the bunch) already has its close→stopAnimation hook, and `OrbitalAnimationController` is fully lifecycle-managed by `SolarSystemSpacePane`. | 37 | done |

**Verification**: full non-integration suite 2,714 tests, no regressions. Compile clean. `FlywayBaselineSmokeTest` still asserts entity↔V1+V2+V3+V4 schema match.

## Phase 3 — Architectural Sanity (before adding more features) — **COMPLETE**

| # | Action | Issue(s) | Status |
|---|---|---|---|
| 3.1 | Pragmatic thread-safety pass on `TripsContext`: defensive `FxThread.runOnFxThread` wrap on the two `@EventListener` methods (matches Phase 2.2 pattern); `constellationMap` switched from `HashMap` to `ConcurrentHashMap` so the startup loader can write while FX-thread dialogs read; class-level Javadoc now documents the threading invariant (mutations happen on the FX thread; the listeners re-dispatch defensively because Spring delivers on the publisher thread). A fuller redesign (AtomicReference snapshots / separated mutator service) is deferred to Phase 7 since the real call sites all run on the FX thread today. | 14 | done |
| 3.2 | Inventory delivered as `tripsapplication/src/main/java/com/teamgannon/trips/events/EVENT_CATALOG.md` (35 events with publishers + subscribers). Deleted 3 truly-dead event classes: `DataSetContextChangeEvent`, `DataSetLoadEvent`, `NewDataSetEvent` (also removed the orphan `publishEvent` calls for `DataSetLoadEvent` in CHV/CSV import services). Reviewed all 16 "listener-publishes-follow-up-event" cases: every chain terminates in `StatusUpdateEvent` or `BusyStateEvent`, both of which are intentional fan-out broadcasts to UI sinks — NOT RPC abuses. Issue 19's RPC-chain concern turned out not to apply in this codebase; no further refactor needed. Startup-validator-for-dead-events is left as a Phase 7 followup. | 19, 56 | done |
| 3.3 | Introduced `RequestTransferPlanningEvent` in `spaceshipmodeller/integration/` and `TransferPlanningCoordinator` (@Component) in `spaceshipmodeller/ui/`. `SolarSystemSpacePane.openTransferPlanner` now just assembles the context (bodies, origin, central-star mass) and publishes the event; the coordinator owns the ship-catalog lookup and dialog open. Pane constructor went from **8 args → 5 args** (dropped `SpaceshipService`, `TransferPlannerBridge`, `TransferPlannerLauncher`). spaceshipmodeller is fully isolated again — its only inbound coupling to the pane is via the event. | 20 | done |
| 3.4 | Documented the convention in `CLAUDE.md > Dialog Creation Pattern`: **two coexisting patterns** — FxWeaver for singleton, service-wired controllers (panels, panes, menubars), raw `FXMLLoader` for transient stateful dialogs (`StarEditDialog`, `StarPropertiesPane`) that callers `new` up with per-edit state. The two "violations" the audit flagged are correct uses of the alternative pattern. No code change needed. ArchUnit rule deferred to Phase 7 (the rule is now expressible in prose; enforcement infra can wait). | 21 | done |
| 3.5 | The in-tree `org.fxyz3d` source copy is a **heavily-modified fork**, not a pristine vendor — confirmed by the user during this phase. Externalization to a Maven dep was retired (would lose the local modifications); a `README.md` was added at `org/fxyz3d/README.md` declaring the fork status, license, the two `com.teamgannon.trips.*` call sites that depend on it (`AsteroidFieldWindow`, `RingFieldRenderer`), and explicit "don't replace with the Maven artifact" guidance. The optional move-to-sibling-module path is documented as a future possibility but not pursued. Also saved as a project memory so future sessions don't re-propose externalization. | 22 | done |

**Verification**: full non-integration suite 2,714 tests, no regressions. Compile clean. `FlywayBaselineSmokeTest` continues to assert entity↔schema match across V1+V2+V3+V4.

## Phase 4 — God-class Decomposition (one per sprint; in this order)

Each step: extract focused collaborators, leave the original class as a thin coordinator, add characterization tests before splitting.

| # | Target | Issue | Rough effort |
|---|---|---|---|
| 4.1 | `SolarSystemRenderer` 2,004 → **1,020 lines** (-49%). Five new collaborators alongside the existing `ScaleManager` / `OrbitVisualizer` / `GridAndZoneRenderer` / `OrbitMarkerRenderer` / `SelectionStyleManager`: `SolarSystemColors` (constants + spectral-class / planet colour helpers), `SystemGeometryHelper` (pure orbital-distance + phase-angle math), `TransferTrajectoryOverlay` (dashed Hohmann arc, owns its own Group), `PlanetaryRingManager` (ring + asteroid + Kuiper-belt subsystem, including the ring-adapter, the seeded `Random`, the three visibility flags, and the public add/remove/animate API), `BodyRenderer` (renderStar + renderPlanet + the orbit-context-menu wiring — the two biggest methods). `SolarSystemRendererCharacterizationTest` (4 tests, FX-toolkit headless) guards the contract. Full non-integration suite 2,718 tests green throughout. | 18 | **done** |
| 4.2 | `ProceduralPlanetViewerDialog` **1,936 → 1,508 lines** (-22%). Seven focused helpers extracted: `PlanetScreenshotExporter` (PNG capture), `PlanetCameraController` (mouse + scroll + keyboard + auto-spin Timeline), `PlanetTerrainClassifier` (pure-function classifier + isIcyWorld predicate), `PlanetAtmosphereRenderer` (atmosphere shell with terrain-aware sizing/colour), `PlanetPoleMarker` (N/S marker spheres), `PlanetLegendSection` (stateless TitledPane builder), `PlanetClimateZoneOverlay` (tropic + polar latitude rings). Remaining big chunks (`renderPlanet`, `addRivers`/`addPlateBoundaries`, the 7 createXSection side-panel methods, `regeneratePlanet` + GenerationProgressListener) carry more dialog-state coupling and are deferred — they'd require a ViewModel split or a context object to make extraction clean. Full non-integration suite 2,718 tests green throughout. | 18 | **partial** |
| 4.3 | `WorkbenchEnrichmentService` **1,832 → 1,272 lines** (-30.6%). Three stateless helper classes extracted: `StellarEstimators` (12 pure-function photometric / spectral estimators — mass / luminosity / radius / distance / temperature / spectral conversions, 396 lines), `TapHttpClient` (TAP `/sync` POST with retry-on-429/5xx and a shared `HttpClient`, 108 lines), `TapCsvParser` (CSV splitting, unquoting, header lookup, safe double parsing, 83 lines). Service is now a thin orchestrator on top of these. Two dead rate-limit counter fields (`debugSkipCount`, `distanceRejectCount`) dropped — their gated `log.info` are now `log.debug` inside `StellarEstimators`. The original plan to split per-source (Gaia / SIMBAD / VizieR clients behind an `EnrichmentSource` interface) is deferred: doing it cleanly requires per-catalogue ID-extractor utilities first, which is a separate design pass. Existing `WorkbenchEnrichmentServiceTest` redirected to assert against `TapHttpClient.HTTP_CLIENT`; full non-integration suite 2,718 tests green. | 18 | **partial** |
| 4.4 | `DataWorkbenchController` **1,706 → 1,581 lines** (-7.3%). Two helpers extracted: `WorkbenchPreviewManager` (219 lines — owns the preview table + pagination + page-lazy CSV loading + table-rebuild; controller wires it once via callbacks for error/status/validation-log reporting), `WorkbenchMappingPersistence` (91 lines — load/save the two-column source→target field mapping CSV, plus the `normalizeFieldName` fuzzy-match helper). The original plan's third cut (`WorkbenchEnrichmentUI`) is deferred: the enrichment handlers (~400 lines across `onEnrichDistances`/`onEnrichMissingDistancesLive`/`onPhotometricEstimation`/`onEnrichMasses`/`onEstimateMassPhotometric`/`onEstimateTemperature`/`onEstimateSpectral`/`onCrossFillTempSpectral`) are each tightly coupled to specific FXML controls (text fields, buttons, progress bar) — clean extraction needs a context-object pattern that's a separate design pass. | 18 | **partial** |
| 4.5 | `JavaFxPlanetMeshConverter` **1,644 → 1,523 lines** (-7.4%). `PlanetColorPalette` (166 lines) extracted in the same package: holds the six 9-stop colour arrays (WET / DRY / ICE / JOVIAN / ICE_GIANT terrains + RAINFALL heatmap) and the six lookup / interpolation methods. Converter keeps the public `getColorForX` / `createMaterialForX` methods as thin delegates so external callers don't need to change. `TerrainType` stayed on the converter (3 other files import it from there — moving it would have a much wider blast radius). Remaining bulk is the convert* mesh-build pipeline (~9 overloads, ~800 lines) and vertex / averaging helpers (~200 lines) — both substantial extractions that need their own design pass. | 18 | **partial** |
| 4.6 | `SolPlanetsInitializer` **1,410 → 1,354 lines** (-4%). `SolFeaturesFactory` (100 lines) extracted: builds the Main Asteroid Belt (2.1-3.3 AU) and Kuiper Belt (30-50 AU) as `SolarSystemFeature`s, with the existing idempotency check (skip if features already exist for the system). Bulk of the file (~9 per-planet definition blocks + ~12 per-moon definition blocks, all hand-curated astronomical data) is left in place — the line volume is data, not architectural complexity, and physically splitting it doesn't address the original "Sol as magical special case" concern. The full plan (unify Sol with procedural pipeline via `ISolarSystemFactory`) is a much bigger refactor and stays deferred. | 18 | **partial** |
| 4.7 | Three remaining big classes from the original Phase 4 list. **SolarSystemService 793 → 669 lines (-16%)**: `GeneratedSystemPersister` (227 lines) extracted as a new `@Component` owning the three ACCRETE save paths — `savePlanets` (planets + moons + simulated-row cleanup + planet-count + habitable-zone update), `saveBelts` (asteroid + Kuiper `SolarSystemFeature` rows), and the `saveSystem` convenience that chains both. Cross-service dependency on `findOrCreateSolarSystem` is passed in as a `Function<StarObject, SolarSystem>` so the persister stays decoupled. `SolarSystemService` keeps the public `saveGenerated*` methods as thin delegates so existing callers (`StarContextMenuHandler.java:303`) don't change. Two stale `{:.2f}` log placeholders fixed in passing (SLF4J doesn't grok Python-style format spec). **PlanetarySkyRenderer 1,204 → 889 lines (-26%)**: two cuts. `MilkyWayBackdrop` (197 lines) — the entire procedural Milky Way subsystem (own seeded `Random`, own `Group`, brightness-tier materials palette, and the galactic→equatorial transform that's used nowhere else in the codebase). `PlanetSkyMath` (175 lines) — pure-function helpers: `solveKeplerEquation`, `calculatePlanetOrbitalPosition` (Keplerian propagation with full ω/i/Ω rotation), `calculatePlanetApparentMagnitude` (type-keyed base + 5·log₁₀(d)), `adjustMagnitude` (catalogue→observer rescale), `getPlanetColor`. Renderer keeps a `MilkyWayBackdrop` field and delegates render/clear; `AU_TO_LY` now sourced from the helper. `MainSplitPaneManager` (670) stays deferred — its split would need a controller-state inventory pass. Full non-integration suite 2,718 tests green throughout. | 9 (followup), 18 | **partial** |

## Phase 5 — Package Consolidation

| # | Action | Issue | Rough effort |
|---|---|---|---|
| 5.1 | Three of the four renames applied in a single commit: `solarsysmodelling` → `solarsystem.modelling` (23 files), `planetarymodelling` → `planetary.modelling` (32 files), `solarsystem/sol/` collapsed into `solarsystem/` (3 files). 58 files moved, 134 import-site rewrites, package decls updated in every moved file, test tree mirrored. Full non-integration suite 2,718 tests green. The neutral `model/` package (PlanetSpec, StarSpec) is deferred — those shared specs don't exist yet; identifying what's neutral enough to extract is a separate design pass and benefits from the new package layout being settled first. | 17 | **partial** |

## Phase 6 — Medium Bugs & UX

| # | Action | Issue(s) | Rough effort |
|---|---|---|---|
| 6.1 | Stop entities escaping into dialogs. Introduce DTOs for edit flows (`StarEditViewModel` first). Load with `JOIN FETCH` in services. | 23 | 1-2 sprints — **deferred** (sprint-scale, touches many dialog → service edges; needs a coherent DTO/ViewModel pattern decision first) |
| 6.2 | Add `Pageable` overloads to every unbounded `List`-returning repo method. Deprecate the unbounded variants. Migrate callers. | 24 | 1 sprint — **deferred** (touches every repo + ~80 call sites; needs per-call decision on what's bounded vs not) |
| 6.3 | Add `@EntityGraph` variants for ExoPlanet ↔ hostStar joins; remove N+1s in the planet rendering path. | 25 | **investigated** (no actual N+1 — `hostStarId` is a raw String FK with no `@ManyToOne`; no caller does the loop-and-fetch pattern; javadoc clarification added). |
| 6.4 | Cache `PhongMaterial` by color in `OrbitVisualizer`; verify GPU memory drop with the worst-case (many-moon) system. | 26 | **done** — `Map<(MaterialVariant, Color), PhongMaterial> materialCache` via `computeIfAbsent` across all three creation paths (orbit base, orbit highlight, position marker). |
| 6.5 | Sweep for per-axis non-linear scaling; add a unit test that fails on regression. | 30 | **done** — audited all 11 `auToScreen(` callsites (every one passes a scalar, not a coordinate triple), added `ScaleManagerRadialScalingTest` (5 tests) pinning the radial-isometry contract. |
| 6.6 | Remove unused `miscN` / `customDataN` columns; if extensibility is wanted, replace with a single JSON column + schema validator. | 31, 54 | 1 sprint — **deferred** (irreversible schema change; needs user signoff on whether `customDataN` is still wanted, plus a Flyway migration path for any existing data) |
| 6.7 | Externalize remaining hardcoded strings in `spaceshipmodeller/ui/*` to the existing `.properties` bundle. Add a CI scan rejecting hardcoded strings under that package. | 32 | **partial** — the 9 specific call-outs from Issue 32 (`TransferPlannerPanel` detail + maneuver-table strings) externalised under `planner.detail.*` / `planner.nodeTable.*` keys. Full sweep + CI scan deferred. |
| 6.8 | Pair every color cue with a glyph or text token. Audit WCAG contrast. | 33 | **done** — `TransferPreviewDialog` now prepends ✓/⚠/✦ for transfer cost and ✓/⚠/✗ + `[FEASIBLE]`/`[MARGINAL]`/`[INSUFFICIENT]` for feasibility status. WCAG-AA contrast audit deferred (needs the actual rendered colours under each palette). |
| 6.9 | Replace modal `Alert`-for-validation with inline validation (red border + helper text). Reserve `Alert` for blocking errors. | 29 | 2-3 days — **deferred** (~28 `Alert` sites; needs a uniform inline-validation pattern across dialog FXMLs first) |
| 6.10 | Enable Hibernate second-level cache with JCache; choose read-mostly entities; benchmark. | 53 | 1-2 days — **deferred** (needs JCache provider added + benchmark harness to verify "before/after" on a real dataset) |
| 6.11 | Responsive layouts (HGrow/VGrow), `promptText`, tooltips on every input. | 34, 35 | 1 sprint — **deferred** (sprint-scale across all dialog FXMLs; needs testing at 1024×768 and 4K) |
| 6.12 | Add mnemonics to menus; standardize button labels (OK/Cancel/Apply). | 36 | **done** — all 11 top-level menus have unique `_X` mnemonics with `mnemonicParsing="true"`; 3 "Dismiss" buttons renamed to "Cancel" where variable+sibling-button signalled cancel intent; 2 stays (one-button warning dialogs); 2 duplicate `setOnAction` typo lines fixed in passing. |

## Phase 7 — Code Quality Sweep (parallelizable cleanups)

| # | Action | Issue(s) | Rough effort |
|---|---|---|---|
| 7.1 | Narrow `catch (Exception)` in CSV import + MainSplitPaneManager event handlers. | 38, 48 | **done** — `RegularStarCatalogCsvReader` per-row catch narrowed to `IllegalArgumentException | ArrayIndexOutOfBoundsException | NullPointerException`; all 4 `MainSplitPaneManager` event-handler catches narrowed from `catch (Exception)` to `catch (RuntimeException)` so `Error` (OOM, StackOverflow) reaches the global handler. |
| 7.2 | Replace `return null` on error paths with `Optional` (start with `SolarSystemService`, `OptionalValue` bridge, `Planet.findPrimaryJovian`). | 39 | **done** — `SolarSystemService.createSolarSystem(StarDisplayRecord)` → `Optional<SolarSystem>`, `findJupiterInSystem(String)` → `Optional<ExoPlanet>`. Both have zero external callers (public API placeholders), so risk-free. The forward references in the original issue (`OptionalValue`, `Planet.findPrimaryJovian`) don't exist in the codebase. |
| 7.3 | try-with-resources sweep across `file/`, `scripting/`, `controller/`, `solarsystem/modelling/accrete/Utils`. | 40, 42 | **partial** — the 3 call-out sites fixed: `Utils.loadStarType` (try-with-resources + `UncheckedIOException` replacing `System.exit`), `MainPane` icon load (twr + narrowed catch + removed impossible null check), `ScriptDialog.loadScriptFile` (nested try/finally collapsed). Full sweep of `service/importservices/` deferred. |
| 7.4 | Run the SLF4J parameterization sweep (~113 sites identified in Phase 0.3 via `scripts/check-logging.sh`). Once the count reaches zero, wire `maven-checkstyle-plugin` to fail builds on regressions. | 41 | **done** — `check-logging.sh` reports 0 violations (was 107); 50 files updated; throwables promoted to last-arg where applicable for full stack traces; latent string-concat-instead-of-int-sum bug at `ChviewReader.java:899` fixed in passing. `maven-checkstyle-plugin` now enforces the rule at the `validate` phase (config at `tripsapplication/config/checkstyle/checkstyle.xml`), smoke-tested with a deliberate violation. |
| 7.5 | Resolve or document accrete physics TODOs against Dole 1970. Replace inline numeric constants with named ones referencing the source. | 43 | **done** — 17 inline TODOs in StarSystem + Planet converted to explanatory docs citing Dole 1970 eqns; 5 class-level constants (B, PROTOPLANET_MASS, DUST_DENSITY_COEFF, ALPHA, N, K) carry full Javadoc with paper references, marked "retained as published — part of the algorithm's contract" per ACCRETE community convention. |
| 7.6 | Document `Coordinates.java` transformation matrices; add round-trip tests. | 44 | **done** — named `EQUATORIAL_TO_GALACTIC_J2000` + `GALACTIC_TO_EQUATORIAL_J2000` constants with citations (IAU 1958, Liu et al. 2011, Reid & Brunthaler 2004); new `CoordinatesRoundTripTest` (6 tests) pins round-trip identity + matrix orthonormality + transpose-equals-inverse. |
| 7.7 | Make entity UUID `@Id` fields `final`; add `@PrePersist` assertion. | 45 | **partial** — `@PrePersist`/`@PreUpdate` id-non-null assertions added on all three entities (StarObject, SolarSystem, ExoPlanet); ExoPlanet's existing auto-id `@PrePersist` consolidated with the assertion to avoid undefined-ordering of multiple hooks. `@Id` kept non-final: Hibernate hydrates via reflection, so `final` would require unsafe-reflection workarounds — the persist-time assertion catches the actual bug class (silent hash-collection corruption). |
| 7.8 | Add `@Basic(fetch = LAZY)` and length caps on `@Lob` columns; benchmark catalog-wide find performance. | 46 | **partial** — `@Basic(fetch = LAZY)` added to 10 @Lob fields (DataSetDescriptor ×6, ExoPlanet procedural ×3, StarCatalogIds ×1); StarObject.notes/source kept eager with a comment explaining the WorkbenchEnrichmentService caller-surface risk. Annotations are *advisory* until `hibernate-enhance-maven-plugin` is wired into the build — without bytecode enhancement, Hibernate ignores them on plain fields. Benchmark harness on a real 2M-row dataset remains the open follow-up. |
| 7.9 | Add characterization + happy-path tests to `service/importservices/` (top priority). Then TestFX on the most-used dialogs. Then a smoke test for `SolarSystemSpacePane.setSystemToDisplay`. | 47 | **partial** — focused payoff from the 7.14 extraction: `PlotStarsCoordinatorTest` (12 tests in 3 @Nested classes) pins the geometry helpers extracted from `MainSplitPaneManager.onPlotStarsEvent`, demonstrating the "extract orchestration to a coordinator, then unit-test the pure helpers" pattern that future extractions can follow. The broader sweep (`service/importservices/` characterization, TestFX on top dialogs, `SolarSystemSpacePane.setSystemToDisplay()` smoke test) is still a multi-sprint effort. |
| 7.10 | Build a global `theme.css` with CSS variables. Remove inline `-fx-style` strings. | 50 | **partial** — `theme.css` seeded under `resources/com/teamgannon/trips/` with CSS variables for severity palette, panel backgrounds, border tokens, typography, spacing — derived from the actual hex values mined from the ~75 inline `setStyle("-fx-…")` callsites. Ready-to-use classes for status badges + severity-typed labels. Per-dialog migration documented as a 6-step plan in the file footer — still a sprint of work because each callsite needs visual confirmation. |
| 7.11 | Accessibility pass: `accessibleText` / `accessibleHelp` on top-used dialogs and controls. | 49 | **partial** — worked example added to `TransferPreviewDialog`: every interactive control + read-only verdict label now carries `setAccessibleText` / `setAccessibleHelp` with verbose intent labels. Establishes the pattern for the broader sweep across ~60 dialogs, which remains a 1-sprint effort. |
| 7.12 | Document `AsteroidFieldWindow` thread invariant; add a runtime assertion. | 51 | **done** — `angles[]` + `displayPoints` field javadoc spells out FX-thread-only invariant; new `FxThread.assertFxThread()` static helper enforces it at the top of `AnimationTimer.handle`. |
| 7.13 | Build `SearchPanelBase` (FXML or Java composite); migrate the 13 search-pane FXMLs. | 55 | **done** — `BasePane` now exposes `loadFxml(String)`; all 14 panels (13 search-pane panels + DataSetPanel) collapsed their 11-line FXML-load constructor blocks + 2 unused imports each into a single `loadFxml("PanelName.fxml")` call. Net 92 lines removed. FXML files stay separate (already minimal, fx:include would add more boilerplate than it removes). |
| 7.14 | Extract event-handler business logic in `MainSplitPaneManager` into a coordinator service. | 57 | **partial** — `PlotStarsCoordinator` (~160-line `@Component`) owns the previously-inlined `onPlotStarsEvent` logic + the two private geometry helpers; `@EventListener` delegates inside the existing FxThread wrap. 12-test unit-test suite proves the pattern works without TestFX. The other 3 listeners on `MainSplitPaneManager` stay inline — they call private methods that read controller `@FXML` state, so extracting them needs a bigger move (moving the state too, or creating a back-reference). |

---

## Tracking & Sequencing

- **Phases 0-2** should land before any new feature work. They're the "stop the silent bugs" set.
- **Phase 3** before **Phase 4**: god-class decomposition is much safer once `TripsContext` is thread-safe and the event graph is mapped.
- **Phase 4** ships incrementally; pause feature work in the target file for the duration of each decomposition.
- **Phase 5** (package rename) is a single atomic commit. Coordinate with any open feature branches before doing it.
- **Phases 6 and 7** are mostly parallelizable. Hand them out to whatever capacity is free.
- Items inside Phase 7 are independent; pick them up between bigger pieces of work.

## Verification at end of each phase

Run, in order:
1. `./mvnw-java25.sh -q -pl tripsapplication -DskipTests compile` — must pass.
2. `./mvnw-java25.sh -q -pl tripsapplication test` — must pass with at most the existing Testcontainers-Docker-unavailable failures.
3. Hand-launch the app, exercise: import a small CSV; jump into a solar system; build a route; design a spaceship; create a transfer plan. Verify no UI freezes.
4. For Phase 1 specifically: full 2M-star HYG import as a memory benchmark (target: peak heap ≤ 1.5 GB).

---

**Review artifacts**:
- This file: `/Users/larrymitchell/tripsnew/trips/trips-full-codebase-review-2026.md`
- First pass: ~917 main + 158 test files (Grok 4.3)
- Second pass: ~1,135 main + 158 test files (Claude Opus 4.7, 2026-05-26)
- Key files re-read in the second pass: SolarSystemSpacePane, SolarSystemService, StarObject, ExoPlanet, DataSetDescriptor, BulkLoadService, RegularStarCatalogCsvReader, application.yml, TripsContext, OrbitVisualizer, LargeGraphSearchTask, SparseTransitComputor, TransferPreviewDialog, TransferPlannerPanel, StarEditDialog, MainSplitPaneManager, PythonScriptEngine, AsteroidFieldWindow, Utils (accrete), Coordinates, plus a survey of repository methods and event publishers/listeners.

**Methodology**:
- First pass: tool-assisted static analysis + architectural cross-reference against CLAUDE.md/AGENTS.md.
- Second pass: six concurrent `Explore` sub-agents (architecture; data/JPA; concurrency; UI/UX; performance/memory; code hygiene) briefed to avoid duplicating first-pass findings; synthesis and remediation plan by the main agent. No execution of the full app in either pass.
