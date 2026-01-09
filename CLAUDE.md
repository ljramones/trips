# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TRIPS (Terran Republic Interstellar Plotting System) is a JavaFX-based 3D stellar cartography application for visualizing and plotting interstellar routes. It combines Spring Boot for backend services with JavaFX for 3D visualization, using an embedded H2 database for persistence.

## Build and Development Commands

### Java Version Requirement

**IMPORTANT**: This project requires **Java 17**. The system may have Java 25 installed, but the project will not compile with it.

Use the included wrapper script to automatically use Java 17:
```bash
# From repository root - this automatically uses Java 17
./mvnw-java17.sh clean install

# Run the application
cd tripsapplication
../mvnw-java17.sh spring-boot:run
```

Alternatively, manually set JAVA_HOME before running Maven:
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
mvn clean install
```

### Building and Running

```bash
# Build the entire project (from repository root)
./mvnw-java17.sh clean install

# Run the application
cd tripsapplication
../mvnw-java17.sh spring-boot:run

# Build without running tests (if tests exist)
./mvnw-java17.sh clean install -DskipTests

# Package for distribution
./mvnw-java17.sh clean package
```

### Running in IntelliJ IDEA

The project uses **JavaFX 21.0.5** which has good macOS compatibility. No special VM options should be needed.

**Important**:
- Do NOT add `--add-modules` flags (project uses classpath, not module path)
- Ensure the **JRE** is set to Java 17 (temurin-17)

**Historical Note**: Earlier versions of this project used JavaFX 17.0.1, which had critical bugs on macOS (especially Apple Silicon) causing `NSTrackingRectTag` crashes. If you encounter these crashes, ensure you're using JavaFX 21+ by checking the `<javafx.version>` property in pom.xml.

### Maven Wrapper

The project includes Maven wrapper scripts:
```bash
./tripsapplication/mvnw spring-boot:run  # Unix/Mac
.\tripsapplication\mvnw.cmd spring-boot:run  # Windows
```

### Database

- **Location**: `./data/tripsdb.*` (H2 file-based database)
- **Reset database**: Delete files in `./data/` directory
- **Connection**: `jdbc:h2:file:./data/tripsdb` (username: sa, no password)

### Third-Party Libraries

The root pom.xml installs custom JAR files from `3rdpartylibs/` during the validate phase:
- aelfred-1.0.0.jar
- acme-1.0.0.jar
- core-1.0.0.jar (toxi core)
- physics-1.0.0.jar (toxi physics)

Run `mvn validate` to install these if needed.

### Lombok Configuration

The project uses **Lombok 1.18.34** for code generation (@Data, @Getter, @Setter, @Slf4j annotations). The `maven-compiler-plugin` is configured with annotation processing to generate getters, setters, constructors, and logger fields at compile time. If you see compilation errors about missing methods, ensure:
- You're using Java 17 (not a newer version)
- The `maven-compiler-plugin` in `tripsapplication/pom.xml` has the `annotationProcessorPaths` configuration
- You run a clean build: `./mvnw-java17.sh clean compile`

## Architecture Overview

### Spring Boot + JavaFX Integration

TRIPS uses a sophisticated integration pattern to combine Spring Boot with JavaFX:

1. **Entry Point**: `TripsSpringBootApplication` - Standard Spring Boot main class
2. **JavaFX Application**: `TripsFxApplication` - Launches JavaFX and creates Spring ApplicationContext
3. **Stage Initialization**: `PrimaryStageInitializer` - Listens for `StageReadyEvent` and loads main window
4. **Dependency Injection**: Uses **FxWeaver** library to inject Spring beans into JavaFX controllers

This means:
- All controllers are Spring `@Component` beans
- Services and repositories use standard Spring annotations (`@Service`, `@Repository`)
- JavaFX FXML controllers get dependencies via FxWeaver constructor injection
- Use `ApplicationEventPublisher` for cross-component communication

### Core Application Structure

```
TripsSpringBootApplication (main)
  └─> TripsFxApplication (JavaFX lifecycle)
      └─> PrimaryStageInitializer (StageReadyEvent listener)
          └─> MainPane (primary controller)
              ├─> MenuBarController
              ├─> ToolBarController
              ├─> StatusBarController
              └─> MainSplitPaneManager
                  ├─> InterstellarSpacePane (3D visualization)
                  └─> SidePanel (accordion with multiple views)
```

### Key Architectural Layers

1. **Controller Layer** (`@Component`): JavaFX controllers managing UI
   - `MainPane`: Primary window controller (~1500 lines)
   - Sub-controllers: MenuBarController, ToolBarController, StatusBarController
   - Dialog controllers in `/dialogs/` package

2. **Service Layer** (`@Service`): Business logic
   - `StarService`: Star queries and filtering
   - `DatabaseManagementService`: Database lifecycle
   - `DataExportService/DataImportService`: Data interchange
   - `LargeGraphSearchService`: Asynchronous graph algorithms

3. **Repository Layer** (`@Repository`): JPA repositories
   - `StarObjectRepository`: Star data persistence
   - `DataSetDescriptorRepository`: Dataset metadata
   - Custom queries using Spring Data JPA and Criteria API

4. **Manager Components** (`@Component`): Complex subsystem orchestration
   - `PlotManager`: Star visualization orchestration
   - `RouteManager`: Route creation and display
   - `StarPlotManager`: 3D star rendering
   - `TransitManager`: Transit calculations

### Event-Driven Communication

The application uses Spring's event publishing for decoupled communication:

```java
@Component
public class SomeService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void doSomething() {
        eventPublisher.publishEvent(new StatusUpdateEvent("Status message"));
    }
}

@Component
public class SomeController {
    @EventListener
    public void handleStatusUpdate(StatusUpdateEvent event) {
        // Handle event
    }
}
```

Common events:
- `StatusUpdateEvent`: UI status bar updates
- `RoutingPanelUpdateEvent`: Route visualization updates
- `ColorPaletteChangeEvent`: Theme changes
- `DataSetContextChangeEvent`: Active dataset changes
- `DisplayStarEvent`: Star selection/highlighting

### Central Application State

`TripsContext` is a Spring `@Component` singleton holding application-wide state:
- Current plot information
- Search context (active dataset, query parameters)
- Application preferences
- Event-driven updates to all listeners

Access via dependency injection:
```java
@Component
public class SomeController {
    private final TripsContext tripsContext;

    @Autowired
    public SomeController(TripsContext tripsContext) {
        this.tripsContext = tripsContext;
    }
}
```

## Data Model

### Primary Entity: StarObject

Located in `jpa/model/StarObject.java`, this is the central data model with:
- **Catalog IDs**: HIP, HD, Gliese, Tycho2, Gaia DR2/DR3, 2MASS
- **3D Coordinates**: x, y, z (in light years, origin at Sol/Earth)
- **Physical Properties**: mass, luminosity, temperature, spectral class, radius
- **Names**: displayName, commonName, systemName, multiple aliases
- **Custom Fields**: Extensible schema via custom data columns
- **Relationships**: Notes, polity affiliation, planet systems

### Dataset System

`DataSetDescriptor` manages dataset metadata:
- Dataset name (primary key)
- File source and creation info
- Number of stars, distance range
- Custom data column definitions
- Associated routes and transit definitions
- UI theme information

Multiple datasets can exist; one is "active" at a time via `DataSetContext`.

### Coordinate System

- **Origin**: Sol (Earth's star system) at (0, 0, 0)
- **Units**: Light years
- **Axes**: Right-handed 3D coordinate system
- **Transformations**: Support for galactic and equatorial coordinate systems via `AstrographicTransformer`

## 3D Visualization System

### InterstellarSpacePane

The main 3D visualization component:
- Extends JavaFX `Pane`
- Contains `SubScene` with `PerspectiveCamera`
- Manages scene graph with multiple render groups:
  - `world`: 3D objects (stars, routes, grids)
  - `root`: 2D overlays (labels, UI elements)
- Handles user interactions: mouse rotate, zoom, pan
- Animation support for rotations

### Star Rendering Pipeline

1. **Query**: `StarService.findStarsByQuery(AstroSearchQuery)` retrieves stars from database
2. **Transform**: `PlotManager` converts `StarObject` to `StarDisplayRecord` (lightweight display model)
3. **Create Geometry**: `StarPlotManager` creates JavaFX 3D nodes (Sphere or MeshView)
4. **Add to Scene**: Nodes added to `InterstellarSpacePane.world` group
5. **Interaction**: Selection model, context menus, labels

### Star Appearance

- **Color**: Based on spectral class (O=blue, B=blue-white, A=white, F=yellow-white, G=yellow, K=orange, M=red)
- **Size**: Based on magnitude or luminosity (configurable)
- **Labels**: Displayed for important/close stars based on display score
- **Selection**: Click to select, shows properties in side panel

### 3D Objects and Meshes

Custom 3D shapes defined in `/resources/objects/`:
- Star shapes (4-point, 5-point, moravian stars)
- Geometric shapes (tetrahedron, cube, octahedron, dodecahedron, icosahedron)
- Created via `CustomObjectFactory` and `StellarEntityFactory`

## Routing and Graph Algorithms

### Graph-Based Pathfinding

TRIPS uses **JGraphT** library for graph algorithms:

```java
Graph<String, DefaultEdge> routingGraph = new SimpleWeightedGraph<>(DefaultEdge.class);
// Nodes: Star IDs
// Edges: Transits (weighted by distance)
// Algorithms: Dijkstra, Yen's K-Shortest Paths
```

### Route Finding Modes

1. **RouteFinderInView**: Routes within currently displayed stars (fast, for small datasets)
2. **RouteFinderDataset**: Routes across entire dataset (asynchronous, for large datasets 100K+ stars)

### Transit System

Transits are possible jumps between stars:
- Calculated by `TransitManager` based on maximum jump distance
- Filtered by user-defined `TransitDefinitions` and `TransitSettings`
- Used to build graph edges for pathfinding

### Routing Workflow

1. Load stars (entire dataset or filtered subset)
2. Calculate transits within max jump distance (`SparseTransitComputor`)
3. Build weighted graph (JGraphT)
4. Apply pathfinding algorithm (Yen's K-Shortest Paths returns multiple alternatives)
5. User selects preferred route
6. Display route in 3D scene as connected line segments
7. Persist route in dataset via `RouteManager`

### Performance Optimizations

For large datasets:
- `SparseStarRecord`: Minimal star data for graph building
- `LargeGraphSearchService`: Asynchronous processing with JavaFX Service
- Progress reporting and cancellation support
- Parallel transit calculation

## Data Import/Export

### Supported Formats

**Import**:
- CSV files (various stellar catalog formats)
- Excel (.xlsx) via Apache POI
- TRIPS native format (.trips.csv with custom schema)
- Gaia DR2/DR3 catalog data (specialized dialogs)

**Export**:
- CSV
- Excel
- TRIPS native format with custom fields

### Import Process

1. User selects file via `DataImportService`
2. Format detection and parsing
3. Mapping to `StarObject` entities
4. Validation (duplicate checking, coordinate validation)
5. Bulk insert into database via `BulkLoadService` (asynchronous with progress)
6. Update `DataSetDescriptor` metadata

### Custom Data Fields

Datasets can define custom columns beyond standard star properties:
- Defined in `DataSetDescriptor.customDataDefinitions`
- Stored in `StarObject.customData1` through `customData10` fields
- UI automatically adapts to show custom columns

## FXML and UI Structure

### FXML Files Location

`/src/main/resources/com/teamgannon/trips/`

Main FXML:
- `controller/MainPane.fxml`: Primary window layout
- `controller/toolbar/toolbar.fxml`: Toolbar UI
- `controller/statusbar/StatusBar.fxml`: Status bar UI
- `controller/menubar/*.fxml`: Individual menu definitions

### Controller Pattern

JavaFX controllers use FXML with Spring DI:

```java
@Component
@FXMLController  // From FxWeaver
public class MyController {
    @FXML private Button myButton;

    private final SomeService service;

    @Autowired  // Constructor injection
    public MyController(SomeService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        // Initialize UI components
    }

    @FXML
    public void handleButtonClick(ActionEvent event) {
        // Event handler
    }
}
```

### Dialog Creation Pattern

Dialogs are typically Spring components loaded via FxWeaver:

```java
DialogPane pane = fxWeaver.loadView(MyDialog.class);
Dialog<ButtonType> dialog = new Dialog<>();
dialog.setDialogPane(pane);
Optional<ButtonType> result = dialog.showAndWait();
```

## Configuration and Preferences

### Application Configuration

Located in `application.yml`:
- Database connection settings (H2)
- HikariCP connection pool settings
- JPA/Hibernate configuration
- Application metadata (version, title, contributors)

### User Preferences

Managed by `ApplicationPreferences` and persisted via `SystemPreferencesService`:
- Star display preferences (size, color schemes)
- UI themes and color palettes (`ColorPalette`)
- Default search parameters
- File paths and directories
- Stored in serialized format in `files/programdata/`

### Color Palettes

`ColorPalette` class defines themes for:
- Background colors
- Grid colors
- Text colors
- Star colors by spectral class
- Route colors

Change palette via `ViewPreferencesDialog` or publish `ColorPaletteChangeEvent`.

## Working with the Codebase

### Adding a New Star Property

1. Add field to `StarObject.java` (JPA entity)
2. Update database schema (Hibernate auto-update or manual migration)
3. Update import/export services to map the field
4. Update `StarDisplayRecord` if needed for visualization
5. Update UI dialogs/forms to display the property

### Adding a New Search Filter

1. Add filter parameter to `AstroSearchQuery` class
2. Update `StarObjectRepository` with new query method
3. Modify `QueryDialog` or `AdvancedQueryDialog` UI to expose filter
4. Update `StarService` to handle new filter in query building

### Adding a New Event Type

1. Create event class extending `ApplicationEvent`
2. Publish via `ApplicationEventPublisher.publishEvent(event)`
3. Create `@EventListener` methods in components that need to respond
4. Consider adding to `TripsContext` if it affects global application state

### Creating a New Dialog

1. Create FXML file in appropriate `/dialogs/` subdirectory
2. Create controller class with `@Component` and `@FXMLController`
3. Use constructor injection for dependencies
4. Load via FxWeaver in parent controller
5. Return result via `Dialog<T>` or custom data object

### Asynchronous Operations

For long-running operations, extend `javafx.concurrent.Service<T>`:

```java
public class MyLongTask extends Service<ResultType> {
    @Override
    protected Task<ResultType> createTask() {
        return new Task<ResultType>() {
            @Override
            protected ResultType call() throws Exception {
                // Long operation
                updateProgress(current, total);
                updateMessage("Status message");
                return result;
            }
        };
    }
}
```

Use `ProgressDialog` or bind to UI progress indicators.

### Lombok Usage

The codebase uses Lombok extensively:
- `@Getter/@Setter`: Automatic getters/setters
- `@AllArgsConstructor/@NoArgsConstructor`: Constructor generation
- `@Builder`: Builder pattern
- `@Slf4j`: Logging (logger available as `log`)
- `@Data`: Combines @Getter, @Setter, @ToString, @EqualsAndHashCode

Ensure Lombok is configured in your IDE.

## Common Pitfalls

### JavaFX Thread Safety

All UI updates must occur on JavaFX Application Thread:
```java
Platform.runLater(() -> {
    // UI update code
});
```

For background tasks, use `Service<T>` and update UI via progress/message properties.

### Database Transactions

Service methods that modify data should be `@Transactional`:
```java
@Service
public class MyService {
    @Transactional
    public void modifyData() {
        // Database operations
    }
}
```

### FxWeaver Controller Loading

Controllers loaded via FxWeaver must:
1. Be Spring `@Component` beans
2. Have `@FXMLController` annotation
3. Use constructor injection (not field injection)
4. Match FXML file `fx:controller` attribute with full class name

### Coordinate System Consistency

Always verify coordinate system when working with star positions:
- Database stores Cartesian (x, y, z) in light years
- Some dialogs use equatorial (RA, Dec, distance) or galactic coordinates
- Use `AstrographicTransformer` for conversions

## Technology Stack

- **Language**: Java 17
- **UI Framework**: JavaFX 17
- **Backend**: Spring Boot 2.7.4
- **Database**: H2 (embedded, file-based)
- **ORM**: Spring Data JPA with Hibernate
- **Dependency Injection**: Spring Framework + FxWeaver
- **Graph Algorithms**: JGraphT 1.5.1
- **Build Tool**: Maven
- **Code Generation**: Lombok
- **3D Graphics**: JavaFX 3D
- **Scripting**: Groovy support
- **Data Processing**: Apache POI (Excel), OpenCSV, Jackson
- **Validation**: JSR-305, javax.validation
