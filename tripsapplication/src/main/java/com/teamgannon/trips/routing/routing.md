# Routing Package

## Overview

The routing package provides functionality for finding and visualizing **interstellar routes** between stars in the TRIPS 3D stellar cartography application. A route represents a multi-hop path connecting two stars through a series of intermediate waypoints within a specified jump distance.

## What Are Routes?

Routes are paths connecting stars that help users:

- Plan interstellar travel between distant star systems
- Visualize the shortest paths using different jump distance constraints
- Compare multiple alternative paths between the same endpoints
- Build custom routes manually by clicking on stars

Users can define **route parameters** including:
- Origin and destination stars
- Upper and lower bounds for jump distance (light years)
- Number of alternative paths to find
- Visual properties (color, line width)
- Spectral class and polity exclusions

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        RouteManager                              │
│  (Spring @Component - coordinates route visualization)          │
└──────────────────────────────────┬──────────────────────────────┘
                                   │
           ┌───────────────────────┼───────────────────┐
           │                       │                   │
           ▼                       ▼                   ▼
┌──────────────────┐      ┌─────────────────┐  ┌─────────────────────┐
│  RoutePlotter    │      │ CurrentManual   │  │ RouteFindingService │
│  (route plotting │      │ Route           │  │ (algorithm,         │
│   and rendering) │      │ (real-time      │  │  JGraphT)           │
│                  │      │  user drawing)  │  │                     │
└────────┬─────────┘      └────────┬────────┘  └─────────────────────┘
         │                         │                    │
         │    ┌────────────────────┘                    ▼
         │    │                                ┌─────────────────────┐
         ▼    ▼                                │     RouteGraph      │
┌──────────────────┐                           │  (JGraphT wrapper,  │
│  RouteDisplay    │                           │   Yen's K-shortest) │
│  (state, segment │                           └─────────────────────┘
│   tracking)      │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│RouteLabelManager │
│  (2D billboard   │
│   label mgmt)    │
└──────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                       RouteDescriptor                            │
│  (Data model - stars, segments, lengths, color, line width)     │
└─────────────────────────────────────────────────────────────────┘
```

## Key Components

### Core Coordination

| Class | Purpose |
|-------|---------|
| `RouteManager` | Spring @Component - main entry point, coordinates route visualization, delegates to specialized handlers |
| `RoutePlotter` | Handles route plotting/rendering - converts route models to 3D graphics, manages segment deduplication |
| `CurrentManualRoute` | Manages real-time manual route creation with undo support |

### Route Display and Labels

| Class | Purpose |
|-------|---------|
| `RouteDisplay` | Route state management - visibility toggling, segment tracking, route group management |
| `RouteLabelManager` | Billboard label positioning - 2D labels tracking 3D anchor points with coordinate transformation |
| `RouteGraphicsUtil` | Creates 3D line segments (cylinders) and distance labels |
| `RouteBuilderUtils` | Creates complete route graphics, checks star visibility |
| `PartialRouteUtils` | Handles visualization when some stars not visible in viewport |

### Route Finding

| Class | Purpose |
|-------|---------|
| `RouteFindingService` | Core algorithm with caching - K-shortest paths using JGraphT |
| `RouteCache` | In-memory LRU cache for route finding results |
| `RouteCacheKey` | Immutable cache key based on algorithmic parameters |
| `RouteFinderInView` | Async route finder for visible stars with progress dialog |
| `InViewRouteFinderService` | JavaFX Service for async in-view route finding |
| `RouteFinderDataset` | Route finder across entire dataset (async, for large datasets) |
| `RouteGraph` | JGraphT wrapper for weighted graph and Yen's algorithm |
| `RouteBuilderHelper` | Constructs RouteDescriptor from path results |

### UI Components

| Class | Purpose |
|-------|---------|
| `RoutingPanel` | Side panel TableView showing all routes |
| `RouteFinderDialogInView` | Dialog for configuring route search (in-view) |
| `RouteFinderDialogInDataSet` | Dialog for configuring route search (full dataset) |
| `DisplayAutoRoutesDialog` | Dialog for selecting which routes to display |
| `RouteEditDialog` | Dialog for editing route properties |
| `ContextManualRoutingDialog` | Dialog for manual route creation |

### Data Models

| Class | Purpose |
|-------|---------|
| `RouteDescriptor` | Complete route specification with segments and metadata |
| `RouteFindingOptions` | User-specified parameters for route search |
| `RouteFindingResult` | Result object (success with routes or failure with message) |
| `RoutingMetric` | Route evaluation data (rank, length, segments) |
| `RouteSegment` | Single segment between two stars (for deduplication) |
| `PossibleRoutes` | Container for multiple route alternatives |

### Interfaces and Enums

| Interface/Enum | Purpose |
|----------------|---------|
| `RoutingCallback` | Callback for color change notifications |
| `RouteVisibility` | Enum: FULL, PARTIAL |
| `RoutingType` | Enum: AUTOMATIC, MANUAL, NONE |

## Component Responsibilities

### RouteManager (Coordinator)

The central coordinator that:
- Initializes visualization components (`setGraphics()`)
- Manages routing mode state (manual routing active, routing type)
- Delegates plotting to `RoutePlotter`
- Exposes manual route operations via `CurrentManualRoute`
- Controls route visibility and label updates

### RoutePlotter

Handles all route plotting operations:
- `plotRoutes(List<Route>)` - plots multiple database routes
- `plotRouteDescriptors(DataSetDescriptor, List<RoutingMetric>)` - plots automated route results
- `plotRoute(Route)` - plots a single route (full or partial)
- Manages segment deduplication to prevent visual overlap
- Converts database `Route` models to graphical `RouteDescriptor`

### RouteDisplay

Manages route display state:
- Route group management (add, remove, lookup by UUID)
- Route segment tracking for deduplication
- Visibility toggling (routes and labels)
- Manual routing mode state
- Delegates label operations to `RouteLabelManager`

### RouteLabelManager

Implements the billboard labels pattern:
- Links 3D anchor nodes to 2D labels
- Updates label positions after view changes
- Handles coordinate transformation (3D → scene → local)
- Clamps labels to viewport boundaries
- Manages label visibility based on viewport clipping

## Usage Workflow

### 1. Automated Route Finding (In-View)

Users can find routes between stars currently visible in the viewport:

```
┌────────────────────────────────────────────────────────────────┐
│ Find Routes (In View)                                           │
├────────────────────────────────────────────────────────────────┤
│ Origin Star:      [Sol              ▼]                         │
│ Destination Star: [Alpha Centauri   ▼]                         │
│                                                                │
│ Upper Bound (ly): [8.0    ]  Lower Bound (ly): [3.0    ]      │
│ Line Width:       [0.5    ]  Number of Paths:  [3      ]      │
│ Route Color:      [■ Coral]                                    │
│                                                                │
│ Exclude Spectral Classes:                                      │
│ [ ] O  [ ] B  [ ] A  [✓] M  [✓] L  [✓] T  [✓] Y               │
│                                                                │
│              [Find Routes]  [Cancel]                           │
└────────────────────────────────────────────────────────────────┘
```

### 2. Route Finding Process

When "Find Routes" is clicked:

1. `RouteFindingService.findRoutes()` is called with options
2. Stars are pruned based on spectral class and polity exclusions
3. `StarMeasurementService` calculates all valid transits (jumps)
4. `RouteGraph` builds a weighted graph using JGraphT
5. Connectivity is checked between origin and destination
6. Yen's K-shortest paths algorithm finds alternatives
7. Results returned as `RouteFindingResult`

```
Stars in View → Prune by Exclusions → Calculate Transits
                                              ↓
                                    Build Weighted Graph
                                              ↓
                              Check Connectivity (origin ↔ dest)
                                              ↓
                              Yen's K-Shortest Paths Algorithm
                                              ↓
                                    Return Route Results
```

### 3. Route Selection

After routes are found, users select which to display:

```
┌────────────────────────────────────────────────────────────────┐
│ Route Results                                                   │
├────────────────────────────────────────────────────────────────┤
│ [✓] Route 1: Sol → Barnard's → Alpha Centauri (7.2 ly)        │
│ [ ] Route 2: Sol → Wolf 359 → Alpha Centauri (8.1 ly)         │
│ [ ] Route 3: Sol → Luyten → Alpha Centauri (9.4 ly)           │
│                                                                │
│              [Plot Selected]  [Cancel]                         │
└────────────────────────────────────────────────────────────────┘
```

### 4. Manual Route Creation

Users can also create routes manually by clicking on stars:

1. Click "Manual Route" button
2. `RouteManager.setManualRoutingActive(true)`
3. Click on origin star → `CurrentManualRoute.startRoute()`
4. Click on subsequent stars → `CurrentManualRoute.continueRoute()`
5. Each segment is drawn in real-time with distance labels
6. Click "Finish" → `CurrentManualRoute.finishRoute()`
7. Route is saved and published via `NewRouteEvent`

### 5. Route Visualization

Routes are rendered as 3D cylinders connecting stars:

```java
// In RouteGraphicsUtil.createLineSegment()
Point3D yAxis = new Point3D(0, 1, 0);
Point3D diff = target.subtract(origin);
double height = diff.magnitude();

// Position at midpoint between stars
Point3D mid = target.midpoint(origin);
Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

// Rotate to align with vector between stars
Point3D axisOfRotation = diff.crossProduct(yAxis);
double angle = Math.acos(diff.normalize().dotProduct(yAxis));
Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);
```

### 6. Label Positioning (Billboard Pattern)

Route distance labels use the billboard pattern for screen-aligned display:

```
sceneRoot (2D)
├── subScene (3D viewport)
│   └── world (3D group - rotates with camera)
│       └── Route cylinders and endpoint spheres
└── labelDisplayGroup (2D group - stays fixed)
    └── Distance labels (always flat to screen)
```

**Two-Step Coordinate Transformation (in RouteLabelManager.updateLabels()):**

```java
// Step 1: Project 3D position to scene coordinates
Point3D sceneCoords = anchorNode.localToScene(Point3D.ZERO, true);

// Step 2: Check visibility within viewport
if (!isWithinViewport(sceneX, sceneY, viewportBounds)) {
    label.setVisible(false);
    continue;
}

// Step 3: Convert scene coordinates to local viewport coordinates
double localX = toLocalX(sceneX, viewportBounds);
double localY = toLocalY(sceneY, viewportBounds);

// Step 4: Clamp to prevent going off-screen
double clampedX = clampX(localX, label.getWidth());
double clampedY = clampY(localY, label.getHeight());

// Step 5: Apply position
label.getTransforms().setAll(new Translate(clampedX, clampedY));
```

## Algorithm Details

### Yen's K-Shortest Paths Algorithm

The routing system uses JGraphT's implementation of Yen's algorithm:

- **Complexity**: O(K × n × (m + n log n)) where K = paths, n = nodes, m = edges
- **Graph Type**: SimpleWeightedGraph with star names as vertices
- **Edge Weights**: Distance in light years between connected stars

### Star Pruning

Before route finding, stars are filtered based on user preferences:

```java
// Spectral class exclusion
if (options.getExcludedSpectralClasses().contains(spectralType)) {
    continue;  // Skip this star
}

// Polity exclusion
if (options.getExcludedPolities().contains(polity)) {
    continue;  // Skip this star
}
```

### Transit Calculation

Transits (possible jumps) are calculated using `StarMeasurementService`:

- Only star pairs within the specified distance bounds are connected
- Upper bound defines maximum jump distance
- Lower bound prevents very short jumps (optional)

### Graph Threshold

For performance, there's a limit on the number of stars:

| Stars | Behavior |
|-------|----------|
| ≤ 1500 | Normal route finding |
| > 1500 | Error message, use dataset-wide search instead |

## Event Integration

The routing system publishes events for cross-component communication:

| Event | When Published |
|-------|----------------|
| `NewRouteEvent` | Route created or finished |
| `RoutingStatusEvent` | Manual routing starts/stops |
| `DisplayRouteEvent` | Route visibility toggled |
| `UpdateRouteEvent` | Route properties edited |
| `DeleteRouteEvent` | Route deleted |

## Partial Route Handling

When some stars in a route are not visible in the current viewport:

1. `PartialRouteUtils.findPartialRoutes()` extracts visible segments
2. Each continuous visible section becomes a separate partial route
3. Partial routes are rendered with the same color/style as the full route
4. When view changes, partial routes are recalculated

```
Full Route: A → B → C → D → E → F
Visible:    A   B       D   E
Partial 1:  A → B
Partial 2:      D → E
```

## Constants

Key constants from `RoutingConstants`:

| Constant | Value | Purpose |
|----------|-------|---------|
| `GRAPH_THRESHOLD` | 1500 | Max stars for in-view route finding |
| `DEFAULT_NUMBER_PATHS` | 3 | Default K for K-shortest paths |
| `DEFAULT_LINE_WIDTH` | 0.5 | Default route line thickness |
| `DEFAULT_UPPER_DISTANCE` | 8.0 | Default max jump distance (ly) |
| `LABEL_CLIPPING_PADDING` | 20.0 | Padding for label visibility |
| `LABEL_EDGE_MARGIN` | 5.0 | Margin for label clamping |
| `ROUTE_POINT_SPHERE_RADIUS` | 0.05 | Radius of label anchor spheres |
| `LABEL_CORNER_RADIUS` | 3.0 | Corner radius for label backgrounds |
| `DARK_BACKGROUND_THRESHOLD` | 380 | RGB sum threshold for contrast |
| `RGB_MAX_VALUE` | 255 | Max RGB component value |
| `FIRST_SEGMENT_PREFIX` | "=>" | Prefix for first segment labels |
| `LABEL_SUFFIX` | "->" | Prefix for subsequent segment labels |

## Route Caching

Route finding results are cached to avoid recalculating the same routes multiple times.

### Cache Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                       RouteFindingService                        │
│  (checks cache before calculating, stores results after)         │
└──────────────────────────────────┬──────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                          RouteCache                              │
│  (LRU cache with thread-safe access, statistics tracking)       │
└──────────────────────────────────┬──────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                        RouteCacheKey                             │
│  (immutable key: origin, dest, bounds, paths, exclusions, hash) │
└─────────────────────────────────────────────────────────────────┘
```

### Cache Key Components

The cache key includes all parameters that affect route calculation:

| Component | Description |
|-----------|-------------|
| `originStarName` | Starting star |
| `destinationStarName` | Ending star |
| `upperBound` | Maximum jump distance (normalized to 2 decimals) |
| `lowerBound` | Minimum jump distance (normalized to 2 decimals) |
| `numberPaths` | K value for K-shortest paths |
| `starExclusions` | Spectral classes to exclude (sorted set) |
| `polityExclusions` | Polities to exclude (sorted set) |
| `starsHash` | Hash of available star names (for in-view caching) |

**Not included:** `color`, `lineWidth` (display-only parameters)

### Cache Features

- **LRU Eviction**: Oldest accessed entries removed when max size (50) reached
- **Thread-Safe**: Read/write locks for concurrent access
- **Statistics**: Tracks hits, misses, and hit rate
- **Manual Clear**: `RouteFindingService.clearCache()` for dataset changes

### Cache Usage

```java
// Cache is used automatically - just call findRoutes()
RouteFindingResult result = routeFindingService.findRoutes(options, starsInView, dataSet);

// To bypass cache:
RouteFindingResult result = routeFindingService.findRoutes(options, starsInView, dataSet, false);

// To clear cache (on dataset change):
routeFindingService.clearCache();

// To check statistics:
String stats = routeFindingService.getCacheStatistics();
// e.g., "RouteCache[size=12, hits=45, misses=23, hitRate=66.2%]"
```

### Cache Invalidation

The cache should be cleared when:
- Dataset changes (new data loaded)
- Star data is modified
- User explicitly requests cache clear

## Asynchronous Route Finding

Route finding runs asynchronously to prevent UI freezing.

### In-View Route Finding (Async)

The `RouteFinderInView` uses `InViewRouteFinderService` (JavaFX Service) to run route
finding in a background thread:

```
User clicks "Find Routes"
        │
        ▼
RouteFinderDialogInView (collect parameters)
        │
        ▼
InViewRouteFinderService.start()
        │
        ├───────────────────────────────────┐
        │ (Background Thread)               │ (UI Thread)
        ▼                                   ▼
RouteFindingService.findRoutes()     Progress Dialog
        │                                   │
        ▼                                   │
Cache check → Algorithm → Cache store       │
        │                                   │
        └───────────────────────────────────┘
                        │
                        ▼
               onSucceeded/onFailed
                        │
                        ▼
              DisplayAutoRoutesDialog
```

### Progress Feedback

During route finding, users see:
- Progress dialog with spinning indicator
- Status messages ("Finding routes...", "Found 3 routes")
- Cancel button to abort the operation

### Dataset-Wide Route Finding (Async)

For large datasets (>1500 stars), `RouteFinderDataset` uses `LargeGraphSearchService`:
- Parallel transit calculation via thread pool
- Detailed progress reporting (batch X of Y)
- Cancellation support

## Testing

Tests for the routing package should cover:

- `RouteFindingService` - algorithm correctness, pruning logic, caching
- `RouteCache` - LRU eviction, thread safety, statistics
- `RouteCacheKey` - equality, hash codes, normalization
- `RouteGraph` - graph construction, connectivity, path finding
- `PartialRouteUtils` - edge cases for visibility extraction
- `RouteGraphicsUtil` - coordinate transformations
- `RouteLabelManager` - label positioning and clamping
- `RoutePlotter` - route plotting and deduplication
- `InViewRouteFinderService` - async execution, cancellation

Run routing tests with:
```bash
./mvnw-java17.sh test -Dtest="com.teamgannon.trips.routing.**"
```

## Performance Considerations

1. **Large Datasets**: Use `RouteFinderDataset` with async `LargeGraphSearchService`
2. **Many Routes**: Label updates occur on every viewport change
3. **Complex Paths**: Yen's algorithm complexity increases with K
4. **Partial Routes**: Recalculated on each view change
5. **Segment Deduplication**: Uses HashSet for O(1) lookup
6. **Route Caching**: Avoids recalculating same routes (LRU cache, max 50 entries)
7. **Async In-View**: Route finding runs in background thread to prevent UI freeze

## Known Limitations

1. **Graph Threshold**: 1500 star limit for in-view route finding
2. **Cache Scope**: Cache is session-based (not persisted to database)
3. **Label Performance**: Many labels may cause frame drops
4. **Cache Key Size**: Large star collections increase cache key computation time
